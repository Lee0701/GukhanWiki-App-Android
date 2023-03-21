package io.github.lee0701.gukhanwiki.android.view.view

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.github.lee0701.gukhanwiki.android.Loadable
import io.github.lee0701.gukhanwiki.android.MainViewModel
import io.github.lee0701.gukhanwiki.android.R
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import io.github.lee0701.gukhanwiki.android.databinding.FragmentViewPageBinding
import io.github.lee0701.gukhanwiki.android.view.WebViewClient
import io.github.lee0701.gukhanwiki.android.view.WebViewRenderer

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ViewPageFragment : Fragment(), WebViewClient.Listener, SwipeRefreshLayout.OnRefreshListener {

    private var _binding: FragmentViewPageBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ViewPageViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var renderer: WebViewRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        renderer = WebViewRenderer(requireContext(), this)
        val content = savedInstanceState?.getString("content")
        if(content == null) {
            val argTitle = arguments?.getString("title")
            if(argTitle != null) viewModel.loadPage(argTitle)
            else viewModel.loadPage(GukhanWikiApi.MAIN_PAGE_TITLE)
        } else {
            viewModel.updatePage(content)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fab.setOnClickListener {
            val title = viewModel.title.value ?: return@setOnClickListener
            val args = Bundle().apply {
                putString("title", title)
            }
            findNavController().navigate(R.id.action_ViewPageFragment_to_editPageFragment, args)
        }

        binding.swipeRefreshLayout.setOnRefreshListener(this)

        viewModel.title.observe(viewLifecycleOwner) { title ->
            activityViewModel.updateTitle(title)
        }
        viewModel.content.observe(viewLifecycleOwner) { content ->
            binding.loadingIndicator.root.visibility = View.GONE
            binding.errorIndicator.root.visibility = View.GONE
            binding.webView.visibility = View.GONE
            binding.swipeRefreshLayout.isRefreshing = false
            when(content) {
                is Loadable.Loading -> {
                    binding.loadingIndicator.root.visibility = View.VISIBLE
                }
                is Loadable.Error -> {
                    binding.errorIndicator.root.visibility = View.VISIBLE
                    binding.errorIndicator.text.text = content.exception.message
                }
                is Loadable.Loaded -> {
                    renderer.render(binding.webView, content.data.orEmpty())
                    binding.webView.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val content = viewModel.content.value
        if(content is Loadable.Loaded) outState.putString("content", content.data)
    }

    override fun onRefresh() {
        val title = viewModel.title.value ?: return
        viewModel.loadPage(title)
    }

    override fun onNavigate(resId: Int, args: Bundle) {
        findNavController().navigate(resId, args)
    }

    override fun onStartActivity(intent: Intent) {
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}