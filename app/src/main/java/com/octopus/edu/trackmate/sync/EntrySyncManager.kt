package com.octopus.edu.trackmate.sync

import com.octopus.edu.core.common.DispatcherProvider
import com.octopus.edu.core.common.Logger
import com.octopus.edu.core.common.RetryPolicy
import com.octopus.edu.core.data.entry.di.SyncErrorClassifierQualifier
import com.octopus.edu.core.domain.repository.EntryRepository
import com.octopus.edu.core.domain.scheduler.EntrySyncScheduler
import com.octopus.edu.core.domain.utils.ErrorClassifier
import com.octopus.edu.trackmate.di.ApplicationScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import javax.inject.Inject

class EntrySyncManager
    @Inject
    constructor(
        private val entryRepository: EntryRepository,
        private val syncScheduler: EntrySyncScheduler,
        @param:SyncErrorClassifierQualifier
        private val errorClassifier: ErrorClassifier,
        private val retryPolicy: RetryPolicy,
        private val dispatcherProvider: DispatcherProvider,
        @param:ApplicationScope private val scope: CoroutineScope
    ) {
        private val exceptionHandler =
            CoroutineExceptionHandler { _, throwable ->
                Logger.e(tag = "EntrySyncManager", message = "EntrySyncManager failed", throwable = throwable)
            }

        fun start() {
            Logger.d("start started")
            scope.launch(dispatcherProvider.io + exceptionHandler) {
                syncScheduler.scheduleBatchSync()

                launch {
                    collectDeletedEntries()
                }

                launch {
                    collectPendingEntries()
                }
            }
        }

        private suspend fun collectDeletedEntries() {
            entryRepository.deletedEntryIds
                .distinctUntilChanged()
                .retryWhen { cause, attempt ->
                    val errorType = errorClassifier.classify(cause)
                    retryPolicy.shouldRetry(errorType, attempt)
                }.catch {
                    Logger.e("Error collecting deleted entries", throwable = it)
                }.collectLatest { deletedEntries ->
                    deletedEntries.forEach { entryId ->
                        runCatching {
                            syncScheduler.scheduleDeletedEntrySync(entryId)
                        }.onFailure {
                            Logger.e("Failed to schedule deleted entry sync for $entryId", throwable = it)
                        }
                    }
                }
        }

        private suspend fun collectPendingEntries() {
            entryRepository.pendingEntries
                .distinctUntilChanged()
                .retryWhen { cause, attempt ->
                    val errorType = errorClassifier.classify(cause)
                    retryPolicy.shouldRetry(errorType, attempt)
                }.catch {
                    Logger.e("Error collecting pending entries", throwable = it)
                }.collectLatest { entries ->
                    entries.forEach { entry ->
                        runCatching {
                            syncScheduler.scheduleEntrySync(entry.id)
                        }.onFailure {
                            Logger.e("Failed to schedule pending entry sync for ${entry.id}", throwable = it)
                        }
                    }
                }
        }
    }
