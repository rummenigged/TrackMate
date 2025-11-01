package com.octopus.edu.core.domain.scheduler

import com.octopus.edu.core.domain.model.Entry

enum class ReminderType {
    NOTIFICATION,
    ALARM
}

interface ReminderStrategyFactory {
    fun getStrategy(
        entry: Entry,
        type: ReminderType
    ): ReminderStrategy?
}
