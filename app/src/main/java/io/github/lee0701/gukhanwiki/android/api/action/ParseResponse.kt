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
    @SerializedName("categories") val categories: List<ParseResultCategory>? = null,
    @SerializedName("categorieshtml") val categoriesHtml: ParseResultCategoriesHtml? = null,
)

data class ParseResultWikiText(
    @SerializedName("*") val wikiText: String? = null,
)

data class ParseResultText(
    @SerializedName("*") val text: String? = null,
)

data class ParseResultCategory(
    @SerializedName("sortkey") val sortKey: String? = null,
    @SerializedName("hidden") val hidden: String? = null,
    @SerializedName("*") val name: String? = null,
)

data class ParseResultCategoriesHtml(
    @SerializedName("*") val html: String? = null,
)