package io.github.lee0701.gukhanwiki.android.view.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lee0701.gukhanwiki.android.Result
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import io.github.lee0701.gukhanwiki.android.api.action.Page
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup

class ConfirmEditViewModel: ViewModel() {

    private val _html = MutableLiveData<Result<String>>()
    val html: LiveData<Result<String>> = _html

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _html.postValue(Result.Error(throwable))
        throwable.printStackTrace()
    }

    fun showConfirmation(page: Page, content: String, summary: String) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _html.postValue(Result.Loading())
            val response = GukhanWikiApi.clientService.index(
                action = "edit",
                title = page.title,
                query = mapOf("section" to page.section.orEmpty()),
            )

            val doc = Jsoup.parse(response)
            val editForm = doc.select("#editform").first()

            val textBox = editForm?.select("#editform #wpTextbox1")?.first()
            textBox?.text(content)
            textBox?.attr("style", "height: 5em;")
            val summaryBox = editForm?.select("#editform #wpSummary")?.first()
            summaryBox?.attr("value", summary)

            if(editForm != null) {
                doc.body().remove()
                doc.body().appendChild(editForm)
            }
            _html.postValue(Result.Loaded(doc.html()))
        }
    }

}