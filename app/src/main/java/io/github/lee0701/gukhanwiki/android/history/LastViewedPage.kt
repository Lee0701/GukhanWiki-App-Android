package io.github.lee0701.gukhanwiki.android.history

data class LastViewedPage(
    val title: String,
) {
    companion object {
        const val FILENAME = "last-viewed-page.txt"
    }
}