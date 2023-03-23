package io.github.lee0701.gukhanwiki.android

sealed interface Loadable<T> {
    data class Loaded<T>(
        val data: T,
    ): Loadable<T>
    class Loading<T>: Loadable<T>
    data class Error<T>(
        val exception: Exception,
    ): Loadable<T>
}