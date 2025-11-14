package com.octopus.edu.core.domain.useCase

import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.SyncState
import com.octopus.edu.core.domain.model.common.ErrorType.PermanentError
import com.octopus.edu.core.domain.model.common.ErrorType.TransientError
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.model.common.SyncResult
import com.octopus.edu.core.domain.repository.EntryRepository
import com.octopus.edu.core.domain.repository.EntrySyncRepository
import javax.inject.Inject

class SyncPendingEntryUseCase
    @Inject
    constructor(
        private val entryRepository: EntryRepository,
        private val syncRepository: EntrySyncRepository
    ) {
        suspend operator fun invoke(entryId: String): SyncResult =
            when (val result = entryRepository.getEntryById(entryId)) {
                is ResultOperation.Success -> pushEntry(result.data)

                is ResultOperation.Error -> {
                    if (result.isRetriable) {
                        SyncResult.Error(TransientError(result.throwable))
                    } else {
                        updateEntrySyncState(entryId, SyncState.FAILED)
                            .takeIf { it is SyncResult.Error }
                            ?: SyncResult.Error(PermanentError(result.throwable))
                    }
                }
            }

        private suspend fun pushEntry(entry: Entry): SyncResult =
            when (val result = syncRepository.pushEntry(entry)) {
                is ResultOperation.Success -> {
                    // TODO: Figure out how to log when gets error as it is the domain
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
            when (val result = syncRepository.updateEntrySyncState(entryId, state)) {
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
