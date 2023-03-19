package io.github.lee0701.gukhanwiki.android.view.view

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RawRes
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import io.github.lee0701.gukhanwiki.android.Loadable
import io.github.lee0701.gukhanwiki.android.MainViewModel
import io.github.lee0701.gukhanwiki.android.R
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import io.github.lee0701.gukhanwiki.android.databinding.FragmentPageViewBinding
import org.jsoup.Jsoup
import org.jsoup.nodes.DataNode
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.net.URL

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class PageViewFragment : Fragment() {

    private var _binding: FragmentPageViewBinding? = null
    private val viewModel: PageViewViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()

    private lateinit var sharedPreferences: SharedPreferences

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
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

        val message = arguments?.getString("message")
        if(message != null && message.isNotBlank()) {
            Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
            arguments?.remove("message")
        }

        binding.fab.setOnClickListener {
            val title = viewModel.title.value ?: return@setOnClickListener
            val args = Bundle().apply {
                putString("title", title)
            }
            findNavController().navigate(R.id.action_PageViewFragment_to_pageEditFragment, args)
        }

        viewModel.title.observe(viewLifecycleOwner) { title ->
            activityViewModel.updateTitle(title)
        }
        viewModel.content.observe(viewLifecycleOwner) { content ->
            binding.loadingIndicator.root.visibility = View.GONE
            binding.errorIndicator.root.visibility = View.GONE
            binding.webView.visibility = View.GONE
            when(content) {
                is Loadable.Loading -> {
                    binding.loadingIndicator.root.visibility = View.VISIBLE
                }
                is Loadable.Error -> {
                    binding.errorIndicator.root.visibility = View.VISIBLE
                    binding.errorIndicator.text.text = content.exception.message
                }
                is Loadable.Loaded -> {
                    val body = Jsoup.parse(content.data.orEmpty()).body()
                    val doc = Document(GukhanWikiApi.DOC_URL.toString())
                    doc.outputSettings(Document.OutputSettings().prettyPrint(false))
                    doc.appendChild(body)
                    doc.head().appendChild(Element("style").appendChild(DataNode((loadCustomCss(R.raw.base)))))
                    doc.head().appendChild(Element("style").appendChild(DataNode((loadCustomCss(R.raw.responsive)))))
                    doc.head().appendChild(Element("style").appendChild(DataNode((loadCustomCss(R.raw.wikitable)))))

                    doc.head().appendChild(Element("style").appendChild(DataNode((loadCustomCss(R.raw.ruby_hide)))))
                    if(sharedPreferences.getBoolean("ruby_enabled", true)) {
                        val minGrade = sharedPreferences.getString("ruby_grade", "80")
                        val position = sharedPreferences.getString("ruby_position", "top")
                        val grades = resources.getStringArray(R.array.pref_ruby_grade_values).reversed()
                        val display = if(position == "top") "revert" else "inline-block"
                        val rubyShow = mutableListOf<String>()
                        for(g in grades) {
                            rubyShow += "ruby.hanja.grade$g > rt { display: $display; } ruby.hanja.grade$g > rp { display: revert; }"
                            if(minGrade == g) break
                        }
                        doc.head().appendChild(Element("style").appendChild(DataNode(rubyShow.joinToString("\n"))))
                    }

                    binding.webView.webViewClient = object: WebViewClient() {
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
                    }
                    binding.webView.loadDataWithBaseURL(
                        GukhanWikiApi.DOC_URL.toString(),
                        doc.html(),
                        "text/html",
                        "UTF-8",
                        null
                    )
                    binding.webView.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadCustomCss(@RawRes id: Int): String {
        val stream = resources.openRawResource(id)
        val data = stream.readBytes()
        stream.close()
        return data.decodeToString()
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
        findNavController().navigate(R.id.action_PageViewFragment_self, args)
    }

    private fun onInternalEditLinkClicked(title: String, section: String? = null) {
        val args = Bundle().apply {
            putString("title", title)
            if(section != null) putString("section", section)
        }
        findNavController().navigate(R.id.action_PageViewFragment_to_pageEditFragment, args)
    }

    private fun onExternalLinkClicked(url: URL) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()))
        startActivity(intent)
    }

}