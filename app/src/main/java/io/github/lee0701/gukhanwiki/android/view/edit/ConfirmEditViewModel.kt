package io.github.lee0701.gukhanwiki.android.view.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lee0701.gukhanwiki.android.Loadable
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import io.github.lee0701.gukhanwiki.android.api.action.Page
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup

class ConfirmEditViewModel: ViewModel() {

    private val _page = MutableLiveData<Loadable<Page>>()
    val page: LiveData<Loadable<Page>> = _page

    private val _html = MutableLiveData<Loadable<String>>()
    val html: LiveData<Loadable<String>> = _html

    fun showConfirmation(page: Page, content: String, summary: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _html.postValue(Loadable.Loading())
            val response = GukhanWikiApi.clientService.index(
                action = "edit",
                title = page.title,
                section = page.section.orEmpty(),
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
            _html.postValue(Loadable.Loaded(doc.html()))
        }
    }

}