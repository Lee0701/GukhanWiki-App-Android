package io.github.lee0701.gukhanwiki.android.api

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface GukhanWikiClientService {
    @GET("/index.php")
    suspend fun index(
        @Query("action") action: String? = "",
        @Query("title") title: String? = "",
    ): String

    @GET("/index.php")
    suspend fun index(
        @Query("action") action: String? = "",
        @Query("title") title: String? = "",
        @QueryMap query: Map<String, String> = mapOf(),
    ): String

}