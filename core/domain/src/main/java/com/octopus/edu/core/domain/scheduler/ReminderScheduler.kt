package com.octopus.edu.core.domain.scheduler

import java.time.Duration

interface ReminderScheduler {
    fun scheduleReminder(
        entryId: String,
        delay: Duration,
        interval: Duration = Duration.ZERO
    )

    fun cancelReminder(entryId: String)
}
