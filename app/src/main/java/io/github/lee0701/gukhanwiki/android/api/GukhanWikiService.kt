package io.github.lee0701.gukhanwiki.android.api

import retrofit2.http.GET
import retrofit2.http.Path

interface GukhanWikiService {
    @GET("page/{title}/html")
    suspend fun getHtml(@Path("title") title: String): String
}