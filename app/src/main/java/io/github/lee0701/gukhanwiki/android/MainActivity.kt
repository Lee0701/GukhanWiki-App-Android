package io.github.lee0701.gukhanwiki.android

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import io.github.lee0701.gukhanwiki.android.auth.AccountHelper
import io.github.lee0701.gukhanwiki.android.auth.AuthenticationActivity
import io.github.lee0701.gukhanwiki.android.auth.SwitchAccountBottomSheet
import io.github.lee0701.gukhanwiki.android.databinding.ActivityMainBinding

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

        viewModel.message.observe(this) { msg ->
            if(msg != null) {
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
                viewModel.clearMessage()
            }
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        viewModel.title.observe(this) { title ->
            this.supportActionBar?.title = title
        }

        viewModel.signInResult.observe(this) { result ->
            when(result) {
                is Loadable.Loading -> {}
                is Loadable.Error -> {
                    val msg = resources.getString(R.string.msg_signin_error, result.exception.message)
                    viewModel.displayMessage(msg)
                }
                is Loadable.Loaded -> {
                    if(result.data == null) {
                        val msg = resources.getString(R.string.msg_signout_success)
                        viewModel.displayMessage(msg)
                    } else {
                        val msg = resources.getString(R.string.msg_signin_success, result.data.username)
                        viewModel.displayMessage(msg)
                    }
                }
            }
        }

        val action = intent?.action
        val path = intent?.data?.path
        if(action != null && path != null) {
            val decoded = GukhanWikiApi.decodeUriComponent(path).removePrefix(GukhanWikiApi.DOC_PATH)
            val args = Bundle().apply { putString("title", decoded) }
            navController.navigate(R.id.action_global_ViewPageFragment, args)
        }

        selectedAccountIndex = preference.getInt("account_last_used", selectedAccountIndex)
        val account = AccountHelper.getAccounts()?.getOrNull(selectedAccountIndex)
        val password = account?.let { AccountHelper.getPassword(it) }
        if(account != null && password != null) {
            viewModel.signIn(account.name, password)
        }

        if(!preference.getBoolean("startpage_hide", false)) {
            startActivity(Intent(this, StartActivity::class.java))
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
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_accounts -> {
                val bottomSheet = SwitchAccountBottomSheet({ index, account ->
                    if(account != null) {
                        val password = AccountHelper.getPassword(account)
                        if(password != null) viewModel.signIn(account.name, password)
                        selectedAccountIndex = index
                        preference.edit().putInt("account_last_used", selectedAccountIndex).apply()
                    }
                }, {
                    val intent = Intent(this, AuthenticationActivity::class.java)
                    startActivity(intent)
                })
                bottomSheet.adapter.submitList(AccountHelper.getAccounts())
                bottomSheet.adapter.selectedIndex = selectedAccountIndex
                bottomSheet.show(supportFragmentManager, SwitchAccountBottomSheet.TAG)
                true
            }
            R.id.action_search -> {
                if(navController.currentDestination?.id == R.id.searchFragment) {
                    navController.navigateUp()
                } else {
                    navController.navigate(R.id.action_global_searchFragment)
                }
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

}