package com.octopus.edu.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import com.octopus.edu.core.data.database.entity.DoneEntryEntity

@Dao
interface DoneEntryDao {
    @Insert
    suspend fun insert(doneEntry: DoneEntryEntity)
}
