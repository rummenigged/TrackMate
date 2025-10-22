package com.octopus.edu.trackmate.workManager.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.octopus.edu.core.domain.model.common.ErrorType.PermanentError
import com.octopus.edu.core.domain.model.common.ErrorType.TransientError
import com.octopus.edu.core.domain.model.common.SyncResult
import com.octopus.edu.core.domain.model.common.SyncResult.Success
import com.octopus.edu.core.domain.useCase.SyncDeletedEntryUseCase
import com.octopus.edu.trackmate.sync.EntrySyncWorkScheduler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncDeletedEntryWorker
    @AssistedInject
    constructor(
        @Assisted val context: Context,
        @Assisted val params: WorkerParameters,
        private val syncDeletedEntryUseCase: SyncDeletedEntryUseCase
    ) : CoroutineWorker(context, params) {
        /**
             * Syncs a deleted entry identified by the `ENTRY_ID_EXTRA` input and maps the sync outcome to a WorkManager `Result`.
             *
             * If the input entry id is missing, returns `Result.failure()`. Otherwise invokes the delete-sync use case:
             * - returns `Result.success()` when the sync succeeds,
             * - returns `Result.retry()` when the sync fails with a transient error,
             * - returns `Result.failure()` when the sync fails with a permanent error.
             *
             * @return A WorkManager `Result`: `Result.success()` on successful sync; `Result.retry()` for transient failures; `Result.failure()` if the id is missing or a permanent error occurred.
             */
            override suspend fun doWork(): Result =
            inputData.getString(EntrySyncWorkScheduler.ENTRY_ID_EXTRA)?.let { id ->
                when (val result = syncDeletedEntryUseCase(id)) {
                    Success -> Result.success()
                    is SyncResult.Error -> {
                        when (result.type) {
                            is PermanentError -> Result.failure()
                            is TransientError -> Result.retry()
                        }
                    }
                }
            } ?: Result.failure()
    }