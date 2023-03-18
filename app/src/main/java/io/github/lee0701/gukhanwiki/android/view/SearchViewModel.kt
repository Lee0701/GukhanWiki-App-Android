package io.github.lee0701.gukhanwiki.android.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchViewModel: ViewModel() {
    private var autocompleteJob: Job? = null

    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private val _autocompleteResult = MutableLiveData<List<SearchAutocompleteItem>>()
    val autocompleteResult: LiveData<List<SearchAutocompleteItem>> = _autocompleteResult
    fun autocompleteSearch(text: String) {
        autocompleteJob?.cancel()
        autocompleteJob = viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val result = GukhanWikiApi.restApiService.autocompletePageTitle(text, 10)
                val match = listOfNotNull(result.pages.find { it.title == text }).map { it.toAutocompleteItem().copy(goto = true) }
                val pages = result.pages.map { it.toAutocompleteItem() }.filter { match.isEmpty() || it.title != match.first().title }
                _autocompleteResult.postValue(match + pages)
            }
        }
    }

    fun clearAutocomplete() {
        autocompleteJob?.cancel()
        _autocompleteResult.postValue(emptyList())
    }

    fun autocompleteSelected(text: String) {
        _title.postValue(text)
    }
}