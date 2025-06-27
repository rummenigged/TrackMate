package com.octopus.edu.core.network.utils

sealed class NetworkResponse<out T : Any> {
    data class Success<out T : Any>(
        val data: T,
    ) : NetworkResponse<T>()

    data class Error(
        val exception: Throwable,
    ) : NetworkResponse<Nothing>()

    fun <R : Any> map(mapper: (T) -> R) =
        when (this) {
            is Success -> Success(mapper(data))
            is Error -> Error(exception)
        }

    suspend fun doOnSuccess(block: suspend (T) -> Unit): NetworkResponse<T> {
        if (this is Success) block(data)
        return this
    }
}
