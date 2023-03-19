package io.github.lee0701.gukhanwiki.android.view

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.annotation.IdRes
import androidx.annotation.RequiresApi
import io.github.lee0701.gukhanwiki.android.R
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import java.net.URL

class WebViewClient(
    private val listener: Listener,
): android.webkit.WebViewClient() {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        val uri = request?.url?.toString()
        if(uri != null) {
            val url = URL(uri.toString())
            return shouldOverrideUrlLoading(url)
        }
        return super.shouldOverrideUrlLoading(view, request)
    }
    override fun shouldOverrideUrlLoading(
        view: WebView?,
        uri: String?
    ): Boolean {
        if(uri != null) {
            val url = URL(uri)
            return shouldOverrideUrlLoading(url)
        }
        return super.shouldOverrideUrlLoading(view, uri.toString())
    }

    private fun shouldOverrideUrlLoading(url: URL): Boolean {
        if(isInternalLink(url)) {
            onInternalLinkClicked(url)
            return true
        } else {
            onExternalLinkClicked(url)
            return true
        }
    }

    private fun isInternalLink(url: URL): Boolean {
        if(url.host == GukhanWikiApi.DOC_URL.host) {
            return true
        }
        return false
    }

    private fun onInternalLinkClicked(url: URL) {
        if(url.path.startsWith(GukhanWikiApi.DOC_PATH)) onDocLinkClicked(url)
        else {
            if(url.path == "/index.php") {
                val query = url.query
                    .split("&").map { it.split("=") }
                    .associate { (key, value) -> key to value }
                val title = GukhanWikiApi.decodeUriComponent(query["title"] ?: return)
                when(query["action"]) {
                    "edit" -> {
                        onInternalEditLinkClicked(title, query["section"])
                    }
                    else -> {
                        onExternalLinkClicked(url)
                    }
                }
            }
        }
    }

    private fun onDocLinkClicked(url: URL) {
        val path = url.path.removePrefix(GukhanWikiApi.DOC_PATH)
        val title = GukhanWikiApi.decodeUriComponent(path)
        val args = Bundle().apply {
            putString("title", title)
        }
        listener.onNavigate(R.id.action_PageViewFragment_self, args)
    }

    private fun onInternalEditLinkClicked(title: String, section: String? = null) {
        val args = Bundle().apply {
            putString("title", title)
            if(section != null) putString("section", section)
        }
        listener.onNavigate(R.id.action_ViewPageFragment_to_editPageFragment, args)
    }

    private fun onExternalLinkClicked(url: URL) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()))
        listener.onStartActivity(intent)
    }

    interface Listener {
        fun onNavigate(@IdRes resId: Int, args: Bundle)
        fun onStartActivity(intent: Intent)
    }

}