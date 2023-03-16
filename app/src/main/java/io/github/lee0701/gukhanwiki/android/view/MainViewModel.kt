package io.github.lee0701.gukhanwiki.android.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private val _autocompleteResult = MutableLiveData<List<SearchAutocompleteItem>>()
    val autocompleteResult: LiveData<List<SearchAutocompleteItem>> = _autocompleteResult

    fun updateTitle(title: String) {
        _title.postValue(title)
    }

    fun autocompleteSearch(text: String) {
        viewModelScope.launch {
            val result = GukhanWikiApi.service.autocompletePageTitle(text, 10)
            _autocompleteResult.postValue(result.pages.map { it.toAutocompleteItem() })
        }
    }
}