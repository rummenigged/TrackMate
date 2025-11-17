package com.octopus.edu.trackmate.workManager.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.octopus.edu.core.common.toLocalDate
import com.octopus.edu.core.domain.model.common.ErrorType
import com.octopus.edu.core.domain.model.common.SyncResult
import com.octopus.edu.core.domain.useCase.SyncEntryMarkedAsDoneUseCase
import com.octopus.edu.trackmate.sync.EntrySyncWorkScheduler.Companion.ENTRY_DATE_EXTRA
import com.octopus.edu.trackmate.sync.EntrySyncWorkScheduler.Companion.ENTRY_ID_EXTRA
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncEntryMarkedAsDoneWorker
    @AssistedInject
    constructor(
        @Assisted val context: Context,
        @Assisted val params: WorkerParameters,
        private val syncEntryMarkedAsDoneUseCase: SyncEntryMarkedAsDoneUseCase
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            val id = inputData.getString(ENTRY_ID_EXTRA) ?: return Result.failure()
            val date = inputData.getLong(ENTRY_DATE_EXTRA, 0L)
            if (date == 0L) return Result.failure()

            return when (
                val result =
                    syncEntryMarkedAsDoneUseCase(
                        entryId = id,
                        entryDate = date.toLocalDate(),
                    )
            ) {
                SyncResult.Success -> Result.success()
                is SyncResult.Error -> {
                    when (result.type) {
                        is ErrorType.PermanentError -> Result.failure()
                        is ErrorType.TransientError -> Result.retry()
                    }
                }
            }
        }
    }
