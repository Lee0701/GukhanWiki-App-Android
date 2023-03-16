package io.github.lee0701.gukhanwiki.android

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import io.github.lee0701.gukhanwiki.android.databinding.FragmentPageViewBinding
import io.github.lee0701.gukhanwiki.android.view.MainViewModel
import io.github.lee0701.gukhanwiki.android.view.PageContent
import io.github.lee0701.gukhanwiki.android.view.PageViewModel

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
                is PageContent.Loading -> {}
                is PageContent.Loaded -> {
                    binding.webView.loadData(content.text, "text/html", "UTF-8")
                }
                is PageContent.Error -> {}
            }
        }

        viewModel.loadPage("國漢大百科:大門")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}