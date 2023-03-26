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
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import io.github.lee0701.gukhanwiki.android.R
import io.github.lee0701.gukhanwiki.android.databinding.FragmentSearchBinding
import io.github.lee0701.gukhanwiki.android.history.SearchHistory
import java.io.File
import java.io.FileNotFoundException
import java.util.*

class SearchFragment: Fragment() {

    private var binding: FragmentSearchBinding? = null
    private val viewModel: SearchViewModel by viewModels()

    private var searchHistory: SearchHistory? = null
    private val searchHistoryFile: File by lazy { File(context?.filesDir, SearchHistory.FILENAME) }
    private val adapter = SearchAutocompleteAdapter { position, item -> onAutocompleteClicked(position, item) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        // Load search history from file, create one if not exists
        try {
            searchHistory = Gson().fromJson(JsonParser().parse(searchHistoryFile.bufferedReader()), SearchHistory::class.java)
        } catch(ex: JsonSyntaxException) {
            ex.printStackTrace()
            searchHistory = SearchHistory(listOf())
            searchHistoryFile.createNewFile()
        } catch(ex: FileNotFoundException) {
            searchHistory = SearchHistory(listOf())
            searchHistoryFile.createNewFile()
        }
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

        searchHistory?.let { viewModel.displayHistory(it) }

        binding.autocompleteList.apply {
            this.adapter = this@SearchFragment.adapter
            this.layoutManager = LinearLayoutManager(context)
            this.addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
        }

        binding.searchInput.addTextChangedListener { editable ->
            val text = editable?.toString() ?: return@addTextChangedListener
            if(text.isBlank()) {
                viewModel.clearAutocomplete()
                searchHistory?.let { viewModel.displayHistory(it) }
            } else {
                viewModel.autocompleteSearch(text)
            }
        }

        fun gotoSearch(text: String) {
            if(text.isBlank()) return
            appendSearchHistory(text)
            binding.searchInput.setText("")
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

        viewModel.autocompleteResult.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        binding.searchInput.requestFocus()
        imm?.showSoftInput(binding.searchInput, 0)
    }

    private fun onAutocompleteClicked(position: Int, item: SearchAutocompleteItem) {
        appendSearchHistory(item.title)
        binding?.searchInput?.setText("")
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
                binding?.searchInput?.setText(item.title)
                binding?.searchInput?.setSelection(item.title.length)
            }
        }
    }

    private fun appendSearchHistory(text: String) {
        searchHistory = searchHistory?.let { history ->
            val entries = history.entries.filter { it.title != text } + SearchHistory.Entry(text, Date())
            history.copy(entries = entries)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val entries = searchHistory?.entries ?: listOf()
        val searchHistory = searchHistory?.copy(entries = entries.sortedByDescending { it.date }.take(20))
        val json = Gson().toJson(searchHistory, SearchHistory::class.java)
        // If history file does not exist, leave it to clear
        if(searchHistoryFile.exists()) searchHistoryFile.writeBytes(json.toByteArray())
        binding = null
    }

}