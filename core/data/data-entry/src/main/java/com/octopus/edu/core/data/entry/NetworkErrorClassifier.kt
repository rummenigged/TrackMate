package com.octopus.edu.core.data.entry

import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreException.Code.DEADLINE_EXCEEDED
import com.google.firebase.firestore.FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED
import com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE
import com.octopus.edu.core.domain.utils.BaseErrorClassifier
import java.io.IOException

class NetworkErrorClassifier : BaseErrorClassifier() {
    /**
             * Determines whether the provided Throwable represents a transient (retryable) error.
             *
             * Treats IOExceptions and Firestore exceptions with codes `UNAVAILABLE`, `DEADLINE_EXCEEDED`, or `RESOURCE_EXHAUSTED` as transient.
             *
             * @param throwable The error to classify.
             * @return `true` if the error is considered transient and may be retried, `false` otherwise.
             */
            override fun isTransient(throwable: Throwable): Boolean =
        throwable is IOException ||
            throwable is FirebaseFirestoreException && throwable.isRetryable()
}

/**
         * Determines whether a Firestore exception's error code indicates a retryable (transient) condition.
         *
         * @return `true` if the exception's code is `UNAVAILABLE`, `DEADLINE_EXCEEDED`, or `RESOURCE_EXHAUSTED`, `false` otherwise.
         */
        private fun FirebaseFirestoreException.isRetryable() =
    code in
        setOf(
            UNAVAILABLE,
            DEADLINE_EXCEEDED,
            RESOURCE_EXHAUSTED,
        )