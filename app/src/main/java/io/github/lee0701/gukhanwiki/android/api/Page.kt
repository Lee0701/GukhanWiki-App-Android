package io.github.lee0701.gukhanwiki.android.api

import com.google.gson.annotations.SerializedName

data class Page(
    val id: Int,
    val key: String,
    val title: String,
    @SerializedName("content_model") val contentModel: String,
    val latest: Latest,
    val license: Map<String, String>,
    @SerializedName("html_url") val htmlUrl: String?,
    val html: String?,
    val source: String?,
)