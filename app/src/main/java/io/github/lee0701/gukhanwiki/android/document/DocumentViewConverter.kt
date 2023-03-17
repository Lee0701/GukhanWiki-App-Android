package io.github.lee0701.gukhanwiki.android.document

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Space
import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent
import io.github.lee0701.gukhanwiki.android.R
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

class DocumentViewConverter(
    context: Context,
    private val listener: Listener
) {
    private val context = ContextThemeWrapper(context, R.style.Theme_GukhanWikiAppAndroid_WikiPage)

    fun convert(html: String): View? {
        val doc = Jsoup.parse(html)
        return parseRecursive(context, doc.body())
    }

    fun parseRecursive(context: Context, element: Element): View? {
        val view = when(element.tagName()) {
            "section" -> sectionElement(context, element)
            in TAGS_HEADER -> headerElement(context, element)
            "p" -> paragraphElement(context, element)
            "a" -> anchorElement(context, element)
            "span" -> spanElement(context, element)
            "body" -> bodyElement(context, element)
//            "ul" -> unorderedListElement(context, element)
//            "ol" -> orderedListElement(context, element)
//            "li" -> listItemElement(context, element)
//            "table" -> tableElement(context, element)
            else -> null
        }
        val newContext = view?.context ?: return null
        val children = element.childNodes().mapNotNull { node ->
            when(node) {
                is TextNode -> textNode(newContext, node.text())
                is Element -> parseRecursive(newContext, node)
                else -> null
            }
        }
        if(view is ViewGroup) {
            children.forEach { view.addView(it) }
            if(element.tagName() == "p") {
                view.addView(Space(context).apply {
                    layoutParams = FlexboxLayout.LayoutParams(
                        FlexboxLayout.LayoutParams.WRAP_CONTENT,
                        FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    ).apply {
                        flexGrow = 1.0f
                    }
                })
            }
        }
        return view
    }

    private fun sectionElement(context: Context, element: Element): View {
        return LinearLayoutCompat(context).apply {
            orientation = LinearLayoutCompat.VERTICAL
        }
    }

    private fun headerElement(baseContext: Context, element: Element): View {
        val level = TAGS_HEADER.indexOf(element.tagName())
        val context = ContextThemeWrapper(baseContext, HEADER_TEXT_STYLES[level])
        return LinearLayoutCompat(context).apply {
            orientation = LinearLayoutCompat.HORIZONTAL
        }
    }

    private fun paragraphElement(context: Context, element: Element): View {
        return FlexboxLayout(context).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.SPACE_BETWEEN
            layoutParams = LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = resources.getDimension(R.dimen.line_spacing).toInt()
            }
        }
    }

    private fun anchorElement(baseContext: Context, element: Element): View {
        val context = ContextThemeWrapper(baseContext, R.style.Theme_GukhanWikiAppAndroid_WikiPage_A)
        return LinearLayoutCompat(context).apply {
            orientation = LinearLayoutCompat.HORIZONTAL
            isClickable = true
            setOnClickListener { listener.onInternalLinkClicked(element.attr("href")) }
        }
    }

    private fun spanElement(context: Context, element: Element): View {
        return LinearLayoutCompat(context).apply {
            orientation = LinearLayoutCompat.HORIZONTAL
            isClickable = true
        }
    }

    private fun bodyElement(context: Context, element: Element): View {
        return LinearLayoutCompat(context).apply {
            orientation = LinearLayoutCompat.VERTICAL
        }
    }

    private fun textNode(context: Context, text: String): View {
        return TextView(context).apply {
            this.text = text
        }
    }

    interface Listener {
        fun onInternalLinkClicked(path: String)
    }

    companion object {
        val TAGS_HEADER = listOf("h1", "h2", "h3", "h4", "h5", "h6")
        @StyleRes val HEADER_TEXT_STYLES = listOf(
            R.style.Theme_GukhanWikiAppAndroid_WikiPage_H1,
            R.style.Theme_GukhanWikiAppAndroid_WikiPage_H2,
            R.style.Theme_GukhanWikiAppAndroid_WikiPage_H3,
            R.style.Theme_GukhanWikiAppAndroid_WikiPage_H4,
            R.style.Theme_GukhanWikiAppAndroid_WikiPage_H5,
            R.style.Theme_GukhanWikiAppAndroid_WikiPage_H6,
        )
    }
}