package com.octopus.edu.core.domain.useCase

import com.octopus.edu.core.domain.model.DoneEntry
import com.octopus.edu.core.domain.model.SyncState
import com.octopus.edu.core.domain.model.common.ErrorType.PermanentError
import com.octopus.edu.core.domain.model.common.ErrorType.TransientError
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.model.common.SyncResult
import com.octopus.edu.core.domain.repository.EntrySyncRepository
import java.time.LocalDate
import javax.inject.Inject

class SyncEntryMarkedAsDoneUseCase
    @Inject
    constructor(
        private val syncRepository: EntrySyncRepository
    ) {
        suspend operator fun invoke(
            entryId: String,
            entryDate: LocalDate
        ): SyncResult =
            when (val result = syncRepository.getDoneEntry(entryId, entryDate)) {
                is ResultOperation.Success -> pushEntryMarkedAsDone(result.data)
                is ResultOperation.Error -> {
                    updateDoneEntrySyncState(entryId, entryDate, SyncState.FAILED)
                        .takeIf { it is SyncResult.Error }
                        ?: SyncResult.Error(PermanentError(result.throwable))
                }
            }

        private suspend fun pushEntryMarkedAsDone(entry: DoneEntry): SyncResult =
            when (val result = syncRepository.pushDoneEntry(entry)) {
                is ResultOperation.Success -> {
                    updateDoneEntrySyncState(entry.id, entry.date, SyncState.SYNCED)
                        .takeIf { it is SyncResult.Success }
                        ?: SyncResult.Success
                }

                is ResultOperation.Error -> {
                    if (result.isRetriable) {
                        SyncResult.Error(TransientError(result.throwable))
                    } else {
                        updateDoneEntrySyncState(entry.id, entry.date, SyncState.FAILED)
                            .takeIf { it is SyncResult.Error }
                            ?: SyncResult.Error(PermanentError(result.throwable))
                    }
                }
            }

        private suspend fun updateDoneEntrySyncState(
            entryId: String,
            entryDate: LocalDate,
            syncState: SyncState
        ): SyncResult =
            when (val result = syncRepository.updateDoneEntrySyncState(entryId, entryDate, syncState)) {
                is ResultOperation.Success -> SyncResult.Success
                is ResultOperation.Error -> {
                    if (result.isRetriable) {
                        SyncResult.Error(TransientError(result.throwable))
                    } else {
                        SyncResult.Error(PermanentError(result.throwable))
                    }
                }
            }
    }
