package io.github.lee0701.gukhanwiki.android.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import io.github.lee0701.gukhanwiki.android.BuildConfig

class AppInfoPreference(
    context: Context,
    attributeSet: AttributeSet,
): Preference(context, attributeSet) {
    init {
        this.summary = BuildConfig.VERSION_NAME
    }
}