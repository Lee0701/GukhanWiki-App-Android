package io.github.lee0701.gukhanwiki.android.view.edit

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
import io.github.lee0701.gukhanwiki.android.databinding.FragmentPageEditBinding

class EditPageFragment: Fragment() {

    private var _binding: FragmentPageEditBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditPageViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val argTitle = arguments?.getString("title")
        val argSection = arguments?.getString("section", null)
        if(argTitle != null) viewModel.loadPageSource(argTitle, argSection)
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
                    binding.editContent.setText(page.data.content)
                    binding.editContent.visibility = View.VISIBLE

                    binding.fab.setOnClickListener {
                        hideAllLayers()
                        binding.loadingIndicator.root.visibility = View.VISIBLE
                        binding.editContent.isEnabled = false
                        binding.fab.isEnabled = false
                        viewModel.updatePage(
                            title = page.data.title,
                            content = binding.editContent.text.toString(),
                            section = page.data.section,
                            summary = "",   // TODO
                            baseRevId = page.data.revId,
                        )
                    }
                }
            }
        }

        viewModel.result.observe(viewLifecycleOwner) { response ->
            val navController = findNavController()
            navController.navigateUp()
            val id = navController.currentDestination?.id!!
            navController.popBackStack(id, true)
            val message =
                if(response is Loadable.Error) {
                    if(response.exception.message == "Failure") resources.getString(R.string.msg_edit_failed)
                    else response.exception.message
                } else resources.getString(R.string.msg_edit_saved)
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