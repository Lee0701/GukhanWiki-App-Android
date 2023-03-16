package io.github.lee0701.gukhanwiki.android.api

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object GukhanWikiApi {
    const val HOST = "wiki.xn--9cs231j0ji.xn--p8s937b.net"
    const val BASE_URL = "https://$HOST/"
    const val BASE_PATH = "rest.php/v1/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("$BASE_URL$BASE_PATH")
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }
    val service: GukhanWikiService by lazy {
        retrofit.create(GukhanWikiService::class.java)
    }
}