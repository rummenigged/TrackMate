package com.octopus.edu.core.domain.utils

import com.octopus.edu.core.domain.model.common.ErrorType

interface RetryPolicy {
    suspend fun shouldRetry(
        errorType: ErrorType,
        attempt: Long
    ): Boolean
}
