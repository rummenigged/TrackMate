package com.octopus.edu.core.data.entry

import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreException.Code.DEADLINE_EXCEEDED
import com.google.firebase.firestore.FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED
import com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE
import com.octopus.edu.core.domain.utils.BaseErrorClassifier
import java.io.IOException

class NetworkErrorClassifier : BaseErrorClassifier() {
    override fun isTransient(throwable: Throwable): Boolean =
        throwable is IOException ||
            throwable is FirebaseFirestoreException && throwable.isRetryable()
}

private fun FirebaseFirestoreException.isRetryable() =
    code in
        setOf(
            UNAVAILABLE,
            DEADLINE_EXCEEDED,
            RESOURCE_EXHAUSTED,
        )
