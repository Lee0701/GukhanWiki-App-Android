package io.github.lee0701.gukhanwiki.android.api

data class TokenResponse(
    val batchComplete: String = "",
    val query: Query,
) {
    data class Query(
        val tokens: Map<String, String>,
    )
}
