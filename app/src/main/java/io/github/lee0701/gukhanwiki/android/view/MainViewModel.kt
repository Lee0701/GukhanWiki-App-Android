package io.github.lee0701.gukhanwiki.android.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job

class MainViewModel: ViewModel() {
    private var autocompleteJob: Job? = null

    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private val _autocompleteResult = MutableLiveData<List<SearchAutocompleteItem>>()
    val autocompleteResult: LiveData<List<SearchAutocompleteItem>> = _autocompleteResult

    fun updateTitle(title: String) {
        _title.postValue(title)
    }

}