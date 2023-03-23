package io.github.lee0701.gukhanwiki.android.view.edit

import androidx.lifecycle.*
import io.github.lee0701.gukhanwiki.android.Loadable
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import io.github.lee0701.gukhanwiki.android.api.action.Page
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class EditPageViewModel: ViewModel() {

    private val _page = MutableLiveData<Loadable<Page>>()
    val page: LiveData<Loadable<Page>> = _page

    private val _content = MutableLiveData<Loadable<String>>()
    val content: LiveData<Loadable<String>> = _content

    fun update(content: String) {
        _content.postValue(Loadable.Loaded(content))
    }

    fun loadPageSource(title: String, section: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            _page.postValue(Loadable.Loading())
            try {
                val response = GukhanWikiApi.actionApiService.parse(page = title, prop = "wikitext", section = section)
                val content = response.parse?.wikiText
                if(content?.wikiText == null) {
                    _content.postValue(Loadable.Loaded(""))
                    _page.postValue(Loadable.Loaded(
                        Page(
                            title = title,
                            section = section,
                            revId = null,
                        )
                    ))
                } else {
                    _content.postValue(Loadable.Loaded(content.wikiText))
                    _page.postValue(Loadable.Loaded(
                        Page(
                            title = response.parse.title ?: title,
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