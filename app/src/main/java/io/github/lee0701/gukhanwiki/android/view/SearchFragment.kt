package io.github.lee0701.gukhanwiki.android.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.lee0701.gukhanwiki.android.R
import io.github.lee0701.gukhanwiki.android.databinding.FragmentSearchBinding

class SearchFragment: Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModels()

    private val adapter = SearchAutocompleteAdapter { position, item -> onAutocompleteClicked(position, item) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager

        binding.autocompleteList.apply {
            this.adapter = this@SearchFragment.adapter
            this.layoutManager = LinearLayoutManager(context)
            this.addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
        }

        binding.searchInput.addTextChangedListener { editable ->
            val text = editable?.toString() ?: return@addTextChangedListener
            if(text.isBlank()) return@addTextChangedListener
            viewModel.autocompleteSearch(text)
        }

        fun gotoSearch(text: String) {
            if(text.isBlank()) return
            val args = Bundle().apply {
                putString("query", text)
            }
            findNavController().navigate(R.id.action_searchFragment_to_searchResultFragment, args)
        }

        binding.searchInput.setOnEditorActionListener { v, id, event ->
            gotoSearch(v.text?.toString() ?: return@setOnEditorActionListener false)
            true
        }

        binding.searchButton.setOnClickListener {
            gotoSearch(binding.searchInput.text.toString())
        }

        binding.searchClear.setOnClickListener {
            binding.searchInput.setText("")
            binding.searchInput.requestFocus()
            imm?.showSoftInput(binding.searchInput, 0)
        }

        viewModel.title.observe(viewLifecycleOwner) { title ->
            binding.searchInput.setText(title)
            binding.searchInput.setSelection(title.length)
        }

        viewModel.autocompleteResult.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        binding.searchInput.requestFocus()
        imm?.showSoftInput(binding.searchInput, 0)
    }

    private fun onAutocompleteClicked(position: Int, item: SearchAutocompleteItem) {
        viewModel.autocompleteSelected(item.title)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}