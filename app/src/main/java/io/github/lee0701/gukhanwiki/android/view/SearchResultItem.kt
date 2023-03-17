package io.github.lee0701.gukhanwiki.android.view

import android.graphics.drawable.Drawable

data class SearchResultItem(
    val id: Int,
    val title: String,
    val excerpt: String,
    val thumbnail: Drawable?,
)
