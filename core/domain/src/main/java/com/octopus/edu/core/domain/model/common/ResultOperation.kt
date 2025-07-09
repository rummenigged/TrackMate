package com.octopus.edu.core.domain.model.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

sealed class ResultOperation<out T : Any> {
    data class Success<out T : Any>(
        val data: T,
    ) : ResultOperation<T>()

    data class Error(
        val throwable: Throwable,
        val isRetriable: Boolean = false,
    ) : ResultOperation<Nothing>()
}

fun <T : Any> Flow<ResultOperation<T>>.retryOnResultError(maxRetries: Int = 3): Flow<ResultOperation<T>> =
    flow {
        var retryCount = 0
        collect { result ->
            if (result is ResultOperation.Error && result.isRetriable && retryCount < maxRetries) {
                retryCount++
            } else {
                emit(result)
            }
        }
    }
