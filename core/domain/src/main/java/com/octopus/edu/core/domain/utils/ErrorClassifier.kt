package com.octopus.edu.core.domain.utils

import com.octopus.edu.core.domain.model.common.ErrorType

interface ErrorClassifier {
    fun classify(throwable: Throwable): ErrorType
}

abstract class BaseErrorClassifier : ErrorClassifier {
    protected open fun isTransient(throwable: Throwable): Boolean = false

    override fun classify(throwable: Throwable): ErrorType =
        if (isTransient(throwable)) {
            ErrorType.TransientError(throwable)
        } else {
            ErrorType.PermanentError(throwable)
        }
}
