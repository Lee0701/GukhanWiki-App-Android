package io.github.lee0701.gukhanwiki.android

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.preference.PreferenceManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import io.github.lee0701.gukhanwiki.android.auth.AccountHelper
import io.github.lee0701.gukhanwiki.android.auth.SwitchAccountBottomSheet
import io.github.lee0701.gukhanwiki.android.databinding.ActivityMainBinding
import io.github.lee0701.gukhanwiki.android.history.LastViewedPage
import io.github.lee0701.gukhanwiki.android.view.edit.EditPageFragment
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private lateinit var preference: SharedPreferences

    private var selectedAccountIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preference = PreferenceManager.getDefaultSharedPreferences(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController
        val listener = AppBarConfiguration.OnNavigateUpListener { navController.navigateUp() }
        appBarConfiguration = AppBarConfiguration.Builder().setFallbackOnNavigateUpListener(listener).build()
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, _, _ ->
            val title = navController.currentBackStackEntry?.arguments?.getString("title")
            if (title == null || title == GukhanWikiApi.MAIN_PAGE_TITLE) binding.toolbar.navigationIcon = null
            else binding.toolbar.navigationIcon =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.baseline_home_24)
        }

        viewModel.title.observe(this) { title ->
            this.supportActionBar?.title = title
        }

        viewModel.tempTitle.observe(this) { tempTitle ->
            if(tempTitle != null) this.supportActionBar?.title = tempTitle
            else this.supportActionBar?.title = viewModel.title.value
        }

        viewModel.snackbarMessage.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.signedInAccount.observe(this) { result ->
            when(result) {
                is Result.Loading -> {}
                is Result.Error -> {
                    val msg = resources.getString(R.string.msg_signin_error, result.exception.message)
                    viewModel.showSnackbar(msg)
                }
                is Result.Loaded -> {
                    if(result.data == null) {
                        val msg = resources.getString(R.string.msg_signout_success)
                        viewModel.showSnackbar(msg)
                    } else {
                        val msg = resources.getString(R.string.msg_signin_success, result.data.username)
                        viewModel.showSnackbar(msg)
                    }
                }
            }
        }

        selectedAccountIndex = preference.getInt("account_last_used", selectedAccountIndex)
        val account = AccountHelper.getAccounts()?.getOrNull(selectedAccountIndex)
        val password = account?.let { AccountHelper.getPassword(it) }
        if(account != null && password != null) {
            viewModel.signIn(account.name, password)
        }

        val action = intent?.action
        val path = intent?.data?.path
        if(action != null && path != null) {
            val decoded = GukhanWikiApi.decodeUriComponent(path).removePrefix(GukhanWikiApi.DOC_PATH)
            val args = Bundle().apply { putString("title", decoded) }
            navController.navigate(R.id.action_global_ViewPageFragment_clearStack, args)
            intent?.action = null
            intent?.data = null
        } else {
            val startpageClosed = viewModel.startpageClosed.value ?: false
            if(!preference.getBoolean("startpage_hide", false) && !startpageClosed) {
                startActivity(Intent(this, StartActivity::class.java))
                viewModel.setStartpageClosed()
            }

            if(preference.getBoolean("history_reload_last_page", true)) {
                val file = File(filesDir, LastViewedPage.FILENAME)
                try {
                    val title = if(file.exists()) file.readBytes().decodeToString() else null
                    val currentTitle = viewModel.title.value
                    if(title != null && title != currentTitle) {
                        val args = Bundle().apply {
                            putString("title", title)
                        }
                        navController.navigate(R.id.action_global_ViewPageFragment_clearStack, args)
                    }
                } catch(ex: IOException) {
                    ex.printStackTrace()
                }
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return when (item.itemId) {
            android.R.id.home -> {
                if(navController.currentDestination?.id == R.id.editPageFragment) {
                    EditPageFragment.showAlertDialog(this) {
                        navController.navigate(R.id.action_global_ViewPageFragment_clearStack)
                    }
                    true
                } else {
                    navController.navigate(R.id.action_global_ViewPageFragment_clearStack)
                    true
                }
            }
            R.id.action_search -> {
                if(navController.currentDestination?.id == R.id.searchFragment) {
                    navController.navigateUp()
                } else {
                    navController.navigate(R.id.action_global_searchFragment)
                }
                true
            }
            R.id.action_accounts -> {
                val bottomSheet = SwitchAccountBottomSheet { index, account, type ->
                    if(type == 0) {
                        if(index == -1) {
                            viewModel.signOut()
                        } else if(account != null) {
                            val password = AccountHelper.getPassword(account)
                            if(password != null) viewModel.signIn(account.name, password)
                        }
                        selectedAccountIndex = index
                        preference.edit().putInt("account_last_used", selectedAccountIndex).apply()
                    } else if(type == 1) {
                        val navController = findNavController(R.id.nav_host_fragment_content_main)
                        val args = Bundle().apply {
                            putString("title", "User:${account?.name}")
                        }
                        navController.navigate(R.id.action_global_ViewPageFragment, args)
                    }
                }
                bottomSheet.adapter.submitList(AccountHelper.getAccounts())
                bottomSheet.adapter.selectedIndex = selectedAccountIndex
                bottomSheet.show(supportFragmentManager, SwitchAccountBottomSheet.TAG)
                true
            }
            R.id.action_share -> {
                val url = viewModel.url.value
                if(url != null) {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, url)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    startActivity(shareIntent)
                }
                true
            }
            R.id.action_recent_changes -> {
                val args = Bundle().apply {
                    putString("title", "Special:RecentChanges")
                }
                navController.navigate(R.id.action_global_ViewPageFragment, args)
                true
            }
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
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

    fun setAppbarShown(shown: Boolean) {
        val params = binding.contentWrapper.layoutParams as CoordinatorLayout.LayoutParams
        if(shown) {
            binding.appbar.visibility = View.VISIBLE
            params.behavior = AppBarLayout.ScrollingViewBehavior()
        } else {
            binding.appbar.visibility = View.GONE
            params.behavior = null
        }
        binding.contentWrapper.requestLayout()
    }

    override fun onPause() {
        super.onPause()
        val title = viewModel.title.value
        if(title != null) {
            val file = File(filesDir, LastViewedPage.FILENAME)
            try {
                if(preference.getBoolean("history_reload_last_page", true)) {
                    file.writeBytes(title.encodeToByteArray())
                } else {
                    file.delete()
                }
            } catch(ex: IOException) {
                ex.printStackTrace()
            }
        }
    }

}