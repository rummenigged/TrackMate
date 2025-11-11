package com.octopus.edu.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.octopus.edu.core.data.database.entity.DoneEntryEntity

@Dao
interface DoneEntryDao {
    @Insert
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

    @Query("DELETE from done_entries WHERE entryId = :entryId AND entryDate = :date")
    suspend fun delete(
        entryId: String,
        date: Long,
    )
}
