package io.github.lee0701.gukhanwiki.android.view

import org.jsoup.nodes.Document

interface WebViewRenderer {
    fun render(content: String): Document
}