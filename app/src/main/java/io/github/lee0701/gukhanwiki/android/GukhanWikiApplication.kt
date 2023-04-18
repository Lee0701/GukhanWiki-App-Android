package io.github.lee0701.gukhanwiki.android

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.multidex.MultiDexApplication
import androidx.preference.PreferenceManager
import io.github.lee0701.gukhanwiki.android.auth.AccountHelper

class GukhanWikiApplication: MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        val preference = PreferenceManager.getDefaultSharedPreferences(this)
        AccountHelper.initAccountManager(this)

        val localeCode = preference.getString("display_locale", "ko-Kore-KR")
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(localeCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}