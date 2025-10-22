package com.octopus.edu.trackmate.workManager.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.octopus.edu.core.domain.model.common.ErrorType.PermanentError
import com.octopus.edu.core.domain.model.common.ErrorType.TransientError
import com.octopus.edu.core.domain.model.common.SyncResult
import com.octopus.edu.core.domain.model.common.SyncResult.Success
import com.octopus.edu.core.domain.useCase.SyncPendingEntryUseCase
import com.octopus.edu.trackmate.sync.EntrySyncWorkScheduler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncEntryWorker
    @AssistedInject
    constructor(
        @Assisted val context: Context,
        @Assisted val params: WorkerParameters,
        private val syncPendingEntryUseCase: SyncPendingEntryUseCase
    ) : CoroutineWorker(context, params) {
        /**
         * Synchronizes a single entry whose ID is read from the worker's inputData and maps the sync outcome to a WorkManager result.
         *
         * @return `Result.success()` if synchronization succeeded, `Result.failure()` if the entry ID is missing or a permanent error occurred, and `Result.retry()` if a transient error occurred.
         */
        override suspend fun doWork(): Result {
            val id = inputData.getString(EntrySyncWorkScheduler.ENTRY_ID_EXTRA) ?: return Result.failure()

            return when (val result = syncPendingEntryUseCase(id)) {
                Success -> Result.success()
                is SyncResult.Error -> {
                    when (result.type) {
                        is PermanentError -> Result.failure()
                        is TransientError -> Result.retry()
                    }
                }
            }
        }
    }