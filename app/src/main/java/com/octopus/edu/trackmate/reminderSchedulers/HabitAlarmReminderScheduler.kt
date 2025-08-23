package com.octopus.edu.trackmate.reminderSchedulers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import com.octopus.edu.core.domain.scheduler.ReminderScheduler
import com.octopus.edu.trackmate.reminder.ReminderAlarmReceiver.Companion.getPendingIntent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import javax.inject.Inject

class HabitAlarmReminderScheduler
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context
    ) : ReminderScheduler {
        val alarmManager: AlarmManager? = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        override fun scheduleReminder(
            entryId: String,
            delay: Duration,
            interval: Duration
        ) {
            alarmManager?.let {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                    alarmManager.canScheduleExactAlarms()
                ) {
                    val triggerAtMillis = System.currentTimeMillis() + delay.toMillis()
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        getPendingIntent(context, entryId, interval.toMillis()),
                    )
                }
            } ?: return
        }

        override fun cancelReminder(entryId: String) {
            val pendingIntent = getPendingIntent(context, entryId, flags = PendingIntent.FLAG_NO_CREATE)
            alarmManager?.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
