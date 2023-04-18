package io.github.lee0701.gukhanwiki.android

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

class SettingsActivity: AppCompatActivity(), OnSharedPreferenceChangeListener {

    private val preference: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
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
            restartApp()
        }
    }

    private fun restartApp() {
        val intent = applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
        val mainIntent = Intent.makeRestartActivityTask(intent?.component)
        applicationContext.startActivity(mainIntent)
        finishAffinity()
        Runtime.getRuntime().exit(0)
    }

    override fun onDestroy() {
        super.onDestroy()
        preference.unregisterOnSharedPreferenceChangeListener(this)
    }
}