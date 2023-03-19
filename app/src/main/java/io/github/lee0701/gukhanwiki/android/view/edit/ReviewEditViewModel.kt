package io.github.lee0701.gukhanwiki.android.view.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lee0701.gukhanwiki.android.Loadable
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import kotlinx.coroutines.launch
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
            val response = GukhanWikiApi.actionApiService.parse(
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
            _page.postValue(Loadable.Loading())
            try {
                val csrfToken = GukhanWikiApi.actionApiService.retrieveToken(type = "csrf").query.tokens["csrftoken"]
                val response = GukhanWikiApi.actionApiService.edit(
                    token = csrfToken,
                    title = page.title,
                    summary = summary,
                    section = page.section,
                    baseRevId = page.revId,
                    text = page.wikiText,
                )
                val result = response.edit
                if(result  != null && result.result == "Success") _result.postValue(Loadable.Loaded(
                    page.copy(
                        revId = result.newRevId,
                        wikiText = page.wikiText
                    )
                ))
                else _result.postValue(Loadable.Error(java.lang.RuntimeException(response.edit?.result)))
            } catch(ex: HttpException) {
                ex.printStackTrace()
                _result.postValue(Loadable.Error(ex))
            }
        }
    }

}