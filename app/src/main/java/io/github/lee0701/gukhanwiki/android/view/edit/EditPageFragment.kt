package io.github.lee0701.gukhanwiki.android.view.edit

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
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
        val content = savedInstanceState?.getString("content")
        if(content == null) {
            val argTitle = arguments?.getString("title")
            val argSection = arguments?.getString("section", null)
            if(argTitle != null) viewModel.loadPageSource(argTitle, argSection)
        } else {
            viewModel.updatePageSource(content)
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
                    if(binding.editContent.toString() != page.data.wikiText)
                        binding.editContent.setText(page.data.wikiText)
                }
            }
        }

        binding.fab.setOnClickListener {
            val page = viewModel.page.value
            if(page is Loadable.Loaded) {
                binding.loadingIndicator.root.visibility = View.VISIBLE
                binding.editContent.isEnabled = false
                binding.fab.isEnabled = false
                val newPage = page.data.copy(wikiText = binding.editContent.text.toString())
                val args = Bundle().apply {
                    putSerializable("page", newPage)
                }
                viewModel.updatePage(page = newPage)
                findNavController().navigate(R.id.action_editPageFragment_to_reviewEditFragment, args)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            AlertDialog.Builder(requireContext())
                .setMessage(R.string.msg_confirm_discard_edit)
                .setPositiveButton(R.string.action_discard_edit) { _, _ ->
                    findNavController().navigateUp()
                }
                .setNegativeButton(R.string.action_keep_editing) { _, _ ->
                }
                .show()
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("content", binding.editContent.text.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}