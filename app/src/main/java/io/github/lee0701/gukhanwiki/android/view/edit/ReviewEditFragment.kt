package io.github.lee0701.gukhanwiki.android.view.edit

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import io.github.lee0701.gukhanwiki.android.Loadable
import io.github.lee0701.gukhanwiki.android.MainViewModel
import io.github.lee0701.gukhanwiki.android.R
import io.github.lee0701.gukhanwiki.android.view.WebViewRenderer
import io.github.lee0701.gukhanwiki.android.databinding.FragmentReviewEditBinding
import io.github.lee0701.gukhanwiki.android.view.WebViewClient

class ReviewEditFragment: Fragment(), WebViewClient.Listener {

    private var _binding: FragmentReviewEditBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReviewEditViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var webViewRenderer: WebViewRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        webViewRenderer = WebViewRenderer(requireContext(), this)
        val page = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("page", Page::class.java)
        } else {
            arguments?.getSerializable("page") as Page
        }
        if(page != null) {
            viewModel.reviewEdit(page)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                    webViewRenderer.render(binding.webView, response.data)
                }
            }
        }

        binding.fab.setOnClickListener {
            val page = viewModel.page.value
            if(page is Loadable.Loaded)
                viewModel.updatePage(page.data, binding.summary.text?.toString().orEmpty())
        }

        viewModel.result.observe(viewLifecycleOwner) { response ->
            val message =
                if(response is Loadable.Error) {
                    if(response.exception.message == "Failure") resources.getString(R.string.msg_edit_failed)
                    else response.exception.message
                } else resources.getString(R.string.msg_edit_saved)
            val args = Bundle().apply {
                putString("title", arguments?.getString("title"))
                putString("message", message)
                putBoolean("reload", true)
            }
            if(message != null) activityViewModel.displayMessage(message)
            findNavController().popBackStack(R.id.ViewPageFragment, false)
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

}