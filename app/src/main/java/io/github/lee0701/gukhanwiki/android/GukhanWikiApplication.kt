package io.github.lee0701.gukhanwiki.android

import androidx.multidex.MultiDexApplication

class GukhanWikiApplication: MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        AccountHelper.initAccountManager(this)
    }
}