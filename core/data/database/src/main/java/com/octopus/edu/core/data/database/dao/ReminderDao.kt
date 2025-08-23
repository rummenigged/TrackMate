package com.octopus.edu.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.octopus.edu.core.data.database.entity.ReminderEntity

@Dao
interface ReminderDao {
    @Insert(onConflict = REPLACE)
    suspend fun insert(reminder: ReminderEntity)

    @Query("SELECT * FROM reminders WHERE taskId = :entryId")
    suspend fun getReminderByEntryId(entryId: String): ReminderEntity?

    @Query("DELETE from reminders WHERE id = :id")
    suspend fun delete(id: String)
}
