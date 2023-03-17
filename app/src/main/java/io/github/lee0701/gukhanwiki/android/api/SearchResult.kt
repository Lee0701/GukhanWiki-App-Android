package io.github.lee0701.gukhanwiki.android.api

import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Html
import androidx.core.text.HtmlCompat
import com.google.gson.annotations.SerializedName
import io.github.lee0701.gukhanwiki.android.view.SearchAutocompleteItem
import io.github.lee0701.gukhanwiki.android.view.SearchResultItem
import org.jsoup.Jsoup

data class SearchResult(
    val id: Int,
    val key: String,
    val title: String,
    val excerpt: String,
    @SerializedName("matched_title") val matchedTitle: String?,
    val description: String?,
    val thumbnail: Thumbnail?,
) {
    fun toAutocompleteItem(): SearchAutocompleteItem {
        return SearchAutocompleteItem(id = id, title = title)
    }

    fun toSearchResultItem(): SearchResultItem {
        val thumbnail = thumbnail?.let {
            val stream = ImageApi.getImageAsStream(it.url)
            Drawable.createFromStream(stream, "thumbnail")
        }
        val doc = Jsoup.parse(this.excerpt)
        val elements = doc.getElementsByTag("span")
        elements.tagName("b").removeClass("searchmatch")
        val excerpt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            Html.fromHtml(doc.body().html(), HtmlCompat.FROM_HTML_MODE_COMPACT)
        else
            Html.fromHtml(doc.body().html())
        return SearchResultItem(
            id = id,
            title = title,
            excerpt = excerpt,
            thumbnail = thumbnail,
        )
    }
}

data class SearchResults(
    val pages: List<SearchResult>,
)
