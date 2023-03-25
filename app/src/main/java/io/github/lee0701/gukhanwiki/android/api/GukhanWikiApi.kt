package io.github.lee0701.gukhanwiki.android.api

import android.os.Build
import io.github.lee0701.gukhanwiki.android.BuildConfig
import io.github.lee0701.gukhanwiki.android.view.WebviewCookieHandler
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object GukhanWikiApi {
    const val PROTOCOL = BuildConfig.API_PROTOCOL
    const val HOST = BuildConfig.API_HOST
    const val REST_BASE_PATH = BuildConfig.REST_BASE_PATH
    const val ACTION_BASE_PATH = BuildConfig.ACTION_BASE_PATH
    const val SEONBI_BASE_PATH = BuildConfig.SEONBI_BASE_PATH
    const val DOC_PATH = BuildConfig.DOC_PATH

    val REST_API_URL = URL(PROTOCOL, HOST, REST_BASE_PATH)
    val ACTION_API_URL = URL(PROTOCOL, HOST, ACTION_BASE_PATH)
    val SEONBI_API_URL = URL(PROTOCOL, HOST, ACTION_BASE_PATH)
    val CLIENT_URL = URL(PROTOCOL, HOST, "/")
    val DOC_URL = URL(PROTOCOL, HOST, DOC_PATH)

    const val MAIN_PAGE_TITLE = "國漢大百科:大門"

    private val okHttpClient = OkHttpClient.Builder()
        .protocols(listOf(Protocol.HTTP_1_1))
        .cookieJar(JavaNetCookieJar(WebviewCookieHandler()))
        .build()

    private val restRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(REST_API_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val restApiService: GukhanWikiRESTApiService by lazy {
        restRetrofit.create(GukhanWikiRESTApiService::class.java)
    }
    private val actionRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ACTION_API_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val actionApiService: GukhanWikiActionApiService by lazy {
        actionRetrofit.create(GukhanWikiActionApiService::class.java)
    }
    private val clientRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(CLIENT_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val clientService: GukhanWikiClientService by lazy {
        clientRetrofit.create(GukhanWikiClientService::class.java)
    }
    private val seonbiRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(SEONBI_API_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val seonbiService: SeonbiApiService by lazy {
        seonbiRetrofit.create(SeonbiApiService::class.java)
    }

    fun getImageAsStream(url: String): InputStream? {
        val call = restApiService.download(url)
        try {
            val response = call.execute()
            if(response.isSuccessful) return response.body()?.byteStream()
        } catch(ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return null
    }

    fun decodeUriComponent(str: String): String {
        val charset = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            StandardCharsets.UTF_8.name()
        else "UTF-8"
        return URLDecoder.decode(str, charset)
    }
    fun encodeUriComponent(str: String): String {
        val charset = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            StandardCharsets.UTF_8.name()
        else "UTF-8"
        return URLEncoder.encode(str, charset)
    }
}