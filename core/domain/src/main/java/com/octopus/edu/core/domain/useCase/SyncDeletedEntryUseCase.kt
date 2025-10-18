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
