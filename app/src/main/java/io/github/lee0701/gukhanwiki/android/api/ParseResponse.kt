package io.github.lee0701.gukhanwiki.android.api

import com.google.gson.annotations.SerializedName

data class ParseResponse(
    val parse: ParseResult? = null,
)

data class ParseResult(
    val title: String? = null,
    @SerializedName("pageid") val pageId: Int? = null,
    @SerializedName("revid") val revId: Int? = null,
    val text: ParseResultText? = null,
)

data class ParseResultText(
    @SerializedName("*") val text: String? = null,
)