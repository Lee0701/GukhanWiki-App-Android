package io.github.lee0701.gukhanwiki.android.view.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lee0701.gukhanwiki.android.Loadable
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import io.github.lee0701.gukhanwiki.android.api.SeonbiApiService
import io.github.lee0701.gukhanwiki.android.api.action.Page
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.HttpException

class ReviewEditViewModel: ViewModel() {

    private val _page = MutableLiveData<Loadable<Page>>()
    val page: LiveData<Loadable<Page>> = _page

    private val _content = MutableLiveData<Loadable<String>>()
    val content: LiveData<Loadable<String>> = _content

    private val _html = MutableLiveData<Loadable<String>>()
    val html: LiveData<Loadable<String>> = _html

    private val _result = MutableLiveData<Loadable<Page>>()
    val result: LiveData<Loadable<Page>> = _result

    fun reviewEdit(page: Page, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _page.postValue(Loadable.Loaded(page))
            _content.postValue(Loadable.Loaded(content))
            _html.postValue(Loadable.Loading())
            val response = GukhanWikiApi.actionApiService.parsePost(
                text = content,
            )
            val result = response.parse?.text?.text
            if(result != null) {
                val seonbiResult = GukhanWikiApi.seonbiService.seonbi(body = SeonbiApiService.Config(result)).resultHtml ?: result
                _html.postValue(Loadable.Loaded(seonbiResult))
            }
        }
    }

    fun updateContent(content: String) {
        _content.postValue(Loadable.Loaded(content))
    }

    fun updatePage(page: Page, content: String, summary: String?, minor: Boolean?, csrfToken: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            _result.postValue(Loadable.Loading())
            try {
                val response = GukhanWikiApi.actionApiService.editMultipart(
                    title = MultipartBody.Part.createFormData("title", page.title),
                    summary = summary?.let { MultipartBody.Part.createFormData("summary", it) },
                    section = page.section?.let { MultipartBody.Part.createFormData("section", it) },
                    baseRevId = page.revId?.let { MultipartBody.Part.createFormData("baserevid", it.toString()) },
                    minor = minor?.let { MultipartBody.Part.createFormData("minor", it.toString()) },
                    token = csrfToken.let { MultipartBody.Part.createFormData("token", it ?: "+\\") },
                    text = MultipartBody.Part.Companion.createFormData("text", content),
                )
                val result = response.edit
                if(result != null) {
                    if(result.result == "Success") {
                        _result.postValue(Loadable.Loaded(
                            page.copy(
                                revId = result.newRevId,
                            )
                        ))
                    } else if(result.captcha?.error != null) {
                        _result.postValue(Loadable.Error(RuntimeException("captcha")))
                    } else {
                        _result.postValue(Loadable.Error(RuntimeException(result.result)))
                    }
                } else {
                    val errorCode = response.error?.get("code")
                    _result.postValue(Loadable.Error(RuntimeException(errorCode)))
                }
            } catch(ex: HttpException) {
                ex.printStackTrace()
                _result.postValue(Loadable.Error(ex))
            }
        }
    }

}