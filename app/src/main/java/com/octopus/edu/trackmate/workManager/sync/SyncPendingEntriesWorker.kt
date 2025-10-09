package com.octopus.edu.trackmate.workManager.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.repository.EntryRepository
import com.octopus.edu.core.domain.useCase.SyncPendingEntryUseCase
import com.octopus.edu.core.domain.useCase.SyncResult
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
                is ResultOperation.Error -> Result.retry()
                is ResultOperation.Success -> {
                    result.data.forEach { entry ->
                        when (syncPendingEntryUseCase(entry.id)) {
                            SyncResult.Success -> {}
                            SyncResult.PermanentError -> sawPermanentError = true
                            SyncResult.TransientError -> sawTransientError = true
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
