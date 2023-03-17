package io.github.lee0701.gukhanwiki.android.api

import retrofit2.Retrofit
import java.io.IOException
import java.io.InputStream
import java.net.URL

object ImageApi {
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(URL(GukhanWikiApi.PROTOCOL, GukhanWikiApi.HOST, GukhanWikiApi.DOC_PATH))
            .build()
    }
    val service = retrofit.create(ImageService::class.java)

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
}