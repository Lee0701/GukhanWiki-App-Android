package io.github.lee0701.gukhanwiki.android.api

import com.google.gson.annotations.SerializedName

data class ParseResponse(
    val parse: ParseResult,
)

data class ParseResult(
    val title: String,
    @SerializedName("pageid") val pageId: Int,
    @SerializedName("revid") val revId: Int,
    val text: ParseResultText,
)

data class ParseResultText(
    @SerializedName("*") val text: String,
)