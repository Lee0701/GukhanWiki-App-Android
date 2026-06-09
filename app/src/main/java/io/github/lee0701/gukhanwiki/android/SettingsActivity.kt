package io.github.lee0701.gukhanwiki.android

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import io.github.lee0701.gukhanwiki.android.databinding.ActivitySettingsBinding

class SettingsActivity: AppCompatActivity(), OnSharedPreferenceChangeListener {

    private val preference: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        val binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            binding.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = statusBarHeight }
            insets
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
        preference.registerOnSharedPreferenceChangeListener(this)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("ApplySharedPref")
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val displayLocaleKey = "display_locale"
        if(key == displayLocaleKey) {
            if(sharedPreferences != null) {
                val value = sharedPreferences.getString(key, "ko-Kore-KR")
                sharedPreferences.edit().putString(displayLocaleKey, value).commit()
            }
            GukhanWikiApplication.restart(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        preference.unregisterOnSharedPreferenceChangeListener(this)
    }
}