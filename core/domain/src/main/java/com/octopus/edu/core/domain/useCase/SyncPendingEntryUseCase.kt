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
        /**
             * Synchronizes a pending entry identified by its ID with the remote data source.
             *
             * @param entryId The identifier of the entry to synchronize.
             * @return A SyncResult describing the outcome:
             *         - `SyncResult.Success` when the entry was pushed and marked SYNCED.
             *         - `SyncResult.Error(TransientError)` when the operation failed but may be retried.
             *         - `SyncResult.Error(PermanentError)` when the failure is not retriable; in this case the entry's sync state is updated to FAILED.
             */
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

        /**
             * Pushes the provided entry to the remote store and updates the entry's local sync state accordingly.
             *
             * @param entry The entry to push.
             * @return `SyncResult.Success` if the push succeeds; `SyncResult.Error(TransientError(...))` if the push fails with a retriable error;
             * `SyncResult.Error(PermanentError(...))` if the push fails with a non-retriable error (the entry's sync state is updated to `FAILED` in this case).
             */
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