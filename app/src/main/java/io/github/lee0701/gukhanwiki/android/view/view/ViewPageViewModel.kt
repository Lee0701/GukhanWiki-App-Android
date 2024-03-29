package io.github.lee0701.gukhanwiki.android.view.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lee0701.gukhanwiki.android.Result
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import io.github.lee0701.gukhanwiki.android.api.SeonbiApiService
import io.github.lee0701.gukhanwiki.android.api.action.CategoryMembersItem
import io.github.lee0701.gukhanwiki.android.api.action.ParseResponse
import io.github.lee0701.gukhanwiki.android.view.SimplePageRenderer
import io.github.lee0701.gukhanwiki.android.view.WebViewRenderer
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ViewPageViewModel: ViewModel() {

    private val renderer: WebViewRenderer by lazy { SimplePageRenderer() }

    private val _url = MutableLiveData<String>()
    val url: LiveData<String> = _url

    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private val _refresh = MutableLiveData<suspend () -> Unit>()
    val refresh: LiveData<suspend () -> Unit> = _refresh

    private val _content = MutableLiveData<Result<String?>>()
    val content: LiveData<Result<String?>> = _content

    private val _associatedPage = MutableLiveData<String>()
    val associatedPage: LiveData<String> = _associatedPage

    private val _hideFab = MutableLiveData<Boolean>()
    val hideFab: LiveData<Boolean> = _hideFab

    private val _scrollY = MutableLiveData<Int>()
    val scrollY: LiveData<Int> = _scrollY

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _content.postValue(Result.Error(throwable))
        throwable.printStackTrace()
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) { refresh.value?.invoke() }
    }

    fun loadPage(path: String, oldId: String? = null, action: String? = null,
                 query: Map<String, String> = mapOf(), ignoreErrors: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _content.postValue(Result.Loading())
            updateTitleAndUrl(path, action, query)
            try {
                if(isNonDocumentPage(path)) {
                    _refresh.postValue {
                        nonDocumentPage(path, action, query, ignoreErrors)
                    }
                } else if(action == "history") {
                    _refresh.postValue {
                        historyPage(path, action, query)
                    }
                } else if("diff" in query) {
                    _refresh.postValue {
                        diffPage(path, action, query)
                    }
                } else {
                    _refresh.postValue {
                        val response = if(oldId != null)
                            GukhanWikiApi.actionApiService.parse(oldid = oldId, query = query)
                        else
                            GukhanWikiApi.actionApiService.parse(page = path, query = query)
                        if(response.error != null) {
                            errorPage(path, action, response)
                        } else {
                            contentPage(path, action, response)
                        }
                    }
                    if(oldId != null) _hideFab.postValue(true)
                }
            } catch(ex: IOException) {
                _content.postValue(Result.Error(ex))
            } catch(ex: HttpException) {
                _content.postValue(Result.Error(ex))
            }
        }
    }

    private suspend fun diffPage(path: String, action: String?, query: Map<String, String>) {
        _hideFab.postValue(true)
        val response = GukhanWikiApi.clientService.index(action = action, query = query)
        val content = renderer.render(response.body().orEmpty()).html()
        _content.postValue(Result.Loaded(content))
    }

    private suspend fun nonDocumentPage(path: String, action: String?,
                                        query: Map<String, String>, ignoreErrors: Boolean = false) {
        val result = kotlin.runCatching {
            _hideFab.postValue(true)
            val response = GukhanWikiApi.clientService.index(title = path, action = action, query = query)
            val content = if(!response.isSuccessful) {
                if(!ignoreErrors) throw RuntimeException(response.message())
                else renderer.render(response.errorBody()?.string().orEmpty()).html()
            } else {
                renderer.render(response.body().orEmpty()).html()
            }
            _content.postValue(Result.Loaded(content))
        }
        if(result.isFailure) result.getOrThrow()
    }

    private suspend fun historyPage(path: String, action: String?, query: Map<String, String>) {
        _hideFab.postValue(true)
        val response = GukhanWikiApi.clientService.index(action = action, title = path, query = query)
        val seonbiResultContent = GukhanWikiApi.seonbiService.seonbi(
            body = SeonbiApiService.Config(response.body().orEmpty())
        ).resultHtml ?: response.body().orEmpty()
        val content = renderer.render(seonbiResultContent).html()
        _title.postValue(path)
        _content.postValue(Result.Loaded(content))
    }

    private suspend fun contentPage(path: String, action: String?, response: ParseResponse) {
        // Retrieve a link for the talk page
        val infoResponse = GukhanWikiApi.actionApiService.info(titles = path, inprop = "associatedpage")
        val associatedPage = infoResponse.query?.pages?.values?.firstOrNull()?.associatedPage
        if(associatedPage != null) _associatedPage.postValue(associatedPage!!)

        val categoryResponse = GukhanWikiApi.actionApiService.parse(page = path, query = mapOf("prop" to "categorieshtml"))
        val categories = categoryResponse.parse?.categoriesHtml?.html.orEmpty()

        val categoryMembers = getCategoryMembers(path)
        val categoryMembersContent =
            if(categoryMembers.isEmpty()) ""
            else "<ul>" + categoryMembers.joinToString("") { formatCategoryMember(it) } + "</ul>"

        val title = response.parse?.title
        val content = response.parse?.text?.text
        if(content == null) {
            _content.postValue(Result.Error(RuntimeException("Result text is null")))
        } else {
            val seonbiResultContent = GukhanWikiApi.seonbiService.seonbi(body = SeonbiApiService.Config(content)).resultHtml ?: content
            _title.postValue(title ?: path)
            _content.postValue(Result.Loaded(seonbiResultContent + categoryMembersContent + categories))
        }
    }

    private suspend fun errorPage(path: String, action: String?, response: ParseResponse) {
        val infoResponse = GukhanWikiApi.actionApiService.info(titles = path)
        val title = infoResponse.query?.pages?.values?.firstOrNull()?.title

        val code = response.error?.get("code")
        if(code == "pagecannotexist" || action == "history") {
            _hideFab.postValue(true)
            val content = GukhanWikiApi.clientService.index(title = path, action = action)
            val seonbiResultContent = GukhanWikiApi.seonbiService.seonbi(body = SeonbiApiService.Config(content)).resultHtml ?: content
            val renderedContent = renderer.render(seonbiResultContent).html()
            _title.postValue(title ?: path)
            _content.postValue(Result.Loaded(renderedContent))
        } else {
            _title.postValue(title ?: path)
            _content.postValue(Result.Error(RuntimeException(code)))
        }
    }

    private fun isSpecialPage(path: String): Boolean {
        val namespace = getNamespace(path)
        return namespace in GukhanWikiApi.SPECIAL_PAGE_NAMESPACE
    }

    private suspend fun updateTitleAndUrl(path: String, action: String?, query: Map<String, String>) {
        val infoResponse = GukhanWikiApi.actionApiService.info(titles = path, inprop = "displaytitle")
        val title = infoResponse.query?.pages?.values?.firstOrNull()?.title
        _title.postValue(title ?: path)

        val queryString = query + listOfNotNull(action?.let { "action" to it }).toMap()
        val url = GukhanWikiApi.DOC_URL.toString() + title +
                queryString.mapNotNull { (k, v) -> "$k=" + GukhanWikiApi.encodeUriComponent(v) }
                    .joinToString("&")
                    .let { if(it.isBlank()) "" else "?$it" }
        _url.postValue(url)
    }

    fun updateScroll(scrollY: Int) {
        _scrollY.postValue(scrollY)
    }

    private suspend fun getCategoryMembers(path: String): List<CategoryMembersItem> {
        var cmContinue: String? = null
        val result = mutableListOf<CategoryMembersItem>()
        do {
            val categoryMembersResponse = GukhanWikiApi.actionApiService.categoryMembers(
                cmTitle = path, cmLimit = 20, cmContinue = cmContinue)
            cmContinue = categoryMembersResponse.queryContinue?.cmContinue
            result += categoryMembersResponse.result?.categoryMembers ?: emptyList()
        } while(cmContinue != null)
        return result
    }

    private fun formatCategoryMember(item: CategoryMembersItem): String {
        val title = item.title ?: return ""
        val url = GukhanWikiApi.DOC_URL.toString() + GukhanWikiApi.encodeUriComponent(title)
        return "<li><a href=\"$url\">$title</a></li>"
    }

    private fun getSeonbiConfig(): Map<String, Any> = mapOf(
        "contentType" to "text/html",
        "quote" to "CurvedQuotes",
        "cite" to "AngleQuotes",
        "arrow" to mapOf<String, Any>(
            "bidirArrow" to true,
            "doubleArrow" to true,
        ),
        "ellipsis" to true,
        "emDash" to true,
        "stop" to "Vertical",
    )

    private fun isNonDocumentPage(path: String): Boolean {
        val namespace = getNamespace(path)
        return namespace in GukhanWikiApi.SPECIAL_PAGE_NAMESPACE
                || namespace in GukhanWikiApi.FILE_NAMESPACE
    }

    private fun getNamespace(path: String): String? {
        val segments = path.split(":")
        if(segments.size < 2) return null
        return segments.takeLast(2).first()
    }

}