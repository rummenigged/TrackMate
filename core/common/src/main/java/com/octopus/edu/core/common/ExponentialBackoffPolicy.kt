package com.octopus.edu.core.common

import com.octopus.edu.core.domain.model.common.ErrorType
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
        val exp = (attempt.coerceAtLeast(0)).coerceAtMost(30)
        val factor = 1L shl exp.toInt()
        val delayMs = min(initialDelay * factor, maxDelay)
        delay(delayMs)
        return true
    }

    companion object {
        const val DEFAULT_INITIAL_DELAY = 2000L
        const val DEFAULT_MAX_DELAY = 5 * 60 * 1000L
    }
}
