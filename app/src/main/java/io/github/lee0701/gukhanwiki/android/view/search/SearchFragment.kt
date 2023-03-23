package io.github.lee0701.gukhanwiki.android.view.search

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
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

    private var binding: FragmentSearchBinding? = null
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
        val binding = FragmentSearchBinding.inflate(inflater, container, false)
        this.binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = binding ?: return
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager

        binding.autocompleteList.apply {
            this.adapter = this@SearchFragment.adapter
            this.layoutManager = LinearLayoutManager(context)
            this.addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
        }

        binding.searchInput.addTextChangedListener { editable ->
            val text = editable?.toString() ?: return@addTextChangedListener
            if(text.isBlank()) viewModel.clearAutocomplete()
            else viewModel.autocompleteSearch(text)
        }

        fun gotoSearch(text: String) {
            if(text.isBlank()) return
            val args = Bundle().apply {
                putString("query", text)
            }
            findNavController().navigate(R.id.action_searchFragment_to_searchResultFragment, args)
        }

        binding.searchInput.setOnEditorActionListener { _, _, _ ->
            gotoSearch(binding.searchInput.text.toString())
            true
        }

        binding.searchInput.setOnKeyListener { _, keyCode, _ ->
            if(keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                gotoSearch(binding.searchInput.text.toString())
                true
            } else false
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
        when(item.action) {
            SearchAutocompleteItem.Action.GOTO -> {
                val args = Bundle().apply {
                    putString("title", item.title)
                }
                findNavController().navigate(R.id.action_searchFragment_to_ViewPageFragment, args)
            }
            SearchAutocompleteItem.Action.NEW -> {
                val args = Bundle().apply {
                    putString("title", item.title)
                }
                findNavController().navigate(R.id.action_searchFragment_to_editPageFragment, args)
            }
            else -> {
                viewModel.autocompleteSelected(item.title)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}