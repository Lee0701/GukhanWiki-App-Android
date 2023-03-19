package io.github.lee0701.gukhanwiki.android.api

import io.github.lee0701.gukhanwiki.android.api.action.ClientLoginResponse
import io.github.lee0701.gukhanwiki.android.api.action.EditResponse
import io.github.lee0701.gukhanwiki.android.api.action.ParseResponse
import io.github.lee0701.gukhanwiki.android.api.action.TokenResponse
import retrofit2.http.*

interface GukhanWikiActionApiService {

    @GET("/api.php")
    suspend fun parse(
        @Query("action") action: String = "parse",
        @Query("format") format: String = "json",
        @Query("page") page: String? = null,
        @Query("prop") prop: String? = null,
        @Query("section") section: String? = null,
    ): ParseResponse

    @FormUrlEncoded
    @POST("/api.php")
    suspend fun edit(
        @Query("action") action: String = "edit",
        @Query("format") format: String = "json",
        @Query("title") title: String? = null,
        @Query("section") section: String? = null,
        @Query("summary") summary: String? = null,
        @Query("baserevid") baseRevId: Int? = null,
        @Field("token") token: String? = null,
        @Field("text") text: String,
    ): EditResponse

    @GET("/api.php")
    suspend fun retrieveToken(
        @Query("action") action: String = "query",
        @Query("format") format: String = "json",
        @Query("meta") meta: String = "tokens",
        @Query("type") type: String,
    ): TokenResponse

    @FormUrlEncoded
    @POST("/api.php")
    suspend fun clientLogin(
        @Query("action") action: String = "clientlogin",
        @Query("format") format: String = "json",
        @Query("loginreturnurl") loginReturnUrl: String = "http://example.com/",
        @Field("logintoken") loginToken: String = "",
        @Field("username") username: String = "",
        @Field("password") password: String = "",
    ): ClientLoginResponse

    @POST("/api.php")
    suspend fun logout(
        @Query("action") action: String = "logout",
        @Query("token") token: String = "",
    )
}