package com.octopus.edu.core.domain.utils

import com.octopus.edu.core.domain.model.common.ResultOperation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

suspend fun <T : Any> safeCall(
    dispatcher: CoroutineDispatcher,
    onErrorReturn: (() -> T)? = null,
    isRetriableWhen: ((Throwable) -> Boolean)? = null,
    doOnError: (suspend (Throwable) -> Unit)? = null,
    block: suspend () -> T,
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
