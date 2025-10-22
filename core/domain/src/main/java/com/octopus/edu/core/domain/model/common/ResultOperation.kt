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

/**
 * Executes the provided suspendable side-effect when this ResultOperation is a Success.
 *
 * @param block Suspendable action invoked with the contained success value.
 * @return The original ResultOperation unchanged.
 */
suspend inline fun <T : Any> ResultOperation<T>.doOnSuccess(block: suspend (T) -> Unit): ResultOperation<T> {
    if (this is ResultOperation.Success) block(data)
    return this
}

/**
 * Invokes the given suspend block if this ResultOperation is an Error.
 *
 * The block is called with the error's throwable and its `isRetriable` flag; the receiver is returned unchanged.
 *
 * @param block A suspend function that receives the error's Throwable and a Boolean indicating whether the error is retriable.
 * @return The original ResultOperation instance.
 */
suspend inline fun <T : Any> ResultOperation<T>.doOnError(block: suspend (Throwable, Boolean) -> Unit): ResultOperation<T> {
    if (this is ResultOperation.Error) block(throwable, isRetriable)
    return this
}

/**
     * Suppresses consecutive retriable `ResultOperation.Error` emissions up to a configured limit before emitting results downstream.
     *
     * The returned flow collects from the source and increments an internal retry counter each time it encounters a `ResultOperation.Error` with `isRetriable == true`; those errors are not emitted while the counter is less than `maxRetries`. Any non-retriable result or an error encountered after the retry limit is emitted to downstream collectors.
     *
     * @param maxRetries Maximum number of retriable errors to skip before emitting them. Defaults to 3.
     * @return A flow that emits the original `ResultOperation` values, with up to `maxRetries` retriable errors suppressed.
     */
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