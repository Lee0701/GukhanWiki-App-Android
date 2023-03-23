package io.github.lee0701.gukhanwiki.android.view

import android.webkit.CookieManager
import java.net.CookieHandler
import java.net.URI

class WebviewCookieHandler: CookieHandler() {

    private val webviewCookieManager = CookieManager.getInstance()

    override fun get(
        uri: URI?,
        requestHeaders: MutableMap<String, MutableList<String>>?
    ): MutableMap<String, MutableList<String>> {
        val cookieValue = this.webviewCookieManager.getCookie(uri.toString()).orEmpty()
        val cookies = mutableMapOf("Cookie" to mutableListOf(cookieValue))
        return cookies
    }

    override fun put(uri: URI?, responseHeaders: MutableMap<String, MutableList<String>>?) {
        responseHeaders?.keys?.forEach { header ->
            if(header.equals("Set-Cookie", true) || header.equals("Set-Cookie2", true)) {
                val values = responseHeaders[header] ?: emptyList()
                for(value in values) {
                    webviewCookieManager.setCookie(uri.toString(), value)
                }
            }
        }
    }
}