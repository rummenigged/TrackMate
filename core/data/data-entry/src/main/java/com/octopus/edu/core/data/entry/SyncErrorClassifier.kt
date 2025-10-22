package com.octopus.edu.core.data.entry

import com.octopus.edu.core.data.entry.di.DatabaseErrorClassifierQualifier
import com.octopus.edu.core.data.entry.di.NetworkErrorClassifierQualifier
import com.octopus.edu.core.domain.model.common.ErrorType.TransientError
import com.octopus.edu.core.domain.utils.BaseErrorClassifier
import com.octopus.edu.core.domain.utils.ErrorClassifier

class SyncErrorClassifier(
    @field:DatabaseErrorClassifierQualifier
    private val databaseErrorClassifier: ErrorClassifier,
    @field:NetworkErrorClassifierQualifier
    private val networkErrorClassifier: ErrorClassifier
) : BaseErrorClassifier() {
    /**
             * Determines whether the given throwable represents a transient error for synchronization.
             *
             * @param throwable The error to classify.
             * @return `true` if either the database or network classifier identifies the throwable as a `TransientError`, `false` otherwise.
             */
            override fun isTransient(throwable: Throwable): Boolean =
        databaseErrorClassifier.classify(throwable) is TransientError ||
            networkErrorClassifier.classify(throwable) is TransientError
}