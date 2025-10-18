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
import com.octopus.edu.core.domain.repository.EntryRepository
import com.octopus.edu.core.domain.useCase.SyncPendingEntryUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncPendingEntriesWorker
    @AssistedInject
    constructor(
        @Assisted val context: Context,
        @Assisted val params: WorkerParameters,
        private val entryRepository: EntryRepository,
        private val syncPendingEntryUseCase: SyncPendingEntryUseCase
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            var sawTransientError = false
            var sawPermanentError = false
            return when (val result = entryRepository.getPendingEntries()) {
                is ResultOperation.Error -> {
                    if (result.isRetriable) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
                is ResultOperation.Success -> {
//                     TODO: Implement a controlled concurrency to sync entries
                    result.data.forEach { entry ->
                        when (val result = syncPendingEntryUseCase(entry.id)) {
                            Success -> {}
                            is Error -> {
                                when (result.type) {
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
