package com.octopus.edu.core.domain.scheduler

import java.time.LocalDate

interface EntrySyncScheduler {
    fun scheduleEntrySync(entryId: String)

    fun scheduleBatchSync()

    fun scheduleDeletedEntrySync(entryId: String)

    fun scheduleEntryMarkedAsDoneSync(
        entryId: String,
        entryDate: LocalDate
    )

    fun cancelEntrySync(entryId: String)
}
