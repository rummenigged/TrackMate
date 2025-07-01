package com.octopus.edu.core.domain.model.common

sealed class ResultOperation<out T : Any> {
    data class Success<out T : Any>(
        val data: T,
    ) : ResultOperation<T>()

    data class Error(
        val exception: Exception,
    ) : ResultOperation<Nothing>()
}
