package com.octopus.edu.core.domain.useCase

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
        /**
             * Syncs a deleted entry identified by `id` with the remote and updates its local sync state.
             *
             * Retrieves the deleted entry, attempts to push the deletion to the remote, and updates the entry's
             * sync state to `SYNCED` on success or to `FAILED` when a push fails permanently.
             *
             * @param id The identifier of the deleted entry to synchronize.
             * @return `SyncResult.Success` when the entry was successfully synced;
             *         `SyncResult.Error(TransientError(...))` for retriable failures;
             *         `SyncResult.Error(PermanentError(...))` for non-retriable failures (in which case the entry's sync state is set to `FAILED`).
             */
            suspend operator fun invoke(id: String): SyncResult =
            when (val result = entryRepository.getDeletedEntry(id)) {
                is ResultOperation.Error -> {
                    if (result.isRetriable) {
                        SyncResult.Error(TransientError(result.throwable))
                    } else {
                        SyncResult.Error(PermanentError(result.throwable))
                    }
                }
                is ResultOperation.Success -> {
                    when (val result = entryRepository.pushDeletedEntry(result.data)) {
                        is ResultOperation.Error -> {
                            if (result.isRetriable) {
                                SyncResult.Error(TransientError(result.throwable))
                            } else {
                                entryRepository.updateDeletedEntrySyncState(
                                    id,
                                    SyncState.FAILED,
                                )
                                SyncResult.Error(PermanentError(result.throwable))
                            }
                        }
                        is ResultOperation.Success -> {
                            entryRepository.updateDeletedEntrySyncState(
                                id,
                                SyncState.SYNCED,
                            )
                            SyncResult.Success
                        }
                    }
                }
            }
    }