package com.octopus.edu.trackmate.workManager.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.octopus.edu.core.domain.useCase.SyncPendingEntryUseCase
import com.octopus.edu.core.domain.useCase.SyncResult
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
        override suspend fun doWork(): Result {
            val id = inputData.getString(EntrySyncWorkScheduler.ENTRY_ID_EXTRA) ?: return Result.failure()

            return when (syncPendingEntryUseCase(id)) {
                SyncResult.PermanentError -> Result.failure()
                SyncResult.TransientError -> Result.retry()
                SyncResult.Success -> Result.success()
            }
        }
    }
