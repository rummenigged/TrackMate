package com.octopus.edu.feature.home.models

import com.octopus.edu.core.domain.model.Recurrence
import java.time.LocalDate
import java.time.LocalTime

internal data class EntryCreationData(
    val currentEntryTitle: String? = null,
    val currentEntryDescription: String? = null,
    val currentEntryDate: LocalDate? = null,
    val currentEntryTime: LocalTime? = null,
    val currentEntryReminder: String? = null,
    val currentEntryRecurrence: Recurrence? = null,
) {
    companion object

    val currentEntryDateOrToday: LocalDate
        get() = currentEntryDate ?: LocalDate.now()
}

internal fun EntryCreationData.Companion.empty(): EntryCreationData = EntryCreationData()
