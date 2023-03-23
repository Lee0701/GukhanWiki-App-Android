package io.github.lee0701.gukhanwiki.android.view.edit

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import io.github.lee0701.gukhanwiki.android.Loadable
import io.github.lee0701.gukhanwiki.android.MainViewModel
import io.github.lee0701.gukhanwiki.android.R
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import io.github.lee0701.gukhanwiki.android.api.action.Page
import io.github.lee0701.gukhanwiki.android.databinding.FragmentReviewEditBinding
import io.github.lee0701.gukhanwiki.android.view.WebViewClient
import io.github.lee0701.gukhanwiki.android.view.PageWebViewRenderer
import io.github.lee0701.gukhanwiki.android.view.WebViewRenderer

class ReviewEditFragment: Fragment(), WebViewClient.Listener {

    private var binding: FragmentReviewEditBinding? = null
    private val viewModel: ReviewEditViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var webViewRenderer: WebViewRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = context ?: return
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        webViewRenderer = PageWebViewRenderer(context)

        val content = savedInstanceState?.getString("content")
        if(content != null) {
            viewModel.updateContent(content)
        } else {
            val page = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arguments?.getSerializable("page", Page::class.java)
            } else {
                arguments?.getSerializable("page") as Page
            }
            val newContent = arguments?.getString("content").orEmpty()
            if(page != null) {
                viewModel.reviewEdit(page, newContent)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val content = viewModel.content.value
        if(content is Loadable.Loaded) {
            outState.putString("content", content.data)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentReviewEditBinding.inflate(inflater, container, false)
        this.binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = binding ?: return

        viewModel.html.observe(viewLifecycleOwner) { response ->
            binding.loadingIndicator.root.visibility = View.GONE
            binding.errorIndicator.root.visibility = View.GONE
            binding.webView.visibility = View.GONE
            when(response) {
                is Loadable.Error -> {
                    binding.errorIndicator.root.visibility = View.VISIBLE
                    binding.errorIndicator.text.text = response.exception.message
                }
                is Loadable.Loading -> {
                    binding.loadingIndicator.root.visibility = View.VISIBLE
                }
                is Loadable.Loaded -> {
                    binding.webView.visibility = View.VISIBLE
                    val html = webViewRenderer.render(response.data)
                    binding.webView.webViewClient = WebViewClient(this)
                    binding.webView.loadDataWithBaseURL(
                        GukhanWikiApi.DOC_URL.toString(),
                        html,
                        "text/html",
                        "UTF-8",
                        null,
                    )
                }
            }
        }

        binding.fab.setOnClickListener {
            val page = viewModel.page.value
            val content = viewModel.content.value
            if(page is Loadable.Loaded && content is Loadable.Loaded)
                viewModel.updatePage(page.data, content.data, binding.summary.text?.toString().orEmpty())
        }

        viewModel.result.observe(viewLifecycleOwner) { response ->
            if(response is Loadable.Error) {
                val page = viewModel.page.value
                val content = viewModel.content.value
                val message = response.exception.message.orEmpty()
                if(page is Loadable.Loaded && content is Loadable.Loaded) {
                    if(response.exception.message == "captcha") {
                        val args = Bundle().apply {
                            putSerializable("page", page.data)
                            putString("content", content.data)
                            putString("summary", binding.summary.text.toString())
                        }
                        findNavController().navigate(R.id.action_reviewEditFragment_to_confirmEditFragment, args)
                    } else {
                        activityViewModel.showSnackbar(resources.getString(R.string.msg_edit_error, message))
                    }
                } else {
                    activityViewModel.showSnackbar(resources.getString(R.string.msg_edit_error, message))
                }
            } else if(response is Loadable.Loaded) {
                findNavController().popBackStack(R.id.ViewPageFragment, false)
            }
        }

    }

    override fun onNavigate(resId: Int, args: Bundle) {
    }

    override fun onStartActivity(intent: Intent) {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}