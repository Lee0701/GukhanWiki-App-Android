package io.github.lee0701.gukhanwiki.android.api

import com.google.gson.annotations.SerializedName
import io.github.lee0701.gukhanwiki.android.view.SearchAutocompleteItem

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
}

data class SearchResults(
    val pages: List<SearchResult>,
)
