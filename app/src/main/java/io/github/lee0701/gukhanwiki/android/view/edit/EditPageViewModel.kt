package io.github.lee0701.gukhanwiki.android.view.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lee0701.gukhanwiki.android.Loadable
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import io.github.lee0701.gukhanwiki.android.api.action.EditResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.io.Serializable

class EditPageViewModel: ViewModel() {

    private val _page = MutableLiveData<Loadable<Page>>()
    val page: LiveData<Loadable<Page>> = _page

    private val _result = MutableLiveData<Loadable<EditResponse>>()
    val result: LiveData<Loadable<EditResponse>> = _result

    fun loadPageSource(title: String, section: String?) {
        viewModelScope.launch {
            _page.postValue(Loadable.Loading())
            try {
                val response = GukhanWikiApi.actionApiService.parse(page = title, prop = "wikitext", section = section)
                val content = response.parse?.wikiText
                if(content?.wikiText == null) {
                    _page.postValue(Loadable.Error(java.lang.RuntimeException("Result text is null")))
                } else {
                    _page.postValue(Loadable.Loaded(
                        Page(
                            title = response.parse.title ?: title,
                            content = content.wikiText,
                            section = section,
                            revId = response.parse.revId,
                        )
                    ))
                }
            } catch(ex: IOException) {
                ex.printStackTrace()
                _page.postValue(Loadable.Error(ex))
            } catch(ex: HttpException) {
                _page.postValue(Loadable.Error(ex))
            }
        }
    }

    fun reviewEdit(page: Page) {
        viewModelScope.launch {
            val response = GukhanWikiApi.actionApiService.parse(
                text = page.content,
            )
            val text = response.parse?.text?.text
            if(text != null) {
                val newPage = page.copy(content = text)
//                _pageToReview.postValue(Loadable.Loaded(newPage))
                _page.postValue(Loadable.Loaded(newPage))
            }
        }
    }

    fun updatePage(page: Page, summary: String?) {
        viewModelScope.launch {
            _page.postValue(Loadable.Loading())
            try {
                val csrfToken = GukhanWikiApi.actionApiService.retrieveToken(type = "csrf").query.tokens["csrftoken"]
                val response = GukhanWikiApi.actionApiService.edit(
                    token = csrfToken,
                    title = page.title,
                    summary = summary,
                    section = page.section,
                    baseRevId = page.revId,
                    text = page.content,
                )
                if(response.edit?.result == "Success") _result.postValue(Loadable.Loaded(response))
                else _result.postValue(Loadable.Error(java.lang.RuntimeException(response.edit?.result)))
            } catch(ex: HttpException) {
                ex.printStackTrace()
                _result.postValue(Loadable.Error(ex))
            }
        }
    }

    data class Page(
        val title: String,
        val content: String,
        val section: String?,
        val revId: Int?,
    ): Serializable

}