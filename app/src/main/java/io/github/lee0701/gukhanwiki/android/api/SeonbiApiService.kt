package io.github.lee0701.gukhanwiki.android.api

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface SeonbiApiService {

    @POST("/seonbi/")
    @Headers("Content-Type: application/json")
    suspend fun seonbi(
        @Body body: Config,
    ): Result

    data class Config(
        val content: String,
        val contentType: String = "text/html",
        val quote: String? = "CurvedQuotes",
        val cite: String? = "AngleQuotes",
        val arrow: ArrowConfig? = ArrowConfig(bidirArrow = true, doubleArrow = true),
        val ellipsis: Boolean? = true,
        val emDash: Boolean? = true,
        val stop: String? = "Vertical",
    )

    data class ArrowConfig(
        val bidirArrow: Boolean = true,
        val doubleArrow: Boolean = true,
    )

    data class Result(
        val content: String,
        val contentType: String = "text/html",
        val resultHtml: String? = null,
        val success: Boolean? = null,
        val warnings: List<String>? = null,
    )
}