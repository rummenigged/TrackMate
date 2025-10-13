package com.octopus.edu.core.data.entry.store

import com.octopus.edu.core.data.database.dao.EntryDao
import com.octopus.edu.core.data.database.entity.EntryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface EntryStore {
    suspend fun getHabits(): List<EntryEntity>

    suspend fun getTasks(): List<EntryEntity>

    suspend fun saveEntry(entry: EntryEntity)

    suspend fun upsertIfNewest(entry: EntryEntity)

    suspend fun getEntryById(id: String): EntryEntity?

    suspend fun deleteEntry(entryId: String)

    suspend fun updateEntrySyncState(
        entryId: String,
        syncStateEntity: EntryEntity.SyncStateEntity
    )

    suspend fun getPendingEntries(): List<EntryEntity>

    fun getAllEntriesByDateAndOrderedByTime(date: Long): Flow<List<EntryEntity>>

    fun getEntriesBeforeOrOn(date: Long): Flow<List<EntryEntity>>

    fun streamPendingEntries(): Flow<List<EntryEntity>>
}

internal class EntryStoreImpl
    @Inject
    constructor(
        private val entryDao: EntryDao,
    ) : EntryStore {
        override suspend fun getHabits(): List<EntryEntity> = entryDao.getHabits()

        override suspend fun getTasks(): List<EntryEntity> = entryDao.getTasks()

        override suspend fun getPendingEntries(): List<EntryEntity> = entryDao.getPendingEntries()

        override suspend fun getEntryById(id: String) = entryDao.getEntryById(id)

        override suspend fun saveEntry(entry: EntryEntity) = entryDao.insert(entry)

        override suspend fun upsertIfNewest(entry: EntryEntity) = entryDao.upsertIfNewest(entry)

        override suspend fun updateEntrySyncState(
            entryId: String,
            syncStateEntity: EntryEntity.SyncStateEntity
        ) = entryDao.updateSyncState(entryId, syncStateEntity)

        override suspend fun deleteEntry(entryId: String) = entryDao.delete(entryId)

        override fun streamPendingEntries(): Flow<List<EntryEntity>> = entryDao.streamPendingEntries()

        override fun getAllEntriesByDateAndOrderedByTime(date: Long): Flow<List<EntryEntity>> =
            entryDao.getAllEntriesByDateAndOrderedByTimeAsc(date)

        override fun getEntriesBeforeOrOn(date: Long): Flow<List<EntryEntity>> = entryDao.getEntriesBeforeOrOn(date)
    }
