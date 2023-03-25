package io.github.lee0701.gukhanwiki.android.view

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.RawRes
import androidx.preference.PreferenceManager
import io.github.lee0701.gukhanwiki.android.R
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import org.jsoup.Jsoup
import org.jsoup.nodes.DataNode
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class PageWebViewRenderer(
    private val context: Context,
    private val fabMargin: Boolean = true,
): WebViewRenderer {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val nightMode = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK).let { when(it) {
        Configuration.UI_MODE_NIGHT_NO -> false
        Configuration.UI_MODE_NIGHT_YES -> true
        else -> false
    } }

    override fun render(content: String): Document {
        val body = Jsoup.parse(content).body()
        val doc = Document(GukhanWikiApi.DOC_URL.toString())
        doc.outputSettings(Document.OutputSettings().prettyPrint(false))
        doc.appendChild(body)

        val textSize = sharedPreferences.getString("display_text_size", "1.0")?.toFloat() ?: 1.0f
        val fabMargin = if(this.fabMargin) 100 else 0
        val inlineCss = """
            body {
                font-size: ${textSize}rem;
                /* FloatingActionButton */
                margin-bottom: ${fabMargin}px;
            }
        """.trimIndent()
        doc.head().appendChild(Element("style").appendChild(DataNode(inlineCss)))

        val arr = context.resources.getStringArray(if(!nightMode) R.array.css_list else R.array.night_css_list)
        val stylesheets = arr.map { filename ->
            val id = context.resources.getIdentifier(filename, "raw", context.packageName)
            val stream = context.resources.openRawResource(id)
            val data = stream.readBytes()
            stream.close()
            data.decodeToString()
        }
        stylesheets.forEach { stylesheet ->
            doc.head().appendChild(Element("style").appendChild(DataNode(stylesheet)))
        }

        doc.head().appendChild(Element("style").appendChild(DataNode(loadCustomCss(R.raw.ruby_hide))))
        if(sharedPreferences.getBoolean("ruby_enabled", true)) {
            val minGrade = sharedPreferences.getString("ruby_grade", "80")
            val position = sharedPreferences.getString("ruby_position", "top")
            val grades = context.resources.getStringArray(R.array.pref_ruby_grade_values).reversed()
            val display = if(position == "top") "revert" else "inline-block"
            val rubyShow = mutableListOf<String>()
            for(g in grades) {
                rubyShow += "ruby.hanja.grade$g > rt { display: $display; } ruby.hanja.grade$g > rp { display: revert; }"
                if(minGrade == g) break
            }
            doc.head().appendChild(Element("style").appendChild(DataNode(rubyShow.joinToString("\n"))))
        }

        // Cite note processing (they would be intercepted on click to display the reference as bottom sheet)
        doc.select("a[href^=\"#cite_note-\"]").forEach { a ->
            val attr = a.attr("href")
            val value = "/" + attr.removePrefix("#")
            a.attr("href", value)
        }

        return doc
    }

    private fun loadCustomCss(@RawRes id: Int): String {
        val stream = context.resources.openRawResource(id)
        val data = stream.readBytes()
        stream.close()
        return data.decodeToString()
    }

}