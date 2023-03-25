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
): WebViewRenderer {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val nightMode = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK).let { when(it) {
        Configuration.UI_MODE_NIGHT_NO -> false
        Configuration.UI_MODE_NIGHT_YES -> true
        else -> false
    } }

    override fun render(content: String): String {
        val body = Jsoup.parse(content).body()
        val doc = Document(GukhanWikiApi.DOC_URL.toString())
        doc.outputSettings(Document.OutputSettings().prettyPrint(false))
        doc.appendChild(body)

        val textSize = sharedPreferences.getString("display_text_size", "1.0")?.toFloat() ?: 1.0f

        println(textSize)
        val textSizeCss = """
            body {
                font-size: ${textSize}rem;
            }
        """.trimIndent()
        doc.head().appendChild(Element("style").appendChild(DataNode(textSizeCss)))

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
        return doc.html()
    }

    private fun loadCustomCss(@RawRes id: Int): String {
        val stream = context.resources.openRawResource(id)
        val data = stream.readBytes()
        stream.close()
        return data.decodeToString()
    }

}