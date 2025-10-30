package com.octopus.edu.trackmate.reminderSchedulers

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.octopus.edu.core.domain.scheduler.ReminderScheduler
import com.octopus.edu.trackmate.reminderSchedulers.ReminderConstants.ENTRY_ID_EXTRA
import com.octopus.edu.trackmate.reminderSchedulers.ReminderConstants.REMINDER_WORK_NAME
import com.octopus.edu.trackmate.workManager.reminder.ReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import javax.inject.Inject

object ReminderConstants {
    const val ENTRY_ID_EXTRA = "entry_id"
    const val REMINDER_NOTIFICATION_CHANNEL_ID_EXTRA = "reminder_channel"
    const val REMINDER_WORK_NAME = "ReminderWork"
}

class TaskNotificationReminderScheduler
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : ReminderScheduler {
        override fun scheduleReminder(
            entryId: String,
            delay: Duration,
            interval: Duration
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
    }
