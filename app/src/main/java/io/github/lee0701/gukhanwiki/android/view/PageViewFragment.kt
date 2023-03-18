package io.github.lee0701.gukhanwiki.android.view

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
import com.google.android.material.snackbar.Snackbar
import io.github.lee0701.gukhanwiki.android.Loadable
import io.github.lee0701.gukhanwiki.android.R
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import io.github.lee0701.gukhanwiki.android.databinding.FragmentPageViewBinding
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import retrofit2.HttpException
import java.net.URL

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class PageViewFragment : Fragment() {

    private var _binding: FragmentPageViewBinding? = null
    private val viewModel: PageViewViewModel by viewModels()
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

        val message = arguments?.getString("message")
        if(message != null && message.isNotBlank()) {
            Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
            arguments?.remove("message")
        }

        binding.fab.setOnClickListener { _ ->
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
                    when(content.exception) {
                        is HttpException -> {
                            if(content.exception.code() == 404) {
                                val args = Bundle().apply {
                                    putString("title", arguments?.getString("title"))
                                }
                                val navController = findNavController()
                                navController.popBackStack()
                                navController.navigate(R.id.action_PageViewFragment_to_pageEditFragment, args)
                            }
                        }
                        else -> {
                            binding.errorIndicator.root.visibility = View.VISIBLE
                            binding.errorIndicator.text.text = content.exception.message
                        }
                    }
                }
                is Loadable.Loaded -> {
                    binding.webView.visibility = View.VISIBLE
                    val body = Jsoup.parse(content.data).body()
                    val doc = Document(GukhanWikiApi.DOC_URL.toString())
                    doc.appendChild(body)
                    doc.head().appendChild(Element("style").text(loadCustomCss(R.raw.base)))
                    doc.head().appendChild(Element("style").text(loadCustomCss(R.raw.wikitable)))
                    binding.webView.webViewClient = object: WebViewClient() {
                        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val uri = request?.url?.toString()
                            if(uri != null) {
                                val url = URL(uri.toString())
                                if(isInternalLink(url)) {
                                    onInternalLinkClicked(url)
                                    return true
                                }
                            }
                            return super.shouldOverrideUrlLoading(view, request)
                        }
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            uri: String?
                        ): Boolean {
                            if(uri != null) {
                                val url = URL(uri)
                                if(isInternalLink(url)) {
                                    onInternalLinkClicked(url)
                                    return true
                                }
                            }
                            return super.shouldOverrideUrlLoading(view, uri)
                        }
                    }
                    binding.webView.loadDataWithBaseURL(
                        GukhanWikiApi.DOC_URL.toString(),
                        doc.html(),
                        "text/html",
                        "UTF-8",
                        null
                    )
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

    private fun isInternalLink(url: URL): Boolean {
        if(url.host == GukhanWikiApi.DOC_URL.host) {
            return true
        }
        return false
    }

    private fun onInternalLinkClicked(url: URL) {
        val path = url.path.removePrefix(GukhanWikiApi.DOC_PATH)
        val title = GukhanWikiApi.decodeUriComponent(path)
        val args = Bundle().apply {
            putString("title", title)
        }
        findNavController().navigate(R.id.action_PageViewFragment_self, args)
    }

}