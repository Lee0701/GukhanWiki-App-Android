package io.github.lee0701.gukhanwiki.android.api.action

import com.google.gson.annotations.SerializedName

data class InfoResponse(
    @SerializedName("error") val error: Map<String, String>? = null,
    val query: InfoQueryResult? = null,
)

data class InfoQueryResult(
    @SerializedName("pages") val pages: Map<String, InfoPageResult> = mapOf(),
)

data class InfoPageResult(
    val title: String? = null,
    @SerializedName("pageid") val pageId: Int? = null,
    @SerializedName("text") val text: ParseResultText? = null,
    @SerializedName("associatedpage") val associatedPage: String? = null,
    @SerializedName("displaytitle") val displayTitle: String? = null,
)