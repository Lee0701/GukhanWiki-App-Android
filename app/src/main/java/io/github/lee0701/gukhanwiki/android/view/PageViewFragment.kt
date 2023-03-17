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
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import io.github.lee0701.gukhanwiki.android.R
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import io.github.lee0701.gukhanwiki.android.databinding.FragmentPageViewBinding
import org.jsoup.Jsoup
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

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
            when(content) {
                is PageContent.Loading -> {
                    binding.loadingIndicator.root.visibility = View.VISIBLE
                }
                is PageContent.Error -> {
                    binding.loadingIndicator.root.visibility = View.GONE
                }
                is PageContent.Loaded -> {
                    binding.loadingIndicator.root.visibility = View.GONE
                    val doc = Jsoup.parse(content.text)
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
                        doc.body().html(),
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

    private fun isInternalLink(url: URL): Boolean {
        if(url.host == GukhanWikiApi.DOC_URL.host) {
            return true
        }
        return false
    }

    private fun onInternalLinkClicked(url: URL) {
        val path = url.path.removePrefix(GukhanWikiApi.DOC_PATH)
        val charset = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            StandardCharsets.UTF_8.name()
        else
            "UTF-8"
        val title = URLDecoder.decode(path, charset)
        val args = Bundle().apply {
            putString("title", title)
        }
        findNavController().navigate(R.id.action_PageViewFragment_self, args)
    }

}