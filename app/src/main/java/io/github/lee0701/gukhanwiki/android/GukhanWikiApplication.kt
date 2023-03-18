package io.github.lee0701.gukhanwiki.android

import androidx.multidex.MultiDexApplication
import io.github.lee0701.gukhanwiki.android.auth.AccountHelper

class GukhanWikiApplication: MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        AccountHelper.initAccountManager(this)
    }
}