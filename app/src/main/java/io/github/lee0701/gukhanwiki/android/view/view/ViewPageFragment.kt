package io.github.lee0701.gukhanwiki.android.view.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.github.lee0701.gukhanwiki.android.Result
import io.github.lee0701.gukhanwiki.android.MainViewModel
import io.github.lee0701.gukhanwiki.android.R
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import io.github.lee0701.gukhanwiki.android.databinding.FragmentViewPageBinding
import io.github.lee0701.gukhanwiki.android.view.PageWebViewRenderer
import io.github.lee0701.gukhanwiki.android.view.WebViewClient
import io.github.lee0701.gukhanwiki.android.view.WebViewRenderer
import java.net.URL

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ViewPageFragment: Fragment(), WebViewClient.Listener, SwipeRefreshLayout.OnRefreshListener {

    private var binding: FragmentViewPageBinding? = null
    private val viewModel: ViewPageViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()

    private lateinit var webViewRenderer: WebViewRenderer
    private lateinit var webViewRendererWithoutFabMargin: WebViewRenderer
    private val references: MutableMap<Int, String> = mutableMapOf()

    private var fabExpanded: Boolean = false
    private val fabMenus: List<View> get() = listOfNotNull(binding?.fabEdit, binding?.fabTalk, binding?.fabHistory)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = context ?: return

        webViewRenderer = PageWebViewRenderer(context)
        webViewRendererWithoutFabMargin = PageWebViewRenderer(context, false)

        setFragmentResultListener(REQUEST_KEY_EDIT_PAGE) { requestKey, bundle ->
            if(bundle.getBoolean("success")) this.onRefresh()
        }

        if(viewModel.content.value == null) {
            val argTitle = arguments?.getString("title")?.let { GukhanWikiApi.decodeUriComponent(it) }
            val argAction = arguments?.getString("action")
            val query = arguments?.keySet().orEmpty()
                .filter { k -> k !in setOf("title", "action") }
                .mapNotNull { k -> arguments?.getString(k)?.let { v -> k to v } }
                .toMap().toMutableMap()
            query += "redirects" to "true"

            if(argTitle != null) viewModel.loadPage(argTitle, argAction, query)
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
        try {
            binding = FragmentViewPageBinding.inflate(inflater, container, false)
        } catch(ex: InflateException) {
            ex.printStackTrace()
            Toast.makeText(context, R.string.msg_launch_failed, Toast.LENGTH_LONG).show()
        }
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

        binding.swipeRefreshLayout.setOnRefreshListener(this)

        viewModel.hideFab.observe(viewLifecycleOwner) { hide ->
            binding.fabGroup.visibility = if(!hide) View.VISIBLE else View.GONE
        }

        viewModel.url.observe(viewLifecycleOwner) { url ->
            activityViewModel.updateUrl(url)
        }

        activityViewModel.title.observe(viewLifecycleOwner) { title ->
            if(viewModel.title.value == title) return@observe
            viewModel.loadPage(title)
        }

        viewModel.title.observe(viewLifecycleOwner) { title ->
            activityViewModel.setTempTitle(null)
            activityViewModel.updateTitle(title)

            binding.fabEdit.setOnClickListener {
                val args = Bundle().apply {
                    putString("title", title)
                }
                saveScrollY()
                findNavController().navigate(R.id.action_ViewPageFragment_to_editPageFragment, args)
            }

            binding.fabHistory.setOnClickListener {
                val args = Bundle().apply {
                    putString("title", title)
                    putString("action", "history")
                }
                onNavigate(R.id.action_ViewPageFragment_self, args)
            }

            binding.missingPageIndicator.createPage.setOnClickListener {
                val args = Bundle().apply {
                    putString("title", title)
                }
                onNavigate(R.id.action_ViewPageFragment_to_editPageFragment, args)
            }

        }

        viewModel.content.observe(viewLifecycleOwner) { content ->
            binding.loadingIndicator.root.visibility = View.GONE
            binding.errorIndicator.root.visibility = View.GONE
            binding.missingPageIndicator.root.visibility = View.GONE
            binding.webView.visibility = View.GONE
            binding.swipeRefreshLayout.isRefreshing = false
            when(content) {
                is Result.Loading -> {
                    activityViewModel.setTempTitle(getString(R.string.label_loading))
                    binding.loadingIndicator.root.visibility = View.VISIBLE
                }
                is Result.Error -> {
                    if(content.exception.message == "missingtitle") {
                        binding.missingPageIndicator.root.visibility = View.VISIBLE
                    } else {
                        activityViewModel.setTempTitle(getString(R.string.label_error))
                        binding.errorIndicator.root.visibility = View.VISIBLE
                        binding.errorIndicator.text.text = content.exception.message
                    }
                }
                is Result.Loaded -> {
                    binding.webView.visibility = View.VISIBLE
                    binding.webView.webViewClient = WebViewClient(this)
                    val rendered = webViewRenderer.render(content.data.orEmpty())
                    rendered.select("li[id^=\"cite_note-\"]").forEach { item ->
                        val href = item.select("span.mw-cite-backlink > a")
                        val html = item.select("span.reference-text").html()
                        val id = href.attr("href").removePrefix("#cite_ref-").toIntOrNull()
                        if(id != null) references += id to html
                    }
                    val html = rendered.html()
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

        viewModel.associatedPage.observe(viewLifecycleOwner) { title ->
            binding.fabTalk.setOnClickListener {
                val args = Bundle().apply {
                    putString("title", title)
                }
                findNavController().navigate(R.id.action_ViewPageFragment_self, args)
            }
        }

        viewModel.scrollY.observe(viewLifecycleOwner) { scrollY ->
            binding.webView.scrollY = scrollY
        }

        viewModel.refresh.observe(viewLifecycleOwner) { refresh ->
            val content = viewModel.content.value
            if(content !is Result.Loaded) viewModel.refresh()
        }
    }

    override fun onRefresh() {
        viewModel.refresh()
    }

    override fun onCiteClicked(id: Int) {
        val title = getString(R.string.footnote, id)
        val html = webViewRendererWithoutFabMargin.render(references[id] ?: return).html()
        val bottomSheet = FootnoteBottomSheet(title, html)
        bottomSheet.show(childFragmentManager, FootnoteBottomSheet.TAG)
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
        val animations = listOfNotNull(fabAnimation(true), fabAnimation(false))
        animatorSet.playSequentially(animations)
        return animatorSet
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        const val REQUEST_KEY_EDIT_PAGE = "edit_page"
    }
}