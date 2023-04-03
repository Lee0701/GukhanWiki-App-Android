package io.github.lee0701.gukhanwiki.android.view.edit

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import io.github.lee0701.gukhanwiki.android.Result
import io.github.lee0701.gukhanwiki.android.MainViewModel
import io.github.lee0701.gukhanwiki.android.R
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import io.github.lee0701.gukhanwiki.android.api.action.Page
import io.github.lee0701.gukhanwiki.android.databinding.FragmentConfirmEditBinding
import io.github.lee0701.gukhanwiki.android.view.view.ViewPageFragment
import java.net.URL

class ConfirmEditFragment: Fragment() {

    private var binding: FragmentConfirmEditBinding? = null
    private val viewModel: ConfirmEditViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()

    private val webViewClient = object: WebViewClient() {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            if(view != null && request != null) {
                val url = URL(request.url.toString())
                if(shouldOverrideUrlLoading(url)) return true
            }
            return super.shouldOverrideUrlLoading(view, request)
        }

        @Suppress("DEPRECATION")
        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView?, strUrl: String?): Boolean {
            if(view != null && strUrl != null) {
                val url = URL(strUrl)
                if(shouldOverrideUrlLoading(url)) return true
            }
            return super.shouldOverrideUrlLoading(view, strUrl)
        }

        fun shouldOverrideUrlLoading(url: URL): Boolean {
            val query = url.query.orEmpty()
                .split("&").map { it.split("=") }
                .filter { it.size >= 2 }
                .associate { (key, value) -> key to value }
            if(query["action"] != "edit") {
                setFragmentResult(ViewPageFragment.REQUEST_KEY_EDIT_PAGE, Bundle().apply {
                    putBoolean("success", true)
                })
                findNavController().popBackStack(R.id.ViewPageFragment, false)
                return true
            }
            return false
        }
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val page = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("page", Page::class.java)
        } else {
            arguments?.getSerializable("page") as Page
        }
        val content = arguments?.getString("content").orEmpty()
        val summary = arguments?.getString("summary").orEmpty()
        if(page != null) {
            viewModel.showConfirmation(page, content, summary)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentConfirmEditBinding.inflate(inflater, container, false)
        this.binding = binding
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = binding ?: return

        binding.webView.settings.javaScriptEnabled = true
        binding.webView.webViewClient = webViewClient

        viewModel.html.observe(viewLifecycleOwner) { response ->
            binding.loadingIndicator.root.visibility = View.GONE
            binding.errorIndicator.root.visibility = View.GONE
            binding.webView.visibility = View.GONE
            when(response) {
                is Result.Error -> {
                    binding.errorIndicator.root.visibility = View.VISIBLE
                    binding.errorIndicator.text.text = response.exception.message
                }
                is Result.Loading -> {
                    binding.loadingIndicator.root.visibility = View.VISIBLE
                }
                is Result.Loaded -> {
                    binding.webView.loadDataWithBaseURL(
                        GukhanWikiApi.CLIENT_URL.toString(),
                        response.data,
                        "text/html",
                        "UTF-8",
                        null,
                    )
                    binding.webView.visibility = View.VISIBLE
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}