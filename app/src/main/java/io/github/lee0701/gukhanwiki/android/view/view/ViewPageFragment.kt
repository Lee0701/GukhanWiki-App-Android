package io.github.lee0701.gukhanwiki.android.view.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.github.lee0701.gukhanwiki.android.Loadable
import io.github.lee0701.gukhanwiki.android.MainViewModel
import io.github.lee0701.gukhanwiki.android.R
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import io.github.lee0701.gukhanwiki.android.databinding.FragmentViewPageBinding
import io.github.lee0701.gukhanwiki.android.view.PageWebViewRenderer
import io.github.lee0701.gukhanwiki.android.view.WebViewClient
import io.github.lee0701.gukhanwiki.android.view.WebViewRenderer

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ViewPageFragment : Fragment(), WebViewClient.Listener, SwipeRefreshLayout.OnRefreshListener {

    private var binding: FragmentViewPageBinding? = null
    private val viewModel: ViewPageViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()

    private lateinit var webViewRenderer: WebViewRenderer

    private var fabExpanded: Boolean = false
    private val fabMenus: List<View> get() = listOfNotNull(binding?.fabEdit, binding?.fabHistory)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = context ?: return
        webViewRenderer = PageWebViewRenderer(context)
        if(viewModel.content.value == null) {
            val argTitle = arguments?.getString("title")
            val argAction = arguments?.getString("action")
            if(argTitle != null) viewModel.loadPage(argTitle, argAction)
            else viewModel.loadPage(GukhanWikiApi.MAIN_PAGE_TITLE)
        }
        val scrollY = savedInstanceState?.getInt("scrollY")
        if(scrollY != null) viewModel.updateScroll(scrollY)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val scrollY = binding?.webView?.scrollY
        if(scrollY != null) {
            outState.putInt("scrollY", scrollY)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewPageBinding.inflate(inflater, container, false)
        return binding?.root ?: View(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = binding ?: return

        fabExpanded = true
        Handler(Looper.getMainLooper()).postDelayed({
            initialFabAnimation().start()
        }, 200)
        binding.fabExpand.setOnClickListener {
            fabAnimation(fabExpanded)?.start()
            fabExpanded = !fabExpanded
        }

        binding.fabEdit.setOnClickListener {
            val title = viewModel.title.value ?: return@setOnClickListener
            val args = Bundle().apply {
                putString("title", title)
            }
            saveScrollY()
            findNavController().navigate(R.id.action_ViewPageFragment_to_editPageFragment, args)
        }

        binding.fabHistory.setOnClickListener {
            val title = viewModel.title.value ?: return@setOnClickListener
            val args = Bundle().apply {
                putString("title", title)
                putString("action", "history")
            }
            saveScrollY()
            findNavController().navigate(R.id.action_ViewPageFragment_self, args)
        }

        binding.swipeRefreshLayout.setOnRefreshListener(this)

        viewModel.hideFab.observe(viewLifecycleOwner) { hide ->
            binding.fabGroup.visibility = if(!hide) View.VISIBLE else View.GONE
        }

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
                    activityViewModel.updateTitle(getString(R.string.label_loading))
                    binding.loadingIndicator.root.visibility = View.VISIBLE
                }
                is Loadable.Error -> {
                    activityViewModel.updateTitle(getString(R.string.label_error))
                    binding.errorIndicator.root.visibility = View.VISIBLE
                    binding.errorIndicator.text.text = content.exception.message
                }
                is Loadable.Loaded -> {
                    binding.webView.visibility = View.VISIBLE
                    binding.webView.webViewClient = WebViewClient(this)
                    val html = webViewRenderer.render(content.data.orEmpty())
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
        viewModel.scrollY.observe(viewLifecycleOwner) { scrollY ->
            binding.webView.scrollY = scrollY
        }
    }

    override fun onRefresh() {
        val title = viewModel.title.value ?: return
        viewModel.loadPage(title)
    }

    override fun onNavigate(resId: Int, args: Bundle) {
        saveScrollY()
        findNavController().navigate(resId, args)
    }

    override fun onStartActivity(intent: Intent) {
        startActivity(intent)
    }

    private fun saveScrollY() {
        viewModel.updateScroll(binding?.webView?.scrollY ?: 0)
    }

    private fun fabAnimation(expanded: Boolean, duration: Int = 200): Animator? {
        val binding = binding ?: return null
        val context = context ?: return null
        val animations = fabMenus.flatMap { fab ->
            val translationYValue = if(!expanded) binding.fabExpand.y - fab.y else 0f
            val scaleValue = if(!expanded) 0f else 1f
            val translationY = ObjectAnimator.ofFloat(fab, "translationY", translationYValue)
            val scaleX = ObjectAnimator.ofFloat(fab, "scaleX", scaleValue)
            val scaleY = ObjectAnimator.ofFloat(fab, "scaleY", scaleValue)
            listOf(translationY, scaleX, scaleY)
        }
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(animations)
        animatorSet.duration = duration.toLong()
        animatorSet.interpolator = DecelerateInterpolator()
        animatorSet.doOnStart {
            if(expanded) fabMenus.forEach { it.visibility = View.VISIBLE }
            val drawableId = if(expanded) R.drawable.baseline_close_24 else R.drawable.baseline_edit_24
            binding.fabExpand.setImageDrawable(ContextCompat.getDrawable(context, drawableId))
        }
        animatorSet.doOnEnd {
            fabMenus.forEach { if(!expanded) it.visibility = View.GONE }
        }
        return animatorSet
    }

    private fun initialFabAnimation(): Animator {
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(fabAnimation(true), fabAnimation(false))
        return animatorSet
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}