package com.octopus.edu.trackmate.reminder

import com.octopus.edu.core.common.ReminderTimeCalculator.calculateReminderDelay
import com.octopus.edu.core.common.ReminderTimeCalculator.defaultTimeIfNull
import com.octopus.edu.core.common.ReminderTimeCalculator.getHabitInterval
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.Reminder
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.scheduler.ReminderScheduler
import com.octopus.edu.core.domain.scheduler.ReminderStrategy
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

            val offset = entry.reminder?.offset ?: Reminder.None.offset

            val delay =
                calculateReminderDelay(
                    time = defaultTimeIfNull(entry.time),
                    date = date,
                    reminderOffset = offset,
                )

            when (entry) {
                is Habit -> {
                    reminderScheduler.scheduleReminder(
                        entry.id,
                        delay,
                        getHabitInterval(entry.recurrence, offset),
                    )
                }
                else -> return
            }
        }
    }
