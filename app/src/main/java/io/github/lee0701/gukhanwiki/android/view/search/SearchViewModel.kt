package io.github.lee0701.gukhanwiki.android.view.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import io.github.lee0701.gukhanwiki.android.history.SearchHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SearchViewModel: ViewModel() {
    private var autocompleteJob: Job? = null

    private val _autocompleteResult = MutableLiveData<List<SearchAutocompleteItem>>()
    val autocompleteResult: LiveData<List<SearchAutocompleteItem>> = _autocompleteResult
    fun autocompleteSearch(text: String) {
        autocompleteJob?.cancel()
        autocompleteJob = viewModelScope.launch(Dispatchers.IO) {
            val result = GukhanWikiApi.restApiService.autocompletePageTitle(text, 10)
            // 검색결과 맨 위에 보여줄 '입력 내용 그대로' 결과
            val input = listOf(SearchAutocompleteItem(-1, text, SearchAutocompleteItem.Action.NEW))
            // 검색결과 중 제목이 일치하는 결과 찾기
            val match = listOfNotNull(result.pages.find { it.title == text }).map { it.toAutocompleteItem().copy(action = SearchAutocompleteItem.Action.GOTO) }
            // match에 우선순위를 두어 검색결과에 삽입
            val top = match.ifEmpty { input }
            val pages = result.pages.map { it.toAutocompleteItem() }.filter { top.isEmpty() || it.title != top.first().title }
            _autocompleteResult.postValue(top + pages)
        }
    }

    fun displayHistory(history: SearchHistory) {
        val result = history.entries
            .sortedByDescending { it.date }
            .map { SearchAutocompleteItem(0, it.title) }
        _autocompleteResult.postValue(result)
    }

    fun clearAutocomplete() {
        autocompleteJob?.cancel()
        _autocompleteResult.postValue(emptyList())
    }

}