package io.github.lee0701.gukhanwiki.android.settings

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import androidx.preference.Preference
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import io.github.lee0701.gukhanwiki.android.R

class OssLicensesMenuPreference(
    context: Context,
    attributeSet: AttributeSet,
): Preference(context, attributeSet, R.style.Theme_GukhanWikiAppAndroid) {
    override fun onClick() {
        context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
        super.onClick()
    }
}