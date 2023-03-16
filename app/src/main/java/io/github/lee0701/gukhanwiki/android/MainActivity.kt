package io.github.lee0701.gukhanwiki.android

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import io.github.lee0701.gukhanwiki.android.databinding.ActivityMainBinding
import io.github.lee0701.gukhanwiki.android.view.MainViewModel
import io.github.lee0701.gukhanwiki.android.view.SearchAutocompleteAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    private val adapter = SearchAutocompleteAdapter { _, item -> viewModel.updateTitle(title = item.title) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        binding.searchInput.addTextChangedListener { editable ->
            val text = editable?.toString() ?: return@addTextChangedListener
            if(text.isBlank()) return@addTextChangedListener
            viewModel.autocompleteSearch(text)
        }


        binding.searchAutocomplete.recyclerView.apply {
            this.adapter = this@MainActivity.adapter
            this.layoutManager = LinearLayoutManager(context)
            this.addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
        }

        binding.searchCancel.setOnClickListener {
            setSearchWindowVisibility(false)
        }

        viewModel.title.observe(this) { title ->
            this.title = title
        }

        viewModel.autocompleteResult.observe(this) { list ->
            adapter.submitList(list)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            R.id.action_search -> {
                setSearchWindowVisibility(true)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun setSearchWindowVisibility(visibility: Boolean) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val visible = if(visibility) View.VISIBLE else View.INVISIBLE
        val invisible = if(!visibility) View.VISIBLE else View.INVISIBLE
        binding.searchInput.visibility = visible
        binding.searchAutocomplete.root.visibility = visible
        binding.toolbar.visibility = invisible
        binding.content.root.visibility = invisible
        if(visibility) {
            binding.searchInput.requestFocus()
            imm.showSoftInput(binding.searchInput, InputMethodManager.SHOW_IMPLICIT)
        } else {
            binding.searchInput.text?.clear()
            binding.searchInput.clearFocus()
            imm.hideSoftInputFromWindow(binding.searchInput.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
            adapter.submitList(emptyList())
        }
    }

}