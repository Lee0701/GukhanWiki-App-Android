package io.github.lee0701.gukhanwiki.android

sealed interface Result<T> {
    data class Loaded<T>(
        val data: T,
    ): Result<T>
    class Loading<T>: Result<T>
    data class Error<T>(
        val exception: Throwable,
    ): Result<T>
}