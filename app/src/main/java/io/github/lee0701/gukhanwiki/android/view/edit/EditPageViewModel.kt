package io.github.lee0701.gukhanwiki.android.view.edit

import androidx.lifecycle.*
import io.github.lee0701.gukhanwiki.android.Result
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import io.github.lee0701.gukhanwiki.android.api.action.Page
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class EditPageViewModel: ViewModel() {

    private val _page = MutableLiveData<Result<Page>>()
    val page: LiveData<Result<Page>> = _page

    private val _content = MutableLiveData<Result<String>>()
    val content: LiveData<Result<String>> = _content

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _page.postValue(Result.Error(throwable))
        throwable.printStackTrace()
    }

    fun update(content: String) {
        _content.postValue(Result.Loaded(content))
    }

    fun loadPageSource(title: String, section: String?) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _page.postValue(Result.Loading())
            try {
                val query = mutableMapOf<String, String>()
                query += "prop" to "wikitext"
                if(section != null) query += "section" to section
                val response = GukhanWikiApi.actionApiService.parse(page = title, query = query)
                val content = response.parse?.wikiText
                if(content?.wikiText == null) {
                    _content.postValue(Result.Loaded(""))
                    _page.postValue(Result.Loaded(
                        Page(
                            title = title,
                            section = section,
                            revId = null,
                        )
                    ))
                } else {
                    _content.postValue(Result.Loaded(content.wikiText))
                    _page.postValue(Result.Loaded(
                        Page(
                            title = response.parse.title ?: title,
                            section = section,
                            revId = response.parse.revId,
                        )
                    ))
                }
            } catch(ex: IOException) {
                ex.printStackTrace()
                _page.postValue(Result.Error(ex))
            } catch(ex: HttpException) {
                _page.postValue(Result.Error(ex))
            }
        }
    }

}