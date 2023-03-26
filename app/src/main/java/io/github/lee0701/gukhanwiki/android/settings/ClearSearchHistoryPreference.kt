package io.github.lee0701.gukhanwiki.android.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.lee0701.gukhanwiki.android.R
import io.github.lee0701.gukhanwiki.android.history.SearchHistory
import java.io.File

class ClearSearchHistoryPreference(
    context: Context,
    attributeSet: AttributeSet,
): Preference(context, attributeSet, R.style.Theme_GukhanWikiAppAndroid) {

    override fun onClick() {
        MaterialAlertDialogBuilder(context)
            .setMessage(R.string.msg_confirm_clear_search_history)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                val file = File(context.filesDir, SearchHistory.FILENAME)
                file.delete()
            }.setNegativeButton(R.string.action_cancel) { _, _ -> }
            .show()
        super.onClick()
    }

}