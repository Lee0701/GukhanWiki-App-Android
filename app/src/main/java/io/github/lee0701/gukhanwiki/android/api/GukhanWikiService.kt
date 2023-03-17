package io.github.lee0701.gukhanwiki.android.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GukhanWikiService {
    @GET("page/{title}/html")
    suspend fun getPageHtml(@Path("title") title: String): String

    @GET("search/title")
    suspend fun autocompletePageTitle(@Query("q") q: String, @Query("limit") limit: Int): SearchResults

    @GET("search/page")
    suspend fun searchPage(@Query("q") q: String, @Query("limit") limit: Int): SearchResults

}