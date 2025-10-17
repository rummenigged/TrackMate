package com.octopus.edu.core.domain.model.common

sealed interface ErrorType {
    class TransientError(
        val cause: Throwable
    ) : ErrorType

    class PermanentError(
        val cause: Throwable
    ) : ErrorType
}
