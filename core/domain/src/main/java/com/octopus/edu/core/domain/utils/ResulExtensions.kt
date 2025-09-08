package com.octopus.edu.core.domain.utils

import com.octopus.edu.core.domain.model.common.ResultOperation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

suspend fun <T : Any> safeCall(
    dispatcher: CoroutineDispatcher,
    onErrorReturn: (() -> T)? = null,
    block: suspend () -> T,
): ResultOperation<T> =
    withContext(dispatcher) {
        try {
            ResultOperation.Success(block())
        } catch (e: Throwable) {
            if (onErrorReturn != null) {
                ResultOperation.Success(onErrorReturn())
            } else {
                ResultOperation.Error(e)
            }
        }
    }
