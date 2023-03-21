package io.github.lee0701.gukhanwiki.android.view.edit

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import io.github.lee0701.gukhanwiki.android.Loadable
import io.github.lee0701.gukhanwiki.android.MainViewModel
import io.github.lee0701.gukhanwiki.android.R
import io.github.lee0701.gukhanwiki.android.databinding.FragmentEditPageBinding

class EditPageFragment: Fragment() {

    private var _binding: FragmentEditPageBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditPageViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState != null) {
            val page = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                savedInstanceState.getSerializable("page", Page::class.java)
            } else {
                savedInstanceState.getSerializable("page")
            } as Page?
            if(page != null) viewModel.restorePage(page)
        } else {
            val argTitle = arguments?.getString("title")
            val argSection = arguments?.getString("section", null)
            if(argTitle != null) viewModel.loadPageSource(argTitle, argSection)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val page = viewModel.page.value
        val text = binding.editContent.text.toString()
        if(page is Loadable.Loaded) {
            outState.putSerializable("page", page.data.copy(wikiText = text))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPageBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.page.observe(viewLifecycleOwner) { page ->
            binding.loadingIndicator.root.visibility = View.GONE
            binding.errorIndicator.root.visibility = View.GONE
            binding.editContent.visibility = View.GONE
            when(page) {
                is Loadable.Loading -> {
                    binding.loadingIndicator.root.visibility = View.VISIBLE
                }
                is Loadable.Error -> {
                    binding.errorIndicator.root.visibility = View.VISIBLE
                    binding.errorIndicator.text.text = page.exception.message
                }
                is Loadable.Loaded -> {
                    binding.editContent.visibility = View.VISIBLE

                    activityViewModel.updateTitle(page.data.title)
                    binding.editContent.setText(page.data.wikiText)
                    binding.editContent.visibility = View.VISIBLE
                }
            }
        }

        binding.fab.setOnClickListener {
            hideAllLayers()
            val page = viewModel.page.value
            if(page is Loadable.Loaded) {
                binding.loadingIndicator.root.visibility = View.VISIBLE
                binding.editContent.isEnabled = false
                binding.fab.isEnabled = false
                val newPage = page.data.copy(wikiText = binding.editContent.text.toString())
                val args = Bundle().apply {
                    putSerializable("page", newPage)
                }
                findNavController().navigate(R.id.action_editPageFragment_to_reviewEditFragment, args)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun hideAllLayers() {
        binding.loadingIndicator.root.visibility = View.GONE
        binding.errorIndicator.root.visibility = View.GONE
        binding.editContent.visibility = View.GONE
    }

}