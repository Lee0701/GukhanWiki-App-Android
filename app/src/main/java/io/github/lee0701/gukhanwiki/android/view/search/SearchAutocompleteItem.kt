package io.github.lee0701.gukhanwiki.android.view.search

data class SearchAutocompleteItem(
    val id: Int,
    val title: String,
    val goto: Boolean = false,
)
