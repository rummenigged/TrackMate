package com.octopus.edu.core.domain.utils

import com.octopus.edu.core.domain.model.common.ErrorType

interface ErrorClassifier {
    /**
 * Maps a throwable to an error category for higher-level handling.
 *
 * @return An ErrorType representing either a transient or permanent error that wraps the input throwable.
 */
fun classify(throwable: Throwable): ErrorType
}

abstract class BaseErrorClassifier : ErrorClassifier {
    /**
 * Determines whether the given throwable represents a transient error.
 *
 * Subclasses may override to classify specific throwables as transient.
 *
 * @param throwable The throwable to evaluate.
 * @return `true` if the throwable should be treated as a transient error, `false` otherwise.
 */
protected open fun isTransient(throwable: Throwable): Boolean = false

    /**
         * Classifies a Throwable as a transient or permanent error.
         *
         * Maps the provided throwable to `ErrorType.TransientError` when it represents a transient condition; otherwise maps it to `ErrorType.PermanentError`.
         *
         * @param throwable The throwable to classify.
         * @return `ErrorType.TransientError(throwable)` if the error is transient, `ErrorType.PermanentError(throwable)` otherwise.
         */
        override fun classify(throwable: Throwable): ErrorType =
        if (isTransient(throwable)) {
            ErrorType.TransientError(throwable)
        } else {
            ErrorType.PermanentError(throwable)
        }
}