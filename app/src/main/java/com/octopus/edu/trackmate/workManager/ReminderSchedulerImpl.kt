package com.octopus.edu.trackmate.workManager

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.octopus.edu.core.domain.scheduler.ReminderScheduler
import com.octopus.edu.trackmate.workManager.ReminderConstants.ENTRY_ID_EXTRA
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import java.time.Duration

object ReminderConstants {
    const val ENTRY_ID_EXTRA = "entry_id"
    const val REMINDER_NOTIFICATION_CHANNEL_ID_EXTRA = "reminder_channel"
}

class ReminderSchedulerImpl
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : ReminderScheduler {
        override fun scheduleReminder(
            entryId: String,
            delay: Duration,
        ) {
            val data =
                workDataOf(
                    ENTRY_ID_EXTRA to entryId,
                )

            OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay)
                .setInputData(data)
                .build()
                .also { workRequest ->
                    WorkManager.Companion
                        .getInstance(context)
                        .beginUniqueWork(
                            "$REMINDER_WORK_NAME#$entryId",
                            ExistingWorkPolicy.REPLACE,
                            workRequest,
                        ).enqueue()
                }
        }

        override fun cancelReminder(entryId: String) {
            WorkManager.getInstance(context).cancelUniqueWork("$REMINDER_WORK_NAME#$entryId")
        }

        companion object {
            private const val REMINDER_WORK_NAME = "ReminderWorker"
        }
    }
