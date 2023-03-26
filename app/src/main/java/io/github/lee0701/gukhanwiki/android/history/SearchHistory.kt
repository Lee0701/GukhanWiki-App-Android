package io.github.lee0701.gukhanwiki.android.history

import java.util.Date

data class SearchHistory(
    val entries: List<Entry>
) {
    data class Entry(
        val title: String,
        val date: Date,
    )
}