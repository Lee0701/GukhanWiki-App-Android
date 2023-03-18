package io.github.lee0701.gukhanwiki.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import io.github.lee0701.gukhanwiki.android.R
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

        binding.editSubmit.setOnClickListener {
            val title = viewModel.page.value?.title
            if(title != null) viewModel.updatePage(title, binding.editContent.text.toString())
        }

        viewModel.page.observe(viewLifecycleOwner) { page ->
            activityViewModel.updateTitle(page.title)
            binding.editContent.setText(page.source)
        }

        viewModel.result.observe(viewLifecycleOwner) { content ->
            Snackbar.make(view, content.message, Snackbar.LENGTH_LONG).show()
            val navController = findNavController()
            navController.navigateUp()
            navController.popBackStack()
            val args = Bundle().apply { putString("title", arguments?.getString("title")) }
            navController.navigate(R.id.action_PageViewFragment_self_noanim, args)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}