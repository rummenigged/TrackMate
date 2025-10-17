package com.octopus.edu.core.domain.useCase

import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.SyncState
import com.octopus.edu.core.domain.model.common.ErrorType.PermanentError
import com.octopus.edu.core.domain.model.common.ErrorType.TransientError
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.model.common.SyncResult
import com.octopus.edu.core.domain.repository.EntryRepository
import javax.inject.Inject

class SyncPendingEntryUseCase
    @Inject
    constructor(
        private val entryRepository: EntryRepository
    ) {
        suspend operator fun invoke(entryId: String): SyncResult =
            when (val result = entryRepository.getEntryById(entryId)) {
                is ResultOperation.Error -> {
                    if (result.isRetriable) {
                        SyncResult.Error(TransientError(result.throwable))
                    } else {
                        entryRepository.updateEntrySyncState(entryId, SyncState.FAILED)
                        SyncResult.Error(PermanentError(result.throwable))
                    }
                }

                is ResultOperation.Success -> {
                    pushEntry(result.data)
                }
            }

        private suspend fun pushEntry(entry: Entry): SyncResult =
            when (val result = entryRepository.pushEntry(entry)) {
                is ResultOperation.Error -> {
                    if (result.isRetriable) {
                        SyncResult.Error(TransientError(result.throwable))
                    } else {
                        entryRepository.updateEntrySyncState(entry.id, SyncState.FAILED)
                        SyncResult.Error(PermanentError(result.throwable))
                    }
                }
                is ResultOperation.Success -> {
                    entryRepository.updateEntrySyncState(entry.id, SyncState.SYNCED)
                    SyncResult.Success
                }
            }
    }
