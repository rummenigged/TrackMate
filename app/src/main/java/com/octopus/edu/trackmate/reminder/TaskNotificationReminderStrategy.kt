package com.octopus.edu.trackmate.reminder

import com.octopus.edu.core.common.ReminderTimeCalculator.calculateReminderDelay
import com.octopus.edu.core.common.ReminderTimeCalculator.defaultTimeIfNull
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.Reminder
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.scheduler.ReminderScheduler
import com.octopus.edu.core.domain.scheduler.ReminderStrategy
import javax.inject.Inject
import javax.inject.Named

class TaskNotificationReminderStrategy
    @Inject
    constructor(
        @param:Named("TaskNotificationReminderScheduler") private val reminderScheduler: ReminderScheduler,
    ) : ReminderStrategy {
        override fun schedule(entry: Entry) {
            val date =
                when (entry) {
                    is Task -> entry.dueDate
                    else -> null
                } ?: return

            val delay =
                calculateReminderDelay(
                    time = defaultTimeIfNull(entry.time),
                    date = date,
                    reminderOffset = entry.reminder?.offset ?: Reminder.None.offset,
                )

            reminderScheduler.scheduleReminder(entry.id, delay)
        }
    }
