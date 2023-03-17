package io.github.lee0701.gukhanwiki.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.lee0701.gukhanwiki.android.Loadable
import io.github.lee0701.gukhanwiki.android.R
import io.github.lee0701.gukhanwiki.android.databinding.FragmentSearchResultBinding

class SearchResultFragment: Fragment() {

    private var _binding: FragmentSearchResultBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchResultViewModel by viewModels()

    private val adapter = SearchResultAdapter { position, item -> onItemClicked(position, item) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val query = arguments?.getString("query")
        if(query != null) viewModel.search(query)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.apply {
            this.adapter = this@SearchResultFragment.adapter
            this.layoutManager = LinearLayoutManager(context)
            this.addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
        }

        viewModel.searchResult.observe(viewLifecycleOwner) { result ->
            when(result) {
                is Loadable.Loading -> {}
                is Loadable.Error -> {}
                is Loadable.Loaded -> {
                    adapter.submitList(result.data)
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onItemClicked(position: Int, item: SearchResultItem) {
        val navController = findNavController()
        val args = Bundle().apply {
            putString("title", item.title)
        }
        navController.navigate(R.id.action_searchResultFragment_pop)
        navController.navigate(R.id.action_global_PageViewFragment, args)
    }
}