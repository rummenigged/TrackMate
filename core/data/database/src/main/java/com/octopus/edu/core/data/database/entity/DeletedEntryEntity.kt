package com.octopus.edu.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "deleted_entry",
)
class DeletedEntryEntity(
    @PrimaryKey
    val id: String,
    val deletedAt: Long,
    val syncState: EntryEntity.SyncStateEntity = EntryEntity.SyncStateEntity.PENDING
)
