package io.github.lee0701.gukhanwiki.android.api.rest

data class Thumbnail(
    val mimetype: String,
    val size: Int?,
    val width: Int?,
    val height: Int?,
    val duration: Int?,
    val url: String,
)