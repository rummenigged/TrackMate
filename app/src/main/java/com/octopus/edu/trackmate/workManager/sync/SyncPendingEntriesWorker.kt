package com.octopus.edu.trackmate.workManager.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.octopus.edu.core.domain.model.common.ErrorType.PermanentError
import com.octopus.edu.core.domain.model.common.ErrorType.TransientError
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.model.common.SyncResult.Error
import com.octopus.edu.core.domain.model.common.SyncResult.Success
import com.octopus.edu.core.domain.repository.EntrySyncRepository
import com.octopus.edu.core.domain.useCase.SyncPendingEntryUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncPendingEntriesWorker
    @AssistedInject
    constructor(
        @Assisted val context: Context,
        @Assisted val params: WorkerParameters,
        private val syncRepository: EntrySyncRepository,
        private val syncPendingEntryUseCase: SyncPendingEntryUseCase
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            var sawTransientError = false
            var sawPermanentError = false
            return when (val result = syncRepository.getPendingEntries()) {
                is ResultOperation.Error -> {
                    if (result.isRetriable) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
                is ResultOperation.Success -> {
//                     TODO: Implement a controlled concurrency to sync entries
                    for (entry in result.data) {
                        when (val res = syncPendingEntryUseCase(entry.id)) {
                            Success -> {}
                            is Error -> {
                                when (res.type) {
                                    is PermanentError -> sawPermanentError = true
                                    is TransientError -> sawTransientError = true
                                }
                            }
                        }
                    }
                    return when {
                        sawTransientError -> Result.retry()
                        sawPermanentError -> Result.failure()
                        else -> Result.success()
                    }
                }
            }
        }
    }
