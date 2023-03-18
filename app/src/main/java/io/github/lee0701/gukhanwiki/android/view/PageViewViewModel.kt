package io.github.lee0701.gukhanwiki.android.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lee0701.gukhanwiki.android.Loadable
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class PageViewViewModel: ViewModel() {

    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private val _content = MutableLiveData<Loadable<String>>()
    val content: LiveData<Loadable<String>> = _content

    fun loadPage(title: String) {
        viewModelScope.launch {
            _content.postValue(Loadable.Loading())
            try {
                val result = GukhanWikiApi.restApiService.getPageHtml(title)
                _title.postValue(title)
                _content.postValue(Loadable.Loaded(result))
            } catch(ex: IOException) {
                _content.postValue(Loadable.Error(ex))
            } catch(ex: HttpException) {
                _content.postValue(Loadable.Error(ex))
            }
        }
    }

}