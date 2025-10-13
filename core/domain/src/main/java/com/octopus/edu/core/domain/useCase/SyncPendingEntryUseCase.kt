package com.octopus.edu.core.domain.useCase

import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.SyncState
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.repository.EntryRepository
import javax.inject.Inject

sealed interface SyncResult {
    object Success : SyncResult

    object TransientError : SyncResult

    object PermanentError : SyncResult
}

class SyncPendingEntryUseCase
    @Inject
    constructor(
        private val entryRepository: EntryRepository
    ) {
        suspend operator fun invoke(entryId: String): SyncResult =
            when (val result = entryRepository.getEntryById(entryId)) {
                is ResultOperation.Error -> {
                    if (result.isRetriable) {
                        SyncResult.TransientError
                    } else {
                        entryRepository.updateEntrySyncState(entryId, SyncState.FAILED)
                        SyncResult.PermanentError
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
                        SyncResult.TransientError
                    } else {
                        entryRepository.updateEntrySyncState(entry.id, SyncState.FAILED)
                        SyncResult.PermanentError
                    }
                }
                is ResultOperation.Success -> {
                    entryRepository.updateEntrySyncState(entry.id, SyncState.SYNCED)
                    SyncResult.Success
                }
            }
    }
