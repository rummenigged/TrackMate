package com.octopus.edu.trackmate.sync

import com.octopus.edu.core.common.DispatcherProvider
import com.octopus.edu.core.common.Logger
import com.octopus.edu.core.domain.repository.EntryRepository
import com.octopus.edu.core.domain.scheduler.EntrySyncScheduler
import com.octopus.edu.trackmate.di.ApplicationScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

class EntrySyncManager
    @Inject
    constructor(
        private val entryRepository: EntryRepository,
        private val syncScheduler: EntrySyncScheduler,
        private val dispatcherProvider: DispatcherProvider,
        @param:ApplicationScope private val scope: CoroutineScope
    ) {
        private val exceptionHandler =
            CoroutineExceptionHandler { _, throwable ->
                Logger.e(tag = "EntrySyncManager", message = "EntrySyncManager failed", throwable = throwable)
            }

        fun start() {
            scope.launch(dispatcherProvider.io + exceptionHandler) {
                syncScheduler.scheduleBatchSync()

                entryRepository.pendingEntries
                    .catch {
                        Logger.e("Error collecting pending entries", throwable = it)
                        emit(emptyList())
                    }.distinctUntilChanged()
                    .collect { entries ->
                        entries.forEach { entry ->
                            syncScheduler.scheduleEntrySync(entry.id)
                        }
                    }
            }
        }
    }
