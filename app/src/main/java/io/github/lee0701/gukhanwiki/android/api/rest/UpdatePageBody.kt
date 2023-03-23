package io.github.lee0701.gukhanwiki.android.api.rest

import com.google.gson.annotations.SerializedName

data class UpdatePageBody(
    @SerializedName("source") val source: String,
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("latest") val latest: Latest? = null,
    @SerializedName("content_model") val contentModel: String? = null,
    @SerializedName("token") val token: String? = null,
)

data class Latest(
    val id: Int,
)