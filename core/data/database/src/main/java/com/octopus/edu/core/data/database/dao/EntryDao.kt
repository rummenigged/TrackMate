package com.octopus.edu.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.octopus.edu.core.data.database.entity.EntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Insert(onConflict = REPLACE)
    suspend fun insert(entry: EntryEntity)

    @Query("SELECT * FROM entries WHERE id = :id")
    fun getEntryById(id: String): EntryEntity?

    @Query("SELECT * FROM entries WHERE type = 'HABIT'")
    suspend fun getHabits(): List<EntryEntity>

    @Query("SELECT * FROM entries WHERE type = 'TASK'")
    suspend fun getTasks(): List<EntryEntity>

    @Query("SELECT * FROM entries ORDER BY time IS NOT NULL, time")
    fun getAllEntriesOrderedByTimeAsc(): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE dueDate = :date OR startDate = :date ORDER BY time IS NOT NULL, time")
    fun getAllEntriesByDateAndOrderedByTimeAsc(date: Long): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE dueDate = :date OR startDate <= :date ORDER BY time IS NOT NULL, time")
    fun getEntriesBeforeOrOn(date: Long): Flow<List<EntryEntity>>
}
