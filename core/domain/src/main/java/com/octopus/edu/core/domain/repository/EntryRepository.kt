package com.octopus.edu.core.domain.repository

import com.octopus.edu.core.domain.model.DeletedEntry
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.SyncState
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.model.common.ResultOperation
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface EntryRepository {
    val pendingEntries: Flow<List<Entry>>

    val deletedEntryIds: Flow<List<String>>

    suspend fun getTasks(): ResultOperation<List<Task>>

    suspend fun getHabits(): ResultOperation<List<Habit>>

    suspend fun saveEntry(entry: Entry): ResultOperation<Unit>

    suspend fun getEntryById(id: String): ResultOperation<Entry>

    fun getEntriesVisibleOn(date: LocalDate = LocalDate.now()): Flow<ResultOperation<List<Entry>>>

    suspend fun deleteEntry(entryId: String): ResultOperation<Unit>

    suspend fun pushEntry(entry: Entry): ResultOperation<Unit>

    suspend fun updateEntrySyncState(
        entryId: String,
        syncState: SyncState
    ): ResultOperation<Unit>

    suspend fun getPendingEntries(): ResultOperation<List<Entry>>

    suspend fun syncEntries(): ResultOperation<Unit>

    suspend fun getDeletedEntry(entryId: String): ResultOperation<DeletedEntry>

    suspend fun pushDeletedEntry(deletedEntry: DeletedEntry): ResultOperation<Unit>

    suspend fun updateDeletedEntrySyncState(
        entryId: String,
        syncState: SyncState
    ): ResultOperation<Unit>

    suspend fun markEntryAsDone(
        entryId: String,
        entryDate: LocalDate,
        isConfirmed: Boolean = false
    ): ResultOperation<Unit>

    suspend fun unmarkEntryAsDone(
        entryId: String,
        entryDate: LocalDate
    ): ResultOperation<Unit>

    suspend fun confirmEntryAsDone(
        entryId: String,
        entryDate: LocalDate
    ): ResultOperation<Unit>
}
