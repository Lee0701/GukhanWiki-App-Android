package io.github.lee0701.gukhanwiki.android.api

import com.google.gson.GsonBuilder
import io.github.lee0701.gukhanwiki.android.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.net.URL

object GukhanWikiApi {
    const val PROTOCOL = "https"
    const val HOST = BuildConfig.API_HOST
    const val BASE_PATH = "/rest.php/v1/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(URL(PROTOCOL, HOST, BASE_PATH))
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()
    }
    val service: GukhanWikiService by lazy {
        retrofit.create(GukhanWikiService::class.java)
    }
}