package com.octopus.edu.trackmate.workManager

import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.repository.EntryRepository
import com.octopus.edu.trackmate.MainActivity
import com.octopus.edu.trackmate.reminderSchedulers.ReminderConstants.ENTRY_ID_EXTRA
import com.octopus.edu.trackmate.utils.NotificationHelper
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
                    val intent =
                        Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            putExtra(ENTRY_ID_EXTRA, entryId)
                        }

                    notificationHelper.showReminderNotification(
                        entryId.hashCode(),
                        entry.data.title,
                        intent,
                    )
                    Result.success()
                }
            }
        }
    }
