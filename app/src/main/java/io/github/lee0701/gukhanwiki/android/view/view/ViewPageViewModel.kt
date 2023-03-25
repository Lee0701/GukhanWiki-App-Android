package io.github.lee0701.gukhanwiki.android.view.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lee0701.gukhanwiki.android.Loadable
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import io.github.lee0701.gukhanwiki.android.api.action.CategoryMembersItem
import io.github.lee0701.gukhanwiki.android.api.action.ParseResponse
import io.github.lee0701.gukhanwiki.android.view.SimplePageRenderer
import io.github.lee0701.gukhanwiki.android.view.WebViewRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ViewPageViewModel: ViewModel() {

    private val renderer: WebViewRenderer by lazy { SimplePageRenderer() }

    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private val _content = MutableLiveData<Loadable<String?>>()
    val content: LiveData<Loadable<String?>> = _content

    private val _associatedPage = MutableLiveData<String>()
    val associatedPage: LiveData<String> = _associatedPage

    private val _hideFab = MutableLiveData<Boolean>()
    val hideFab: LiveData<Boolean> = _hideFab

    private val _scrollY = MutableLiveData<Int>()
    val scrollY: LiveData<Int> = _scrollY

    fun loadPage(path: String, action: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            _content.postValue(Loadable.Loading())
            try {
                when(action) {
                    "history" -> {
                        val response = GukhanWikiApi.clientService.index(action = action, title = path)
                        val content = renderer.render(response)
                        _title.postValue(path)
                        _content.postValue(Loadable.Loaded(content))
                        _hideFab.postValue(true)
                    }
                    else -> {
                        val response = GukhanWikiApi.actionApiService.parse(page = path)
                        if(response.error != null) {
                            errorPage(path, action, response)
                        } else {
                            contentPage(path, action, response)
                        }
                    }
                }
            } catch(ex: IOException) {
                _content.postValue(Loadable.Error(ex))
            } catch(ex: HttpException) {
                _content.postValue(Loadable.Error(ex))
            }
        }
    }

    private suspend fun contentPage(path: String, action: String?, response: ParseResponse) {
        // Retrieve a link for the talk page
        val infoResponse = GukhanWikiApi.actionApiService.info(titles = path, inprop = "associatedpage")
        val associatedPage = infoResponse.query?.pages?.values?.firstOrNull()?.associatedPage
        if(associatedPage != null) _associatedPage.postValue(associatedPage!!)

        val categoryResponse = GukhanWikiApi.actionApiService.parse(page = path, prop = "categorieshtml")
        val categories = categoryResponse.parse?.categoriesHtml?.html.orEmpty()

        val categoryMembers = getCategoryMembers(path)
        val categoryMembersContent =
            if(categoryMembers.isEmpty()) ""
            else "<ul>" + categoryMembers.joinToString("") { formatCategoryMember(it) } + "</ul>"

        val title = response.parse?.title
        val content = response.parse?.text?.text
        if(content == null) {
            _content.postValue(Loadable.Error(RuntimeException("Result text is null")))
        } else {
            _title.postValue(title ?: path)
            _content.postValue(Loadable.Loaded(content + categoryMembersContent + categories))
            _hideFab.postValue(false)
        }
    }

    private suspend fun errorPage(path: String, action: String?, response: ParseResponse) {
        val infoResponse = GukhanWikiApi.actionApiService.info(titles = path)
        val title = infoResponse.query?.pages?.values?.firstOrNull()?.title

        val code = response.error?.get("code")
        if(code == "pagecannotexist" || action == "history") {
            val content = GukhanWikiApi.clientService.index(title = path, action = action)
            val renderedContent = renderer.render(content)
            _title.postValue(title ?: path)
            _content.postValue(Loadable.Loaded(renderedContent))
            _hideFab.postValue(true)
        } else {
            _title.postValue(title ?: path)
            _content.postValue(Loadable.Error(RuntimeException(code)))
        }
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

}