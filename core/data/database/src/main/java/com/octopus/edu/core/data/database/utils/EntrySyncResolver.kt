package com.octopus.edu.core.data.database.utils

import com.octopus.edu.core.data.database.entity.DoneEntryEntity
import com.octopus.edu.core.data.database.entity.EntryEntity

object EntrySyncResolver {
    fun shouldReplace(
        currentEntry: EntryEntity,
        newEntry: EntryEntity
    ): Boolean {
        val currentEntryUpdatedAt = currentEntry.updatedAt ?: 0L
        val newEntryUpdatedAt = newEntry.updatedAt ?: 0L
        return newEntryUpdatedAt > currentEntryUpdatedAt
    }
}

object DoneEntrySyncResolver {
    fun shouldReplace(
        currentEntry: DoneEntryEntity,
        newEntry: DoneEntryEntity
    ): Boolean {
        val currentEntryUpdatedAt = currentEntry.doneAt
        val newEntryUpdatedAt = newEntry.doneAt
        return newEntryUpdatedAt < currentEntryUpdatedAt
    }
}
