package io.github.lee0701.gukhanwiki.android.view.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lee0701.gukhanwiki.android.Loadable
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import retrofit2.HttpException
import java.io.IOException

class ViewPageViewModel: ViewModel() {

    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private val _content = MutableLiveData<Loadable<String?>>()
    val content: LiveData<Loadable<String?>> = _content

    fun loadPage(path: String) {
        viewModelScope.launch {
            _content.postValue(Loadable.Loading())
            try {
                val response = GukhanWikiApi.actionApiService.parse(page = path)
                val title = response.parse?.title
                val content = response.parse?.text?.text
                if(response.error?.get("code") == "pagecannotexist") {
                    val clientContent = GukhanWikiApi.clientService.index(title = path)
                    val doc = Jsoup.parse(clientContent)
                    val mainContent = doc.select("#content").first()
                    if(mainContent != null) {
                        doc.body().remove()
                        doc.body().appendChild(mainContent)
                    }
                    _title.postValue(path)
                    _content.postValue(Loadable.Loaded(doc.html()))
                } else {
                    if(content == null) {
                        _content.postValue(Loadable.Error(RuntimeException("Result text is null")))
                    } else {
                        _title.postValue(title ?: path)
                        _content.postValue(Loadable.Loaded(content))
                    }
                }
            } catch(ex: IOException) {
                _content.postValue(Loadable.Error(ex))
            } catch(ex: HttpException) {
                _content.postValue(Loadable.Error(ex))
            }
        }
    }

    fun restorePage(content: String) {
        _content.postValue(Loadable.Loaded(content))
    }

}