package com.octopus.edu.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.octopus.edu.core.data.database.entity.DoneEntryEntity
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity.PENDING
import com.octopus.edu.core.data.database.utils.DoneEntrySyncResolver
import kotlinx.coroutines.flow.Flow

@Dao
interface DoneEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(doneEntry: DoneEntryEntity)

    @Query("SELECT * FROM done_entries WHERE entryId = :entryId AND entryDate = :entryDate")
    suspend fun getDoneEntry(
        entryId: String,
        entryDate: Long
    ): DoneEntryEntity?

    @Query("UPDATE done_entries SET isConfirmed = :isConfirmed WHERE entryId = :entryId AND entryDate = :entryDate")
    suspend fun updateIsConfirmed(
        entryId: String,
        entryDate: Long,
        isConfirmed: Boolean,
    )

    @Query("UPDATE done_entries SET syncState = :syncState WHERE entryId = :entryId AND entryDate = :entryDate")
    suspend fun updateSyncState(
        entryId: String,
        entryDate: Long,
        syncState: SyncStateEntity
    )

    @Query("SELECT * FROM done_entries WHERE syncState == :pending AND isConfirmed = 1")
    fun streamPendingEntriesMarkedAsDone(pending: SyncStateEntity = PENDING): Flow<List<DoneEntryEntity>>

    @Query("DELETE from done_entries WHERE entryId = :entryId AND entryDate = :date")
    suspend fun delete(
        entryId: String,
        date: Long,
    )

    @Transaction
    suspend fun upsertIfOldest(doneEntry: DoneEntryEntity) {
        val localDoneEntry = getDoneEntry(doneEntry.entryId, doneEntry.entryDate)
        if (localDoneEntry == null || DoneEntrySyncResolver.shouldReplace(localDoneEntry, doneEntry)) {
            insert(doneEntry)
        }
    }
}
