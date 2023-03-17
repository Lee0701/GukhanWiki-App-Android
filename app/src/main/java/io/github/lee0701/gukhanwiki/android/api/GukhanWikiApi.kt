package io.github.lee0701.gukhanwiki.android.api

import android.os.Build
import com.google.gson.GsonBuilder
import io.github.lee0701.gukhanwiki.android.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object GukhanWikiApi {
    const val PROTOCOL = BuildConfig.API_PROTOCOL
    const val HOST = BuildConfig.API_HOST
    const val BASE_PATH = BuildConfig.API_BASE_PATH
    const val DOC_PATH = BuildConfig.DOC_PATH

    val API_URL = URL(PROTOCOL, HOST, BASE_PATH)
    val DOC_URL = URL(PROTOCOL, HOST, DOC_PATH)

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(API_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()
    }
    val service: GukhanWikiService by lazy {
        retrofit.create(GukhanWikiService::class.java)
    }

    fun getImageAsStream(url: String): InputStream? {
        val call = service.download(url)
        try {
            val response = call.execute()
            if(response.isSuccessful) return response.body()?.byteStream()
        } catch(ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return null
    }

    fun decodeUriComponent(path: String): String {
        val charset = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            StandardCharsets.UTF_8.name()
        else
            "UTF-8"
        val title = URLDecoder.decode(path, charset)
        return title
    }
}