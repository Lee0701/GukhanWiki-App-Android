package io.github.lee0701.gukhanwiki.android.view.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lee0701.gukhanwiki.android.Result
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import io.github.lee0701.gukhanwiki.android.api.action.Page
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.HttpException

class ReviewEditViewModel: ViewModel() {

    private val _page = MutableLiveData<Result<Page>>()
    val page: LiveData<Result<Page>> = _page

    private val _content = MutableLiveData<Result<String>>()
    val content: LiveData<Result<String>> = _content

    private val _html = MutableLiveData<Result<String>>()
    val html: LiveData<Result<String>> = _html

    private val _result = MutableLiveData<Result<Page>>()
    val result: LiveData<Result<Page>> = _result

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _html.postValue(Result.Error(throwable))
        throwable.printStackTrace()
    }

    fun reviewEdit(page: Page, content: String) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _page.postValue(Result.Loaded(page))
            _content.postValue(Result.Loaded(content))
            _html.postValue(Result.Loading())
            val response = GukhanWikiApi.actionApiService.parsePost(
                text = content,
            )
            val result = response.parse?.text?.text
            if(result != null) {
                _html.postValue(Result.Loaded(result))
            }
        }
    }

    fun updateContent(content: String) {
        _content.postValue(Result.Loaded(content))
    }

    fun updatePage(page: Page, content: String, summary: String?, minor: Boolean?, csrfToken: String?) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _result.postValue(Result.Loading())
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
                        _result.postValue(Result.Loaded(
                            page.copy(
                                revId = result.newRevId,
                            )
                        ))
                    } else if(result.captcha?.error != null) {
                        _result.postValue(Result.Error(RuntimeException("captcha")))
                    } else {
                        _result.postValue(Result.Error(RuntimeException(result.result)))
                    }
                } else {
                    val errorCode = response.error?.get("code")
                    _result.postValue(Result.Error(RuntimeException(errorCode)))
                }
            } catch(ex: HttpException) {
                ex.printStackTrace()
                _result.postValue(Result.Error(ex))
            }
        }
    }

}