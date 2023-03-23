package io.github.lee0701.gukhanwiki.android.view.search

import androidx.annotation.StringRes
import io.github.lee0701.gukhanwiki.android.R

data class SearchAutocompleteItem(
    val id: Int,
    val title: String,
    val action: Action? = null,
) {
    enum class Action(@StringRes val label: Int) {
        GOTO(R.string.action_goto),
        NEW(R.string.action_new_doc),
    }
}