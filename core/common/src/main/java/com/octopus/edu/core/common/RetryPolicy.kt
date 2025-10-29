package com.octopus.edu.core.common

import com.octopus.edu.core.domain.model.common.ErrorType

interface RetryPolicy {
    suspend fun shouldRetry(
        errorType: ErrorType,
        attempt: Long
    ): Boolean
}
