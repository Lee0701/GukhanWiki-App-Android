package io.github.lee0701.gukhanwiki.android.api

import io.github.lee0701.gukhanwiki.android.api.action.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface GukhanWikiActionApiService {

    @GET("/api.php")
    suspend fun info(
        @Query("action") action: String = "query",
        @Query("format") format: String = "json",
        @Query("prop") prop: String = "info",
        @Query("titles") titles: String? = null,
        @Query("inprop") inprop: String? = null,
    ): InfoResponse

    @GET("/api.php")
    suspend fun parse(
        @Query("action") action: String = "parse",
        @Query("format") format: String = "json",
        @Query("page") page: String? = null,
        @Query("oldid") oldid: String? = null,
        @QueryMap query: Map<String, String> = mapOf(),
    ): ParseResponse

    @FormUrlEncoded
    @POST("/api.php")
    suspend fun parsePost(
        @Field("action") action: String = "parse",
        @Field("format") format: String = "json",
        @Field("text") text: String? = null,
        @Field("page") page: String? = null,
        @Field("prop") prop: String? = null,
        @Field("section") section: String? = null,
        @FieldMap field: Map<String, String> = mapOf(),
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
        @Query("minor") minor: Boolean? = null,
        @Field("token") token: String? = null,
        @Field("text") text: String,
    ): EditResponse

    @Multipart
    @POST("/api.php")
    suspend fun editMultipart(
        @Part action: MultipartBody.Part = MultipartBody.Part.createFormData("action", "edit"),
        @Part format: MultipartBody.Part = MultipartBody.Part.createFormData("format", "json"),
        @Part title: MultipartBody.Part? = null,
        @Part section: MultipartBody.Part? = null,
        @Part summary: MultipartBody.Part? = null,
        @Part baseRevId: MultipartBody.Part? = null,
        @Part minor: MultipartBody.Part? = null,
        @Part token: MultipartBody.Part? = null,
        @Part text: MultipartBody.Part,
    ): EditResponse

    @GET("/api.php")
    suspend fun random(
        @Query("action") action: String = "query",
        @Query("format") format: String = "json",
        @Query("list") list: String = "random",
        @Query("rnnamespace") rnNamespaces: String?,
        @Query("rnfilterredir") rnFilteredDir: String? = null,
        @Query("rnlimit") rnLimit: Int? = null,
    ): RandomResponse

    @GET("/api.php")
    suspend fun categoryMembers(
        @Query("action") action: String = "query",
        @Query("format") format: String = "json",
        @Query("list") list: String = "categorymembers",
        @Query("cmcontinue") cmContinue: String? = null,
        @Query("cmtitle") cmTitle: String? = null,
        @Query("cmprop") cmProp: String? = null,
        @Query("cmlimit") cmLimit: Int? = null,
    ): CategoryMembersResponse

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

    @FormUrlEncoded
    @POST("/api.php")
    suspend fun logout(
        @Query("action") action: String = "logout",
        @Query("format") format: String = "json",
        @Field("token") token: String = "",
    ): LogoutResponse
}