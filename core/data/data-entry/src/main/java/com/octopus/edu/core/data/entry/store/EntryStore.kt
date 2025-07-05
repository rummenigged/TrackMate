package com.octopus.edu.core.data.entry.store

import com.octopus.edu.core.data.database.dao.EntryDao
import com.octopus.edu.core.data.database.entity.EntryEntity
import javax.inject.Inject

interface EntryStore {
    suspend fun getHabits(): List<EntryEntity>

    suspend fun getTasks(): List<EntryEntity>

    suspend fun getAllEntries(): List<EntryEntity>
}

class EntryStoreImpl
    @Inject
    constructor(
        private val entryDao: EntryDao,
    ) : EntryStore {
        override suspend fun getHabits(): List<EntryEntity> = entryDao.getHabits()

        override suspend fun getTasks(): List<EntryEntity> = entryDao.getTasks()

        override suspend fun getAllEntries(): List<EntryEntity> = entryDao.getAllEntries()
    }
