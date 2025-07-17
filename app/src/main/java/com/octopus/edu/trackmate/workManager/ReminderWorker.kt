package com.octopus.edu.trackmate.workManager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.repository.EntryRepository
import com.octopus.edu.trackmate.utils.NotificationHelper
import com.octopus.edu.trackmate.workManager.ReminderConstants.ENTRY_ID_EXTRA
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker
    @AssistedInject
    constructor(
        @Assisted val context: Context,
        @Assisted val params: WorkerParameters,
        private val entryRepository: EntryRepository,
        private val notificationHelper: NotificationHelper
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            val entryId = inputData.getString(ENTRY_ID_EXTRA)
            return when (val entry = entryRepository.getEntryById(entryId!!)) {
                is ResultOperation.Error -> Result.failure()

                is ResultOperation.Success -> {
                    notificationHelper.showReminderNotification(
                        entryId,
                        entry.data.title,
                    )
                    Result.success()
                }
            }
        }
    }
