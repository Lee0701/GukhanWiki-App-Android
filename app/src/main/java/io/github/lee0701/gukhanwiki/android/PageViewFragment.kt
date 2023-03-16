package io.github.lee0701.gukhanwiki.android

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Space
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.marginBottom
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent
import io.github.lee0701.gukhanwiki.android.databinding.FragmentPageViewBinding
import io.github.lee0701.gukhanwiki.android.view.MainViewModel
import io.github.lee0701.gukhanwiki.android.view.PageContent
import io.github.lee0701.gukhanwiki.android.view.PageViewModel
import org.jsoup.Jsoup

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class PageViewFragment : Fragment() {

    private var _binding: FragmentPageViewBinding? = null
    private val viewModel: PageViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val argTitle = arguments?.getString("title")
        if(argTitle != null) viewModel.loadPage(argTitle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPageViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.title.observe(viewLifecycleOwner) { title ->
            activityViewModel.updateTitle(title)
        }
        viewModel.content.observe(viewLifecycleOwner) { content ->
            val context = ContextThemeWrapper(context ?: return@observe, R.style.Theme_GukhanWikiAppAndroid_WikiPage)
            when(content) {
                is PageContent.Loading -> {}
                is PageContent.Loaded -> {
                    binding.contentView.removeAllViews()
                    val wrapContent = { FlexboxLayout.LayoutParams(
                        FlexboxLayout.LayoutParams.WRAP_CONTENT,
                        FlexboxLayout.LayoutParams.WRAP_CONTENT)
                    }
                    val doc = Jsoup.parse(content.text)
                    val paragraphs = doc.select("h1, h2, h3, h4, h5, h6, p")
                    paragraphs.map { paragraph ->
                        val words = paragraph.text().split(Regex("\\s")).map { word ->
                            TextView(context).apply {
                                text = word
                            }
                        }
                        FlexboxLayout(context).apply {
                            flexDirection = FlexDirection.ROW
                            flexWrap = FlexWrap.WRAP
                            justifyContent = JustifyContent.SPACE_BETWEEN
                            layoutParams = LinearLayoutCompat.LayoutParams(
                                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
                            ).apply {
                                bottomMargin = resources.getDimension(R.dimen.line_spacing).toInt()
                            }
                            words.forEach { word ->
                                addView(word.apply {
                                    layoutParams = wrapContent().apply {
                                        rightMargin = resources.getDimension(R.dimen.word_spacing).toInt()/2
                                        leftMargin = resources.getDimension(R.dimen.word_spacing).toInt()/2
                                    }
                                })
                            }
                            addView(Space(context).apply {
                                layoutParams = wrapContent().apply { flexGrow = 1.0f }
                            })
                        }
                    }.forEach { paragraph ->
                        binding.contentView.addView(paragraph)
                    }
//                    binding.webView.loadData(content.text, "text/html", "UTF-8")
                }
                is PageContent.Error -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}