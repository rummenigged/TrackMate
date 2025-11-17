package com.octopus.edu.core.domain.repository

import com.octopus.edu.core.domain.model.DeletedEntry
import com.octopus.edu.core.domain.model.DoneEntry
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.SyncState
import com.octopus.edu.core.domain.model.common.ResultOperation
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface EntrySyncRepository {
    val pendingEntries: Flow<List<Entry>>

    val deletedEntryIds: Flow<List<String>>

    val pendingEntriesMarkedAsDone: Flow<List<DoneEntry>>

    suspend fun getDoneEntry(
        entryId: String,
        entryDate: LocalDate
    ): ResultOperation<DoneEntry>

    suspend fun pushDoneEntry(entry: DoneEntry): ResultOperation<Unit>

    suspend fun updateDoneEntrySyncState(
        entryId: String,
        entryDate: LocalDate,
        syncState: SyncState
    ): ResultOperation<Unit>

    suspend fun updateEntrySyncState(
        entryId: String,
        syncState: SyncState
    ): ResultOperation<Unit>

    suspend fun pushEntry(entry: Entry): ResultOperation<Unit>

    suspend fun getPendingEntries(): ResultOperation<List<Entry>>

    suspend fun syncEntries(): ResultOperation<Unit>

    suspend fun getDeletedEntry(entryId: String): ResultOperation<DeletedEntry>

    suspend fun pushDeletedEntry(deletedEntry: DeletedEntry): ResultOperation<Unit>

    suspend fun updateDeletedEntrySyncState(
        entryId: String,
        syncState: SyncState
    ): ResultOperation<Unit>
}
