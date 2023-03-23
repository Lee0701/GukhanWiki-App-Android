package io.github.lee0701.gukhanwiki.android.view.search

import android.graphics.drawable.Drawable
import android.text.Spanned

data class SearchResultItem(
    val id: Int,
    val title: String,
    val excerpt: Spanned,
    val thumbnail: Drawable?,
)
