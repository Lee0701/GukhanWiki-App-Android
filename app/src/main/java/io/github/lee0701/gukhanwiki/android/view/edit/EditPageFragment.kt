package io.github.lee0701.gukhanwiki.android.view.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.lee0701.gukhanwiki.android.Loadable
import io.github.lee0701.gukhanwiki.android.MainViewModel
import io.github.lee0701.gukhanwiki.android.R
import io.github.lee0701.gukhanwiki.android.databinding.FragmentEditPageBinding

class EditPageFragment: Fragment() {

    private var binding: FragmentEditPageBinding? = null
    private val viewModel: EditPageViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(findNavController().currentDestination?.id != R.id.editPageFragment) return
        val content = savedInstanceState?.getString("content")
        if(content != null) {
            viewModel.update(content)
        } else {
            val argTitle = arguments?.getString("title")
            val argSection = arguments?.getString("section")
            if(argTitle != null) viewModel.loadPageSource(argTitle, argSection)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val content = binding?.editContent?.text?.toString()
        if(content != null) {
            outState.putString("content", content)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentEditPageBinding.inflate(layoutInflater, container, false)
        this.binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = binding ?: return

        viewModel.page.observe(viewLifecycleOwner) { page ->
            when(page) {
                is Loadable.Loading -> {}
                is Loadable.Error -> {
                    binding.errorIndicator.text.text = page.exception.message
                }
                is Loadable.Loaded -> {
                    activityViewModel.updateTitle(page.data.title)
                }
            }
        }

        viewModel.content.observe(viewLifecycleOwner) { content ->
            when(content) {
                is Loadable.Loaded -> {
                    binding.editContent.setText(content.data)
                }
                else -> {}
            }
        }

        binding.fab.setOnClickListener {
            val page = viewModel.page.value
            val content = viewModel.content.value
            if(page is Loadable.Loaded && content is Loadable.Loaded) {
                binding.loadingIndicator.root.visibility = View.VISIBLE
                binding.editContent.isEnabled = false
                binding.fab.isEnabled = false
                viewModel.update(binding.editContent.text.toString())
                val args = Bundle().apply {
                    putSerializable("page", page.data)
                    putString("content", binding.editContent.text.toString())
                }
                findNavController().navigate(R.id.action_editPageFragment_to_reviewEditFragment, args)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val context = context ?: return@addCallback
            MaterialAlertDialogBuilder(context)
                .setMessage(R.string.msg_confirm_discard_edit)
                .setPositiveButton(R.string.action_discard_edit) { _, _ ->
                    findNavController().navigateUp()
                }
                .setNegativeButton(R.string.action_keep_editing) { _, _ ->
                }
                .show()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}