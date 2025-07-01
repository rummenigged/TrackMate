package com.octopus.edu.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.octopus.edu.core.data.database.entity.EntryEntity

@Dao
interface EntryDao {
    @Insert(onConflict = REPLACE)
    suspend fun insert(entry: EntryEntity)

    @Query("SELECT * FROM entries WHERE type = 'HABIT'")
    suspend fun getHabits(): List<EntryEntity>

    @Query("SELECT * FROM entries WHERE type = 'TASK'")
    suspend fun getTasks(): List<EntryEntity>
}
