package com.octopus.edu.core.domain.repository

import com.octopus.edu.core.domain.model.DoneEntry
import com.octopus.edu.core.domain.model.SyncState
import com.octopus.edu.core.domain.model.common.ResultOperation
import java.time.LocalDate

interface EntrySyncRepository {
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
}
