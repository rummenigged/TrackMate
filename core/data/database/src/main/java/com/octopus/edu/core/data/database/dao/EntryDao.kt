package com.octopus.edu.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import com.octopus.edu.core.data.database.entity.EntryEntity
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity.PENDING
import com.octopus.edu.core.data.database.entity.databaseView.DoneEntryView
import com.octopus.edu.core.data.database.utils.EntrySyncResolver
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Insert(onConflict = REPLACE)
    suspend fun insert(entry: EntryEntity)

    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getEntryById(id: String): EntryEntity?

    @Query("SELECT * FROM entries WHERE type = 'HABIT'")
    suspend fun getHabits(): List<EntryEntity>

    @Query("SELECT * FROM entries WHERE type = 'TASK'")
    suspend fun getTasks(): List<EntryEntity>

    @Query("SELECT * FROM entries ORDER BY time IS NOT NULL, time")
    fun getAllEntriesOrderedByTimeAsc(): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE dueDate = :date OR startDate = :date ORDER BY time IS NOT NULL, time")
    fun getAllEntriesByDateAndOrderedByTimeAsc(date: Long): Flow<List<EntryEntity>>

    @Query("SELECT * FROM done_entry_view WHERE dueDate = :date OR startDate <= :date ORDER BY time IS NOT NULL, time")
    fun getEntriesBeforeOrOn(date: Long): Flow<List<DoneEntryView>>

    @Query("SELECT * FROM entries WHERE syncState = :pending")
    fun streamPendingEntries(pending: SyncStateEntity = PENDING): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE syncState = :pending")
    suspend fun getPendingEntries(pending: SyncStateEntity = PENDING): List<EntryEntity>

    @Query("DELETE from entries WHERE id = :entryId")
    suspend fun delete(entryId: String)

    @Query("UPDATE entries SET syncState = :syncState WHERE id = :entryId")
    suspend fun updateSyncState(
        entryId: String,
        syncState: SyncStateEntity
    )

    @Query("UPDATE entries SET syncState = :state, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateSyncMetadata(
        id: String,
        state: SyncStateEntity,
        updatedAt: Long
    )

    @Query("UPDATE entries SET isDone = 1 WHERE id = :entryId")
    suspend fun markEntryAsDone(entryId: String)

    @Transaction
    suspend fun upsertIfNewest(entry: EntryEntity) {
        val localEntry = getEntryById(entry.id)
        if (localEntry == null || EntrySyncResolver.shouldReplace(localEntry, entry)) {
            insert(entry)
        }
    }
}
