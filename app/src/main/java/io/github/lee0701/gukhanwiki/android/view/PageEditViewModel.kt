package io.github.lee0701.gukhanwiki.android.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import io.github.lee0701.gukhanwiki.android.api.Page
import io.github.lee0701.gukhanwiki.android.api.UpdatePageBody
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class PageEditViewModel: ViewModel() {

    private val _page = MutableLiveData<Page>()
    val page: LiveData<Page> = _page

    private val _result = MutableLiveData<EditResult>()
    val result: LiveData<EditResult> = _result

    fun loadPageSource(title: String) {
        viewModelScope.launch {
            try {
                val response = GukhanWikiApi.restApiService.getPageSource(title)
                _page.postValue(response)
            } catch(ex: IOException) {
                ex.printStackTrace()
            } catch(ex: HttpException) {
                _page.postValue(Page(title = title))
            }
        }
    }

    fun updatePage(title: String, content: String) {
        viewModelScope.launch {
            try {
                val csrfToken = GukhanWikiApi.actionApiService.retrieveToken(type = "csrf").query.tokens["csrftoken"]
                val result = GukhanWikiApi.restApiService.updatePage(
                    title = title,
                    body = UpdatePageBody(
                        token = csrfToken,
                        source = content,
                        comment = "",
                        latest = page.value?.latest,
                    ),
                )
                _result.postValue(EditResult.Success(result, null))
            } catch(ex: HttpException) {
                ex.printStackTrace()
                _result.postValue(EditResult.Error(ex, ex.message ?: ""))
            }
        }
    }

    sealed interface EditResult {
        val message: String?
        data class Success(
            val page: Page,
            override val message: String?,
        ): EditResult
        data class Error(
            val exception: Exception,
            override val message: String?,
        ): EditResult
    }
}