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
    @Insert(onConflict = REPLACE)
    suspend fun save(deletedEntry: DeletedEntryEntity)

    @Query("SELECT * FROM deleted_entry WHERE id == :id")
    fun getDeletedEntry(id: String): DeletedEntryEntity?

    @Query("SELECT * FROM deleted_entry WHERE syncState == :pending")
    fun streamPendingDeletedEntries(pending: SyncStateEntity = PENDING): Flow<List<DeletedEntryEntity>>

    @Query("UPDATE deleted_entry SET syncState = :syncState WHERE id = :id")
    fun updateSyncState(
        id: String,
        syncState: SyncStateEntity
    )

    @Query("DELETE from deleted_entry WHERE id = :id")
    suspend fun delete(id: String)
}
