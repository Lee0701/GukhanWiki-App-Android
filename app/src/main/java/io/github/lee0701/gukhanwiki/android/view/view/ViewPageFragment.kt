package io.github.lee0701.gukhanwiki.android.view.view

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RawRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import io.github.lee0701.gukhanwiki.android.Loadable
import io.github.lee0701.gukhanwiki.android.MainViewModel
import io.github.lee0701.gukhanwiki.android.R
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import io.github.lee0701.gukhanwiki.android.databinding.FragmentViewPageBinding
import io.github.lee0701.gukhanwiki.android.view.WebViewClient
import org.jsoup.Jsoup
import org.jsoup.nodes.DataNode
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ViewPageFragment : Fragment(), WebViewClient.Listener {

    private var _binding: FragmentViewPageBinding? = null
    private val viewModel: ViewPageViewModel by viewModels()
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
        _binding = FragmentViewPageBinding.inflate(inflater, container, false)
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

                    binding.webView.webViewClient = WebViewClient(this)
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

    override fun onNavigate(resId: Int, args: Bundle) {
        findNavController().navigate(resId, args)
    }

    override fun onStartActivity(intent: Intent) {
        startActivity(intent)
    }
}