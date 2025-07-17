package com.octopus.edu.core.domain.scheduler

import com.octopus.edu.core.domain.model.Entry

interface ReminderStrategy {
    fun schedule(entry: Entry)
}
