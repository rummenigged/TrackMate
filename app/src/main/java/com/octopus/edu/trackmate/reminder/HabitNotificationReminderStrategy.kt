package com.octopus.edu.trackmate.reminder

import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.Recurrence
import com.octopus.edu.core.domain.model.Reminder
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.scheduler.ReminderScheduler
import com.octopus.edu.core.domain.scheduler.ReminderStrategy
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Named

class HabitNotificationReminderStrategy
    @Inject
    constructor(
        @param:Named("HabitNotificationReminderScheduler") private val reminderScheduler: ReminderScheduler
    ) : ReminderStrategy {
        override fun schedule(entry: Entry) {
            val date =
                when (entry) {
                    is Habit -> entry.startDate
                    is Task -> null
                } ?: return

            val time = entry.time ?: LocalTime.of(8, 0)
            val reminderOffset = entry.reminder?.offset ?: Reminder.None.offset
            val reminderDateTime = date.atTime(time).minus(reminderOffset)
            val now = LocalDateTime.now()
            val delay = Duration.between(now, reminderDateTime)

            val interval =
                when (entry) {
                    is Habit ->
                        when (entry.recurrence) {
                            Recurrence.Daily -> Duration.ofDays(1)
                            Recurrence.Weekly -> Duration.ofDays(7)
                            else -> Duration.ofDays(1)
                        }
                    else -> Duration.ofDays(1)
                }

            reminderScheduler.scheduleReminder(
                entry.id,
                delay,
                interval.minus(reminderOffset),
            )
        }
    }
