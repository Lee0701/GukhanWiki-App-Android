package io.github.lee0701.gukhanwiki.android.view.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lee0701.gukhanwiki.android.Loadable
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.HttpException

class ReviewEditViewModel: ViewModel() {

    private val _page = MutableLiveData<Loadable<Page>>()
    val page: LiveData<Loadable<Page>> = _page

    private val _html = MutableLiveData<Loadable<String>>()
    val html: LiveData<Loadable<String>> = _html

    private val _result = MutableLiveData<Loadable<Page>>()
    val result: LiveData<Loadable<Page>> = _result

    fun reviewEdit(page: Page) {
        viewModelScope.launch {
            _page.postValue(Loadable.Loaded(page))
            val response = GukhanWikiApi.actionApiService.parsePost(
                text = page.wikiText,
            )
            val result = response.parse?.text?.text
            if(result != null) {
                _html.postValue(Loadable.Loaded(result))
            }
        }
    }

    fun updatePage(page: Page, summary: String?) {
        viewModelScope.launch {
            _result.postValue(Loadable.Loading())
            try {
                val csrfToken = GukhanWikiApi.actionApiService.retrieveToken(type = "csrf").query.tokens["csrftoken"]
                val response = GukhanWikiApi.actionApiService.editMultipart(
                    title = MultipartBody.Part.createFormData("title", page.title),
                    summary = summary?.let { MultipartBody.Part.createFormData("summary", it) },
                    section = page.section?.let { MultipartBody.Part.createFormData("section", it) },
                    baseRevId = page.revId?.let { MultipartBody.Part.createFormData("baserevid", it.toString()) },
                    token = csrfToken?.let { MultipartBody.Part.createFormData("token", it) },
                    text = MultipartBody.Part.Companion.createFormData("text", page.wikiText),
                )
                val result = response.edit
                if(result != null) {
                    if(result.result == "Success") {
                        _result.postValue(Loadable.Loaded(
                            page.copy(
                                revId = result.newRevId,
                                wikiText = page.wikiText
                            )
                        ))
                    } else if(result.captcha?.error != null) {
                        _result.postValue(Loadable.Error(RuntimeException("captcha")))
                    } else {
                        _result.postValue(Loadable.Error(RuntimeException(result.result)))
                    }
                } else {
                    val errorCode = response.error?.get("code")
                    if(errorCode != null) _result.postValue(Loadable.Error(RuntimeException(errorCode)))
                    else _result.postValue(Loadable.Error(RuntimeException("null")))
                }
            } catch(ex: HttpException) {
                ex.printStackTrace()
                _result.postValue(Loadable.Error(ex))
            }
        }
    }

}