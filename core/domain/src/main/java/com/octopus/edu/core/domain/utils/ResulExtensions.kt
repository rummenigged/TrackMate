package com.octopus.edu.core.domain.utils

import com.octopus.edu.core.domain.model.common.ResultOperation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext

/**
     * Run the provided suspending block on the given dispatcher and map its outcome to a ResultOperation.
     *
     * @param dispatcher Dispatcher used to execute the block.
     * @param onErrorReturn Optional supplier whose value will be returned as a successful result if the block throws.
     * @param isRetriableWhen Optional predicate that, when it returns `true` for a thrown Throwable, causes the returned `ResultOperation.Error` to be marked retriable.
     * @param doOnError Optional suspend callback invoked with the thrown Throwable when an error occurs (only invoked if `onErrorReturn` is not provided).
     * @param block The suspending CoroutineScope receiver block to execute.
     * @return `ResultOperation.Success` containing the block's result, or the value from `onErrorReturn()` if provided and the block throws. Otherwise returns `ResultOperation.Error` with the thrown `Throwable`; the error will be marked retriable if `isRetriableWhen` returns `true` for that Throwable.
     */
    suspend fun <T : Any> safeCall(
    dispatcher: CoroutineDispatcher,
    onErrorReturn: (() -> T)? = null,
    isRetriableWhen: ((Throwable) -> Boolean)? = null,
    doOnError: (suspend (Throwable) -> Unit)? = null,
    block: suspend CoroutineScope.() -> T,
): ResultOperation<T> =
    withContext(dispatcher) {
        try {
            ResultOperation.Success(block())
        } catch (e: Throwable) {
            if (onErrorReturn != null) {
                ResultOperation.Success(onErrorReturn())
            } else {
                doOnError?.invoke(e)
                if (isRetriableWhen?.invoke(e) == true) {
                    ResultOperation.Error(e, true)
                } else {
                    ResultOperation.Error(e)
                }
            }
        }
    }