package com.octopus.edu.core.domain.model.common

sealed interface SyncResult {
    object Success : SyncResult

    class Error(
        val type: ErrorType
    ) : SyncResult
}
