package com.octopus.edu.trackmate.workManager.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.octopus.edu.core.domain.model.common.ErrorType.PermanentError
import com.octopus.edu.core.domain.model.common.ErrorType.TransientError
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.model.common.SyncResult.Error
import com.octopus.edu.core.domain.model.common.SyncResult.Success
import com.octopus.edu.core.domain.repository.EntryRepository
import com.octopus.edu.core.domain.useCase.SyncPendingEntryUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncPendingEntriesWorker
    @AssistedInject
    constructor(
        @Assisted val context: Context,
        @Assisted val params: WorkerParameters,
        private val entryRepository: EntryRepository,
        private val syncPendingEntryUseCase: SyncPendingEntryUseCase
    ) : CoroutineWorker(context, params) {
        /**
         * Synchronizes pending entries and yields the appropriate WorkManager Result based on encountered errors.
         *
         * Fetches pending entries from the repository and attempts to sync each entry via the sync use case.
         * If the repository fetch failed and is retriable, the worker requests a retry; if the fetch failed and is not retriable, the worker fails.
         * After attempting to sync all entries, the worker requests a retry if any transient errors occurred, fails if any permanent errors occurred, and succeeds if all entries synced without error.
         *
         * @return `Result.retry()` if the repository fetch was retriable or any entry produced a transient error; `Result.failure()` if the repository fetch was not retriable or any entry produced a permanent error; `Result.success()` if all entries synced without error.
         */
        override suspend fun doWork(): Result {
            var sawTransientError = false
            var sawPermanentError = false
            return when (val result = entryRepository.getPendingEntries()) {
                is ResultOperation.Error -> {
                    if (result.isRetriable) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
                is ResultOperation.Success -> {
//                     TODO: Implement a controlled concurrency to sync entries
                    result.data.forEach { entry ->
                        when (val result = syncPendingEntryUseCase(entry.id)) {
                            Success -> {}
                            is Error -> {
                                when (result.type) {
                                    is PermanentError -> sawPermanentError = true
                                    is TransientError -> sawTransientError = true
                                }
                            }
                        }
                    }
                    return when {
                        sawTransientError -> Result.retry()
                        sawPermanentError -> Result.failure()
                        else -> Result.success()
                    }
                }
            }
        }
    }