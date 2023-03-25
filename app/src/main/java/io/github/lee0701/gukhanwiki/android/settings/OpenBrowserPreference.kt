package io.github.lee0701.gukhanwiki.android.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import androidx.preference.Preference
import io.github.lee0701.gukhanwiki.android.R

class OpenBrowserPreference(
    context: Context,
    attributeSet: AttributeSet,
): Preference(context, attributeSet, R.style.Theme_GukhanWikiAppAndroid) {

    private val uri: String = attributeSet.getAttributeValue(null, "uri")

    override fun onClick() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        context.startActivity(intent)
        super.onClick()
    }

}