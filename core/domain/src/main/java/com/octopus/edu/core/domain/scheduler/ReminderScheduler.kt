package com.octopus.edu.core.domain.scheduler

import java.time.Duration

interface ReminderScheduler {
    fun scheduleReminder(
        entryId: String,
        delay: Duration
    )

    fun cancelReminder(entryId: String)
}
