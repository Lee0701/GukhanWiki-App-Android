package io.github.lee0701.gukhanwiki.android.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class PageViewModel: ViewModel() {

    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private val _content = MutableLiveData<PageContent>()
    val content: LiveData<PageContent> = _content

    fun loadPage(title: String) {
        viewModelScope.launch {
            _content.postValue(PageContent.Loading)
            try {
                val result = GukhanWikiApi.service.getPageHtml(title)
                _title.postValue(title)
                _content.postValue(PageContent.Loaded(result))
            } catch(e: IOException) {
                _content.postValue(PageContent.Error(e.message))
            } catch(e: HttpException) {
                _content.postValue(PageContent.Error(e.message))
            }
        }
    }

}