package io.github.lee0701.gukhanwiki.android.api

import io.github.lee0701.gukhanwiki.android.api.action.ClientLoginResponse
import io.github.lee0701.gukhanwiki.android.api.action.ParseResponse
import io.github.lee0701.gukhanwiki.android.api.action.TokenResponse
import retrofit2.http.*

interface GukhanWikiActionApiService {

    @GET("/api.php")
    suspend fun parse(
        @Query("action") action: String = "parse",
        @Query("format") format: String = "json",
        @Query("page") page: String? = null,
    ): ParseResponse

    @GET("/api.php")
    suspend fun retrieveToken(
        @Query("action") action: String = "query",
        @Query("format") format: String = "json",
        @Query("meta") meta: String = "tokens",
        @Query("type") type: String,
    ): TokenResponse

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
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