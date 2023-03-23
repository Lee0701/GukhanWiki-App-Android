package io.github.lee0701.gukhanwiki.android.api

import retrofit2.http.GET
import retrofit2.http.Query

interface GukhanWikiClientService {
    @GET("/index.php")
    suspend fun index(
        @Query("action") action: String? = "",
        @Query("title") title: String? = "",
        @Query("section") section: String? = "",
    ): String
}