package io.github.lee0701.gukhanwiki.android.api.action

import com.google.gson.annotations.SerializedName

data class ParseResponse(
    @SerializedName("error") val error: Map<String, String>? = null,
    val parse: ParseResult? = null,
)

data class ParseResult(
    val title: String? = null,
    @SerializedName("pageid") val pageId: Int? = null,
    @SerializedName("revid") val revId: Int? = null,
    @SerializedName("text") val text: ParseResultText? = null,
    @SerializedName("wikitext") val wikiText: ParseResultWikiText? = null,
)

data class ParseResultWikiText(
    @SerializedName("*") val wikiText: String? = null,
)

data class ParseResultText(
    @SerializedName("*") val text: String? = null,
)