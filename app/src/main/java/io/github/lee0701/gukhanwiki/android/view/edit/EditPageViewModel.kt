package io.github.lee0701.gukhanwiki.android.view.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lee0701.gukhanwiki.android.Loadable
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class EditPageViewModel: ViewModel() {

    private val _page = MutableLiveData<Loadable<Page>>()
    val page: LiveData<Loadable<Page>> = _page

    fun updatePageSource(text: String) {
        val page = page.value
        if(page is Loadable.Loaded) {
            _page.postValue(Loadable.Loaded(data = page.data.copy(wikiText = text)))
        }
    }

    fun loadPageSource(title: String, section: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            _page.postValue(Loadable.Loading())
            try {
                val response = GukhanWikiApi.actionApiService.parse(page = title, prop = "wikitext", section = section)
                val content = response.parse?.wikiText
                if(content?.wikiText == null) {
                    _page.postValue(Loadable.Error(RuntimeException("Result text is null")))
                } else {
                    _page.postValue(Loadable.Loaded(
                        Page(
                            title = response.parse.title ?: title,
                            wikiText = content.wikiText,
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

}