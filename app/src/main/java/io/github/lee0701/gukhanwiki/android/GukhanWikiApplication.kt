package io.github.lee0701.gukhanwiki.android

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.multidex.MultiDexApplication
import androidx.preference.PreferenceManager
import io.github.lee0701.gukhanwiki.android.auth.AccountHelper

class GukhanWikiApplication: MultiDexApplication() {
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        val preference = PreferenceManager.getDefaultSharedPreferences(this)
        AccountHelper.initAccountManager(this)

        // Needed because setApplicationLocales must be called after activity creation.
        handler.postDelayed({
            val localeCode = preference.getString("display_locale", "ko-Kore-KR")
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(localeCode)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }, 500)
    }

    companion object {
        fun restart(activity: Activity) {
            val applicationContext = activity.applicationContext
            val intent = applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
            val mainIntent = Intent.makeRestartActivityTask(intent?.component)
            applicationContext.startActivity(mainIntent)
            activity.finishAffinity()
            Runtime.getRuntime().exit(0)
        }
    }
}