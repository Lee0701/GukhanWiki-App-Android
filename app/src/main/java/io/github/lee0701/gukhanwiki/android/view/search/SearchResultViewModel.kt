package io.github.lee0701.gukhanwiki.android.view.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lee0701.gukhanwiki.android.Result
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class SearchResultViewModel: ViewModel() {

    private val _query = MutableLiveData<String>()
    val query: LiveData<String> = _query

    private val _searchResult = MutableLiveData<Result<List<SearchResultItem>>>()
    val searchResult: LiveData<Result<List<SearchResultItem>>> = _searchResult

    fun search(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _searchResult.postValue(Result.Loading())
            try {
                val result = GukhanWikiApi.restApiService.searchPage(query, 10)
                val list = result.pages.map { it.toSearchResultItem() }
                _query.postValue(query)
                _searchResult.postValue(Result.Loaded(list))
            } catch(ex: IOException) {
                _searchResult.postValue(Result.Error(ex))
            } catch(ex: HttpException) {
                _searchResult.postValue(Result.Error(ex))
            }
        }
    }
}