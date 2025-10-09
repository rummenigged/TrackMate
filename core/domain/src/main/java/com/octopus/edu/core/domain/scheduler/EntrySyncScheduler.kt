package com.octopus.edu.core.domain.scheduler

interface EntrySyncScheduler {
    fun scheduleEntrySync(entryId: String)

    fun scheduleBatchSync()

    fun cancelEntrySync(entryId: String)
}
