package io.github.lee0701.gukhanwiki.android.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import androidx.preference.Preference
import io.github.lee0701.gukhanwiki.android.R

class OpenStorePreference(
    context: Context,
    attributeSet: AttributeSet,
): Preference(context, attributeSet, R.style.Theme_GukhanWikiAppAndroid) {

    private val uri: String = attributeSet.getAttributeValue(null, "uri")

    override fun onClick() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(uri)
                setPackage("com.android.vending")
            }
            context.startActivity(intent)
        } catch(ex: ActivityNotFoundException) {
            val uri = Uri.parse("https://play.google.com/store/apps/developer?id=%E6%9D%8E%EF%BC%81")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        }
        super.onClick()
    }

}