package io.github.lee0701.gukhanwiki.android.view.edit

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import io.github.lee0701.gukhanwiki.android.Loadable
import io.github.lee0701.gukhanwiki.android.MainViewModel
import io.github.lee0701.gukhanwiki.android.R
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import io.github.lee0701.gukhanwiki.android.databinding.FragmentConfirmEditBinding
import io.github.lee0701.gukhanwiki.android.view.WebViewClient

class ConfirmEditFragment: Fragment(), WebViewClient.Listener {

    private var _binding: FragmentConfirmEditBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ConfirmEditViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val page = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("page", Page::class.java)
        } else {
            arguments?.getSerializable("page") as Page
        }
        val summary = arguments?.getString("summary").orEmpty()
        if(page != null) {
            viewModel.showConfirmation(page, summary)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfirmEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val onSubmit = {
            activity?.runOnUiThread {
                findNavController().popBackStack(R.id.ViewPageFragment, false)
            }
            Unit
        }

        binding.webView.settings.javaScriptEnabled = true
        binding.webView.addJavascriptInterface(WebAppInterface(onSubmit), "Android")

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

    override fun onNavigate(resId: Int, args: Bundle) {
    }

    override fun onStartActivity(intent: Intent) {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class WebAppInterface(val onSubmitLambda: () -> Unit) {
        @JavascriptInterface
        fun onSubmit() {
            this.onSubmitLambda()
        }
    }

}