package io.github.lee0701.gukhanwiki.android.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface GukhanWikiService {
    @GET("page/{title}/html")
    suspend fun getPageHtml(@Path("title") title: String): String

    @GET("search/title")
    suspend fun autocompletePageTitle(@Query("q") q: String, @Query("limit") limit: Int): SearchResults

    @GET("search/page")
    suspend fun searchPage(@Query("q") q: String, @Query("limit") limit: Int): SearchResults

    @GET
    fun download(@Url url: String): Call<ResponseBody>
}