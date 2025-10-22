package com.octopus.edu.core.domain.utils

import com.octopus.edu.core.domain.model.common.ErrorType

interface RetryPolicy {
    /**
     * Determines whether another retry should be attempted for the given error and attempt count.
     *
     * @param errorType The type of error that occurred.
     * @param attempt The current retry attempt count.
     * @return `true` if another retry should be attempted, `false` otherwise.
     */
    suspend fun shouldRetry(
        errorType: ErrorType,
        attempt: Long
    ): Boolean
}