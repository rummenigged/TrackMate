package com.octopus.edu.core.data.entry

import com.octopus.edu.core.data.entry.di.DatabaseErrorClassifierQualifier
import com.octopus.edu.core.data.entry.di.NetworkErrorClassifierQualifier
import com.octopus.edu.core.domain.model.common.ErrorType.TransientError
import com.octopus.edu.core.domain.utils.BaseErrorClassifier
import com.octopus.edu.core.domain.utils.ErrorClassifier
import javax.inject.Inject

class SyncErrorClassifier
    @Inject
    constructor(
        @field:DatabaseErrorClassifierQualifier
        private val databaseErrorClassifier: ErrorClassifier,
        @field:NetworkErrorClassifierQualifier
        private val networkErrorClassifier: ErrorClassifier
    ) : BaseErrorClassifier() {
        override fun isTransient(throwable: Throwable): Boolean =
            databaseErrorClassifier.classify(throwable) is TransientError ||
                networkErrorClassifier.classify(throwable) is TransientError
    }
