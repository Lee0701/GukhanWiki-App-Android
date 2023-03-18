package io.github.lee0701.gukhanwiki.android.api

import io.github.lee0701.gukhanwiki.android.api.action.Page
import io.github.lee0701.gukhanwiki.android.api.rest.SearchResults
import io.github.lee0701.gukhanwiki.android.api.rest.UpdatePageBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface GukhanWikiRESTApiService {

    @GET("page/{title}")
    suspend fun getPageSource(@Path("title") title: String): Page

    @GET("page/{title}/html")
    suspend fun getPageHtml(@Path("title") title: String): String

    @PUT("page/{title}")
    suspend fun updatePage(
        @Path("title") title: String,
        @Body body: UpdatePageBody,
    ): Page

    @GET("search/title")
    suspend fun autocompletePageTitle(@Query("q") q: String, @Query("limit") limit: Int): SearchResults

    @GET("search/page")
    suspend fun searchPage(@Query("q") q: String, @Query("limit") limit: Int): SearchResults

    @GET
    fun download(@Url url: String): Call<ResponseBody>
}