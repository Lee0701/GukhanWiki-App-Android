package io.github.lee0701.gukhanwiki.android.view

import org.jsoup.Jsoup

class SimplePageRenderer(
): WebViewRenderer {
    override fun render(content: String): String {
        val doc = Jsoup.parse(content)
        val mainContent = doc.select("#content").first()
        if(mainContent != null) {
            doc.body().remove()
            doc.body().appendChild(mainContent)
        }
        return doc.html()
    }
}