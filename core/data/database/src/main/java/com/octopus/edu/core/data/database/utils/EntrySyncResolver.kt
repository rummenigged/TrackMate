package com.octopus.edu.core.data.database.utils

import com.octopus.edu.core.data.database.entity.EntryEntity

object EntrySyncResolver {
    /**
     * Determines whether an existing entry should be replaced by a newer entry based on their `updatedAt` timestamps.
     *
     * @param currentEntry The existing entry; its `updatedAt` is treated as 0 if null.
     * @param newEntry The candidate entry; its `updatedAt` is treated as 0 if null.
     * @return `true` if `newEntry` has a strictly greater `updatedAt` than `currentEntry`, `false` otherwise.
     */
    fun shouldReplace(
        currentEntry: EntryEntity,
        newEntry: EntryEntity
    ): Boolean {
        val currentEntryUpdatedAt = currentEntry.updatedAt ?: 0L
        val newEntryUpdatedAt = newEntry.updatedAt ?: 0L
        return newEntryUpdatedAt > currentEntryUpdatedAt
    }
}