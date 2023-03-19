package io.github.lee0701.gukhanwiki.android.api.action

import com.google.gson.annotations.SerializedName

data class EditResponse(
    @SerializedName("error") val error: Map<String, String>? = null,
    val edit: EditResult? = null,
)

data class EditResult(
    val result: String,
    @SerializedName("pageid") val pageId: String,
    val title: String,
    @SerializedName("oldrevid") val oldRevId: Int? = null,
    @SerializedName("newrevid") val newRevId: Int? = null,
    @SerializedName("newtimestamp") val newTimeStamp: String? = null,
)