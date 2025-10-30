package com.octopus.edu.core.domain.scheduler

interface EntrySyncScheduler {
    fun scheduleEntrySync(entryId: String)

    fun scheduleBatchSync()

    fun scheduleDeletedEntrySync(entryId: String)

    fun cancelEntrySync(entryId: String)
}
