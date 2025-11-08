package com.octopus.edu.core.data.entry.store.decorator

import com.octopus.edu.core.common.AppClock
import com.octopus.edu.core.common.Logger
import com.octopus.edu.core.common.TransactionRunner
import com.octopus.edu.core.data.database.dao.EntryDao
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity.PENDING
import com.octopus.edu.core.data.entry.store.EntryStore
import javax.inject.Inject

class TrackingEntryStoreDecorator
    @Inject
    constructor(
        entryStore: EntryStore,
        private val entryDao: EntryDao,
        private val roomTransactionRunner: TransactionRunner,
        private val appClock: AppClock
    ) : DelegatingEntryStoreDecorator(entryStore) {
        override suspend fun markEntryAsDone(
            entryId: String,
            entryDate: Long
        ) {
            runCatching {
                roomTransactionRunner.run {
                    super.markEntryAsDone(entryId, entryDate)
                    entryDao.updateSyncMetadata(
                        entryId,
                        PENDING,
                        appClock.nowEpocMillis(),
                    )
                }
            }.onFailure {
                Logger.e(message = "Error marking entry $entryId as done", throwable = it)
            }.getOrThrow()
        }
    }
