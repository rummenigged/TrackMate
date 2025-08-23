package com.octopus.edu.core.data.entry.store

import com.octopus.edu.core.data.database.dao.ReminderDao
import com.octopus.edu.core.data.database.entity.ReminderEntity
import javax.inject.Inject

interface ReminderStore {
    suspend fun saveReminder(reminder: ReminderEntity)

    suspend fun getReminderByEntryId(entryId: String): ReminderEntity?
}

internal class ReminderStoreImpl
    @Inject
    constructor(
        private val reminderDao: ReminderDao
    ) : ReminderStore {
        override suspend fun saveReminder(reminder: ReminderEntity) = reminderDao.insert(reminder)

        override suspend fun getReminderByEntryId(entryId: String): ReminderEntity? = reminderDao.getReminderByEntryId(entryId)
    }
