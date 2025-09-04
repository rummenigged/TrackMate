package com.octopus.edu.feature.home.models

import com.octopus.edu.core.domain.model.Recurrence
import com.octopus.edu.core.domain.model.Reminder
import com.octopus.edu.core.domain.scheduler.ReminderType
import java.time.LocalDate
import java.time.LocalTime

data class EntryCreationData(
    val title: String? = null,
    val description: String? = null,
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val reminder: Reminder? = null,
    val reminderType: ReminderType? = null,
    val recurrence: Recurrence? = null,
) {
    companion object

    val currentEntryDateOrToday: LocalDate
        get() = date ?: LocalDate.now()
}

internal fun EntryCreationData.Companion.empty(): EntryCreationData = EntryCreationData()
