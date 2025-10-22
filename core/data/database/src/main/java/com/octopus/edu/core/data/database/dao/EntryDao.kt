package com.octopus.edu.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import com.octopus.edu.core.data.database.entity.EntryEntity
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity.PENDING
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

    /**
     * Streams entries whose due date equals the given date or whose start date is on or before it,
     * ordered with entries that have a time first and then by time ascending.
     *
     * @param date The date to match, expressed as milliseconds since the Unix epoch.
     * @return A Flow that emits lists of matching EntryEntity objects in the described order.
     */
    @Query("SELECT * FROM entries WHERE dueDate = :date OR startDate <= :date ORDER BY time IS NOT NULL, time")
    fun getEntriesBeforeOrOn(date: Long): Flow<List<EntryEntity>>

    /**
     * Emit lists of entries whose sync state equals the provided state.
     *
     * @param pending The sync state to filter entries by. Defaults to `PENDING`.
     * @return A Flow that emits lists of EntryEntity objects with `syncState` equal to `pending`.
     */
    @Query("SELECT * FROM entries WHERE syncState = :pending")
    fun streamPendingEntries(pending: SyncStateEntity = PENDING): Flow<List<EntryEntity>>

    /**
     * Fetches all entries whose sync state equals the provided pending state.
     *
     * @param pending The sync state to filter entries by; defaults to `PENDING`.
     * @return A list of `EntryEntity` objects with `syncState` equal to `pending`.
     */
    @Query("SELECT * FROM entries WHERE syncState = :pending")
    fun getPendingEntries(pending: SyncStateEntity = PENDING): List<EntryEntity>

    /**
     * Deletes the entry with the specified id from the database.
     *
     * @param entryId The id of the entry to delete.
     */
    @Query("DELETE from entries WHERE id = :entryId")
    suspend fun delete(entryId: String)

    /**
     * Set the synchronization state for an entry identified by its id.
     *
     * @param entryId The id of the entry to update.
     * @param syncState The new sync state to store for the entry.
     */
    @Query("UPDATE entries SET syncState = :syncState WHERE id = :entryId")
    fun updateSyncState(
        entryId: String,
        syncState: SyncStateEntity
    )

    /**
     * Inserts or replaces the given entry in the database only when no entry exists with the same id
     * or the provided entry is newer according to its sync metadata and recency.
     *
     * @param entry The entry to insert or replace if it is considered newer than the stored entry.
     */
    @Transaction
    suspend fun upsertIfNewest(entry: EntryEntity) {
        val localEntry = getEntryById(entry.id)
        if (localEntry == null || EntrySyncResolver.shouldReplace(localEntry, entry)) {
            insert(entry)
        }
    }
}