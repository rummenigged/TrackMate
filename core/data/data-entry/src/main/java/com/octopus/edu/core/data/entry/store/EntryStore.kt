package com.octopus.edu.core.data.entry.store

import com.octopus.edu.core.data.database.dao.EntryDao
import com.octopus.edu.core.data.database.entity.EntryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface EntryStore {
    suspend fun getHabits(): List<EntryEntity>

    suspend fun getTasks(): List<EntryEntity>

    fun getAllEntriesOrderedByTime(): Flow<List<EntryEntity>>

    suspend fun saveEntry(entry: EntryEntity)
}

class EntryStoreImpl
    @Inject
    constructor(
        private val entryDao: EntryDao,
    ) : EntryStore {
        override suspend fun getHabits(): List<EntryEntity> = entryDao.getHabits()

        override suspend fun getTasks(): List<EntryEntity> = entryDao.getTasks()

        override fun getAllEntriesOrderedByTime(): Flow<List<EntryEntity>> = entryDao.getAllEntriesOrderedByTimeAsc()

        override suspend fun saveEntry(entry: EntryEntity) = entryDao.insert(entry)
    }
