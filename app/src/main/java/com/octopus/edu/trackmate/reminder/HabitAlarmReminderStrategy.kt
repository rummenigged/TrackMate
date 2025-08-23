package com.octopus.edu.trackmate.reminder

import android.util.Log
import com.octopus.edu.core.common.ReminderTimeCalculator.calculateReminderDelay
import com.octopus.edu.core.common.ReminderTimeCalculator.defaultTimeIfNull
import com.octopus.edu.core.common.ReminderTimeCalculator.getHabitInterval
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.Reminder
import com.octopus.edu.core.domain.scheduler.ReminderScheduler
import com.octopus.edu.core.domain.scheduler.ReminderStrategy
import javax.inject.Inject
import javax.inject.Named

class HabitAlarmReminderStrategy
    @Inject
    constructor(
        @param:Named("HabitAlarmReminderScheduler") private val reminderScheduler: ReminderScheduler
    ) : ReminderStrategy {
        override fun schedule(entry: Entry) {
            val date = if (entry is Habit) entry.startDate else return

            val offset = entry.reminder?.offset ?: Reminder.None.offset

            val delay =
                calculateReminderDelay(
                    time = defaultTimeIfNull(entry.time),
                    date = date,
                    reminderOffset = offset,
                )

            val interval = getHabitInterval(entry.recurrence, offset)

            if (interval.toMillis() <= 0) return

            reminderScheduler.scheduleReminder(
                entry.id,
                delay,
                interval,
            )
        }
    }
