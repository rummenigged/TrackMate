package com.octopus.edu.trackmate.workManager

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.octopus.edu.core.domain.scheduler.ReminderScheduler
import com.octopus.edu.trackmate.workManager.ReminderConstants.ENTRY_ID_EXTRA
import com.octopus.edu.trackmate.workManager.ReminderConstants.REMINDER_WORK_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import javax.inject.Inject

class HabitNotificationReminderScheduler
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context
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

            PeriodicWorkRequestBuilder<ReminderWorker>(interval)
                .setInitialDelay(delay)
                .setInputData(data)
                .build()
                .also { workRequest ->
                    WorkManager.Companion
                        .getInstance(context)
                        .enqueueUniquePeriodicWork(
                            "$REMINDER_WORK_NAME#$entryId",
                            ExistingPeriodicWorkPolicy.REPLACE,
                            workRequest,
                        )
                }
        }

        override fun cancelReminder(entryId: String) {
            WorkManager.getInstance(context).cancelUniqueWork("$REMINDER_WORK_NAME#$entryId")
        }
    }
