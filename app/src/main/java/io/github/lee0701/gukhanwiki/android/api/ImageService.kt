package io.github.lee0701.gukhanwiki.android.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface ImageService {
    @GET
    fun download(@Url url: String): Call<ResponseBody>
}