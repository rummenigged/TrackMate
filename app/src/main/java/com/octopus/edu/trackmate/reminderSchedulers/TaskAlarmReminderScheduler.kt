package com.octopus.edu.trackmate.reminderSchedulers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.octopus.edu.core.domain.scheduler.ReminderScheduler
import com.octopus.edu.trackmate.ui.reminder.ReminderActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import javax.inject.Inject

class TaskAlarmReminderScheduler
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context
    ) : ReminderScheduler {
        override fun scheduleReminder(
            entryId: String,
            delay: Duration,
            interval: Duration
        ) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent =
                Intent(context, ReminderActivity::class.java).apply {
                    this.putExtra(ENTRY_ID_EXTRA, entryId)
                }
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + delay.toMillis(),
                        pendingIntent,
                    )
                }
            } else {
                return
            }
        }

        override fun cancelReminder(entryId: String) {
            TODO("Not yet implemented")
        }

        companion object {
            const val ENTRY_ID_EXTRA = "reminder_id"
        }
    }
