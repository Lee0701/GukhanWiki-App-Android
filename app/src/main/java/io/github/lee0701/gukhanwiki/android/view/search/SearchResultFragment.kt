package io.github.lee0701.gukhanwiki.android.view.search

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
import io.github.lee0701.gukhanwiki.android.Result
import io.github.lee0701.gukhanwiki.android.R
import io.github.lee0701.gukhanwiki.android.databinding.FragmentSearchResultBinding

class SearchResultFragment: Fragment() {

    private var binding: FragmentSearchResultBinding? = null
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
        val binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        this.binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = binding ?: return

        binding.recyclerView.apply {
            this.adapter = this@SearchResultFragment.adapter
            this.layoutManager = LinearLayoutManager(context)
            this.addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
        }

        viewModel.searchResult.observe(viewLifecycleOwner) { result ->
            binding.loadingIndicator.root.visibility = View.GONE
            binding.errorIndicator.root.visibility = View.GONE
            binding.recyclerView.visibility = View.GONE
            when(result) {
                is Result.Loading -> {
                    binding.loadingIndicator.root.visibility = View.VISIBLE
                }
                is Result.Error -> {
                    binding.errorIndicator.root.visibility = View.VISIBLE
                    binding.errorIndicator.text.text = result.exception.message
                }
                is Result.Loaded -> {
                    binding.recyclerView.visibility = View.VISIBLE
                    adapter.submitList(result.data)
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun onItemClicked(position: Int, item: SearchResultItem) {
        val navController = findNavController()
        val args = Bundle().apply {
            putString("title", item.title)
        }
        navController.navigate(R.id.action_searchResultFragment_to_ViewPageFragment, args)
    }
}