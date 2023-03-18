package io.github.lee0701.gukhanwiki.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import io.github.lee0701.gukhanwiki.android.R
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import io.github.lee0701.gukhanwiki.android.databinding.FragmentPageEditBinding

class PageEditFragment: Fragment() {

    private var _binding: FragmentPageEditBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PageEditViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val argTitle = arguments?.getString("title")
        if(argTitle != null) viewModel.loadPageSource(argTitle)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPageEditBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideAllLayers()
        binding.loadingIndicator.root.visibility = View.VISIBLE

        activityViewModel.signedInAccount.observe(viewLifecycleOwner) { account ->
            binding.fab.setOnClickListener {
                val title = viewModel.page.value?.title
                if(title != null) {
                    hideAllLayers()
                    binding.loadingIndicator.root.visibility = View.VISIBLE
                    binding.editContent.isEnabled = false
                    binding.fab.isEnabled = false
                    viewModel.updatePage(title, binding.editContent.text.toString())
                }
            }
        }

        viewModel.page.observe(viewLifecycleOwner) { page ->
            hideAllLayers()
            activityViewModel.updateTitle(page.title)
            binding.editContent.setText(page.source)
            binding.editContent.visibility = View.VISIBLE
        }

        viewModel.result.observe(viewLifecycleOwner) { content ->
            val navController = findNavController()
            navController.navigateUp()
            val id = navController.currentDestination?.id!!
            navController.popBackStack(id, true)
            val message =
                if(content.message != null) content.message
                else resources.getString(R.string.msg_edit_saved)
            val args = Bundle().apply {
                putString("title", arguments?.getString("title"))
                putString("message", message)
            }
            navController.navigate(id, args)
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