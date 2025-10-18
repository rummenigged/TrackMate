package com.octopus.edu.core.common

import com.octopus.edu.core.domain.model.common.ErrorType
import com.octopus.edu.core.domain.utils.RetryPolicy
import kotlinx.coroutines.delay
import kotlin.math.min

class ExponentialBackoffPolicy(
    private val initialDelay: Long,
    private val maxDelay: Long
) : RetryPolicy {
    override suspend fun shouldRetry(
        errorType: ErrorType,
        attempt: Long
    ): Boolean {
        if (errorType is ErrorType.PermanentError) return false
        val delayMs = min(initialDelay * (attempt + 1), maxDelay)
        delay(delayMs)
        return true
    }

    companion object {
        const val DEFAULT_INITIAL_DELAY = 2000L
        const val DEFAULT_MAX_DELAY = 5 * 60 * 1000L
    }
}
