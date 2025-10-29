package com.octopus.edu.core.domain.useCase

import com.octopus.edu.core.domain.model.DeletedEntry
import com.octopus.edu.core.domain.model.SyncState
import com.octopus.edu.core.domain.model.common.ErrorType.PermanentError
import com.octopus.edu.core.domain.model.common.ErrorType.TransientError
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.model.common.SyncResult
import com.octopus.edu.core.domain.repository.EntryRepository
import javax.inject.Inject

class SyncDeletedEntryUseCase
    @Inject
    constructor(
        private val entryRepository: EntryRepository
    ) {
        suspend operator fun invoke(id: String): SyncResult =
            when (val result = entryRepository.getDeletedEntry(id)) {
                is ResultOperation.Success -> pushDeletedEntry(result.data)

                is ResultOperation.Error -> {
                    if (result.isRetriable) {
                        SyncResult.Error(TransientError(result.throwable))
                    } else {
                        updateEntrySyncState(id, SyncState.FAILED)
                            .takeIf { it is SyncResult.Error }
                            ?: SyncResult.Error(PermanentError(result.throwable))
                    }
                }
            }

        private suspend fun pushDeletedEntry(entry: DeletedEntry): SyncResult =
            when (val result = entryRepository.pushDeletedEntry(entry)) {
                is ResultOperation.Success -> {
                    updateEntrySyncState(entry.id, SyncState.SYNCED)
                        .takeIf { it is SyncResult.Success }
                        ?: SyncResult.Success
                }

                is ResultOperation.Error -> {
                    if (result.isRetriable) {
                        SyncResult.Error(TransientError(result.throwable))
                    } else {
                        updateEntrySyncState(entry.id, SyncState.FAILED)
                            .takeIf { it is SyncResult.Error }
                            ?: SyncResult.Error(PermanentError(result.throwable))
                    }
                }
            }

        private suspend fun updateEntrySyncState(
            entryId: String,
            state: SyncState
        ): SyncResult =
            when (val result = entryRepository.updateEntrySyncState(entryId, state)) {
                is ResultOperation.Error -> {
                    if (result.isRetriable) {
                        SyncResult.Error(TransientError(result.throwable))
                    } else {
                        SyncResult.Error(PermanentError(result.throwable))
                    }
                }
                is ResultOperation.Success -> SyncResult.Success
            }
    }
