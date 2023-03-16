package io.github.lee0701.gukhanwiki.android.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {
    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    fun updateTitle(title: String) {
        _title.postValue(title)
    }
}