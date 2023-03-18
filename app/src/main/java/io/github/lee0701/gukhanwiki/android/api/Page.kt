package io.github.lee0701.gukhanwiki.android.api

import com.google.gson.annotations.SerializedName

data class Page(
    val id: Int? = null,
    val key: String? = null,
    val title: String,
    @SerializedName("content_model") val contentModel: String? = null,
    val latest: Latest? = null,
    val license: Map<String, String>? = null,
    @SerializedName("html_url") val htmlUrl: String? = null,
    val html: String? = null,
    val source: String? = null,
)