package io.github.lee0701.gukhanwiki.android.view

sealed interface PageContent {
    data class Loaded(
        val text: String,
    ): PageContent
    object Loading: PageContent
    data class Error(
        val message: String?,
    ): PageContent
}