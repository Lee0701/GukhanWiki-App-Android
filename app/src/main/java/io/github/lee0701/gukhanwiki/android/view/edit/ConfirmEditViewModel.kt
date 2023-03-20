package io.github.lee0701.gukhanwiki.android.view.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.lee0701.gukhanwiki.android.Loadable
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.DataNode
import org.jsoup.nodes.Element

class ConfirmEditViewModel: ViewModel() {

    private val _page = MutableLiveData<Loadable<Page>>()
    val page: LiveData<Loadable<Page>> = _page

    private val _html = MutableLiveData<Loadable<String>>()
    val html: LiveData<Loadable<String>> = _html

    fun showConfirmation(page: Page, summary: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _html.postValue(Loadable.Loading())
            val content = GukhanWikiApi.clientService.index(
                action = "edit",
                title = page.title,
                section = page.section.orEmpty()
            )

            val doc = Jsoup.parse(content)
            val script = Element("script").appendChild(DataNode("""
                function onSubmit() {
                    if(typeof Android !== "undefined" && Android !== null) {
                        Android.onSubmit();
                    }
                }
            """.trimIndent()))
            doc.head().appendChild(script)

            val textBox = doc.select("#editform #wpTextbox1").first()
            textBox?.text(page.wikiText)
            val summaryBox = doc.select("#editform #wpSummary").first()
            summaryBox?.attr("value", summary)
            val editForm = doc.select("#editform")
            editForm.attr("onsubmit", "onSubmit()")

            _html.postValue(Loadable.Loaded(doc.html()))
        }
    }

}