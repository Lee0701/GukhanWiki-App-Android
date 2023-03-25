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
import io.github.lee0701.gukhanwiki.android.view.edit.EditPageFragment

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
            val hasTitle =
                navController.currentBackStackEntry?.arguments?.getString("title") != null
            if (!hasTitle) binding.toolbar.navigationIcon = null
            else binding.toolbar.navigationIcon =
                ContextCompat.getDrawable(this@MainActivity, R.drawable.baseline_arrow_back_24)
        }

        viewModel.title.observe(this) { title ->
            this.supportActionBar?.title = title
        }

        viewModel.snackbarMessage.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.signInResult.observe(this) { result ->
            when(result) {
                is Loadable.Loading -> {}
                is Loadable.Error -> {
                    val msg = resources.getString(R.string.msg_signin_error, result.exception.message)
                    viewModel.showSnackbar(msg)
                }
                is Loadable.Loaded -> {
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
        } else {
            val startpageClosed = viewModel.startpageClosed.value ?: false
            if(!preference.getBoolean("startpage_hide", false) && !startpageClosed) {
                startActivity(Intent(this, StartActivity::class.java))
                viewModel.setStartpageClosed()
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
                        navController.navigateUp()
                    }
                    true
                } else {
                    super.onOptionsItemSelected(item)
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
                val bottomSheet = SwitchAccountBottomSheet { index, account ->
                    if(index == -1) {
                        viewModel.signOut()
                    } else if(account != null) {
                        val password = AccountHelper.getPassword(account)
                        if(password != null) viewModel.signIn(account.name, password)
                    }
                    selectedAccountIndex = index
                    preference.edit().putInt("account_last_used", selectedAccountIndex).apply()
                }
                bottomSheet.adapter.submitList(AccountHelper.getAccounts())
                bottomSheet.adapter.selectedIndex = selectedAccountIndex
                bottomSheet.show(supportFragmentManager, SwitchAccountBottomSheet.TAG)
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

}