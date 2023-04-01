package io.github.lee0701.gukhanwiki.android.api.action

import java.io.Serializable

data class Page(
    val title: String,
    val section: String?,
    val revId: Int?,
): Serializable
