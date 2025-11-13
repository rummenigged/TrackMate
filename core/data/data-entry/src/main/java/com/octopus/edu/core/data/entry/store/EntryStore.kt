package com.octopus.edu.core.data.entry.store

import com.octopus.edu.core.common.AppClock
import com.octopus.edu.core.common.TransactionRunner
import com.octopus.edu.core.data.database.dao.DeletedEntryDao
import com.octopus.edu.core.data.database.dao.DoneEntryDao
import com.octopus.edu.core.data.database.dao.EntryDao
import com.octopus.edu.core.data.database.entity.DeletedEntryEntity
import com.octopus.edu.core.data.database.entity.DoneEntryEntity
import com.octopus.edu.core.data.database.entity.EntryEntity
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity.PENDING
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface EntryStore {
    suspend fun getHabits(): List<EntryEntity>

    suspend fun getTasks(): List<EntryEntity>

    suspend fun saveEntry(entry: EntryEntity)

    suspend fun upsertIfNewest(entry: EntryEntity)

    suspend fun getEntryById(id: String): EntryEntity?

    suspend fun deleteEntry(
        entryId: String,
        state: SyncStateEntity
    )

    suspend fun getDeletedEntry(entryId: String): DeletedEntryEntity?

    suspend fun updateEntrySyncState(
        entryId: String,
        syncStateEntity: SyncStateEntity
    )

    suspend fun updateDeletedEntrySyncState(
        entryId: String,
        syncState: SyncStateEntity
    )

    suspend fun getPendingEntries(): List<EntryEntity>

    suspend fun markEntryAsDone(
        entryId: String,
        entryDate: Long,
        isConfirmed: Boolean
    )

    suspend fun unmarkEntryAsDone(
        entryId: String,
        entryDate: Long
    )

    suspend fun confirmEntryAsDone(
        entryId: String,
        entryDate: Long
    )

    suspend fun upsertDoneEntryIfOldest(doneEntry: DoneEntryEntity)

    suspend fun getDoneEntry(
        entryId: String,
        entryDate: Long
    ): DoneEntryEntity?

    suspend fun updateDoneEntrySyncState(
        entryId: String,
        entryDate: Long,
        syncState: SyncStateEntity
    )

    fun getAllEntriesByDateAndOrderedByTime(date: Long): Flow<List<EntryEntity>>

    fun getEntriesBeforeOrOn(date: Long): Flow<List<EntryEntity>>

    fun streamPendingEntries(): Flow<List<EntryEntity>>

    fun streamPendingDeletedEntries(): Flow<List<DeletedEntryEntity>>

    fun streamPendingDoneEntries(): Flow<List<DoneEntryEntity>>
}

internal class EntryStoreImpl
    @Inject
    constructor(
        private val entryDao: EntryDao,
        private val doneEntryDao: DoneEntryDao,
        private val deletedEntryDao: DeletedEntryDao,
        private val appClock: AppClock,
        private val roomTransactionRunner: TransactionRunner
    ) : EntryStore {
        override suspend fun getHabits(): List<EntryEntity> = entryDao.getHabits()

        override suspend fun getTasks(): List<EntryEntity> = entryDao.getTasks()

        override suspend fun getPendingEntries(): List<EntryEntity> = entryDao.getPendingEntries()

        override suspend fun markEntryAsDone(
            entryId: String,
            entryDate: Long,
            isConfirmed: Boolean
        ) = doneEntryDao.insert(
            DoneEntryEntity(
                entryId = entryId,
                entryDate = entryDate,
                doneAt = appClock.nowInstant().toEpochMilli(),
                isConfirmed = isConfirmed,
                syncState = PENDING,
            ),
        )

        override suspend fun unmarkEntryAsDone(
            entryId: String,
            entryDate: Long
        ) = doneEntryDao.delete(entryId, entryDate)

        override suspend fun confirmEntryAsDone(
            entryId: String,
            entryDate: Long
        ) = doneEntryDao.updateIsConfirmed(entryId, entryDate, true)

        override suspend fun getDoneEntry(
            entryId: String,
            entryDate: Long
        ): DoneEntryEntity? = doneEntryDao.getDoneEntry(entryId, entryDate)

        override suspend fun updateDoneEntrySyncState(
            entryId: String,
            entryDate: Long,
            syncState: SyncStateEntity
        ) = doneEntryDao.updateSyncState(entryId, entryDate, syncState)

        override suspend fun upsertDoneEntryIfOldest(doneEntry: DoneEntryEntity) = doneEntryDao.upsertIfOldest(doneEntry)

        override suspend fun getEntryById(id: String) = entryDao.getEntryById(id)

        override suspend fun saveEntry(entry: EntryEntity) = entryDao.insert(entry)

        override suspend fun upsertIfNewest(entry: EntryEntity) = entryDao.upsertIfNewest(entry)

        override suspend fun updateEntrySyncState(
            entryId: String,
            syncStateEntity: SyncStateEntity
        ) = entryDao.updateSyncState(entryId, syncStateEntity)

        override suspend fun updateDeletedEntrySyncState(
            entryId: String,
            syncState: SyncStateEntity
        ) {
            deletedEntryDao.updateSyncState(entryId, syncState)
        }

        override suspend fun deleteEntry(
            entryId: String,
            state: SyncStateEntity
        ) {
            roomTransactionRunner.run {
                entryDao.delete(entryId)
                deletedEntryDao.save(
                    DeletedEntryEntity(
                        id = entryId,
                        deletedAt = System.currentTimeMillis(),
                        syncState = state,
                    ),
                )
            }
        }

        override suspend fun getDeletedEntry(entryId: String): DeletedEntryEntity? = deletedEntryDao.getDeletedEntry(entryId)

        override fun getAllEntriesByDateAndOrderedByTime(date: Long): Flow<List<EntryEntity>> =
            entryDao.getAllEntriesByDateAndOrderedByTimeAsc(date)

        override fun getEntriesBeforeOrOn(date: Long): Flow<List<EntryEntity>> =
            entryDao
                .getEntriesBeforeOrOn(date)
                .map { entries ->
                    entries.map { (entry, doneDates) ->
                        entry.copy(isDone = date in doneDates)
                    }
                }

        override fun streamPendingEntries(): Flow<List<EntryEntity>> = entryDao.streamPendingEntries()

        override fun streamPendingDeletedEntries(): Flow<List<DeletedEntryEntity>> = deletedEntryDao.streamPendingDeletedEntries()

        override fun streamPendingDoneEntries(): Flow<List<DoneEntryEntity>> = doneEntryDao.streamPendingEntriesMarkedAsDone()
    }
