package com.octopus.edu.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.octopus.edu.core.data.database.entity.DeletedEntryEntity
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity.PENDING
import kotlinx.coroutines.flow.Flow

@Dao
interface DeletedEntryDao {
    /**
     * Inserts or replaces the given deleted entry in the database.
     *
     * If a row with the same primary key already exists, it will be replaced.
     *
     * @param deletedEntry The DeletedEntryEntity to insert or replace.
     */
    @Insert(onConflict = REPLACE)
    suspend fun save(deletedEntry: DeletedEntryEntity)

    /**
     * Retrieves a deleted entry by its id.
     *
     * @param id The unique identifier of the deleted entry.
     * @return The matching DeletedEntryEntity, or `null` if no entry exists with the given id.
     */
    @Query("SELECT * FROM deleted_entry WHERE id == :id")
    fun getDeletedEntry(id: String): DeletedEntryEntity?

    /**
     * Streams deleted entries whose sync state matches the provided value.
     *
     * @param pending The sync state to filter by; defaults to `PENDING`.
     * @return A Flow that emits lists of DeletedEntryEntity matching the given sync state.
     */
    @Query("SELECT * FROM deleted_entry WHERE syncState == :pending")
    fun streamPendingDeletedEntries(pending: SyncStateEntity = PENDING): Flow<List<DeletedEntryEntity>>

    /**
     * Updates the syncState of the deleted entry with the given id.
     *
     * @param id The identifier of the deleted entry to update.
     * @param syncState The new sync state to assign to the entry.
     */
    @Query("UPDATE deleted_entry SET syncState = :syncState WHERE id = :id")
    fun updateSyncState(
        id: String,
        syncState: SyncStateEntity
    )

    /**
     * Deletes the DeletedEntryEntity with the specified id from the deleted_entry table.
     *
     * @param id The identifier of the deleted entry to remove.
     */
    @Query("DELETE from deleted_entry WHERE id = :id")
    suspend fun delete(id: String)
}