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

class SearchResultViewModel: ViewModel() {

    private val _query = MutableLiveData<String>()
    val query: LiveData<String> = _query

    private val _searchResult = MutableLiveData<Loadable<List<SearchResultItem>>>()
    val searchResult: LiveData<Loadable<List<SearchResultItem>>> = _searchResult

    fun search(query: String) {
        viewModelScope.launch {
            _searchResult.postValue(Loadable.Loading())
            try {
                val result = GukhanWikiApi.service.searchPage(query, 10)
                val list = result.pages.map { it.toSearchResultItem() }
                _query.postValue(query)
                _searchResult.postValue(Loadable.Loaded(list))
            } catch(e: IOException) {
                _searchResult.postValue(Loadable.Error(e.message))
            } catch(e: HttpException) {
                _searchResult.postValue(Loadable.Error(e.message))
            }
        }
    }
}