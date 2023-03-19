package io.github.lee0701.gukhanwiki.android.view.edit

import java.io.Serializable


data class Page(
    val title: String,
    val wikiText: String,
    val section: String?,
    val revId: Int?,
): Serializable
