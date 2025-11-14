package com.octopus.edu.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index

@Entity(
    tableName = "done_entries",
    primaryKeys = ["entryId", "entryDate"],
    foreignKeys = [
        ForeignKey(
            entity = EntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = CASCADE,
        ),
    ],
    indices = [Index("entryId")],
)
data class DoneEntryEntity(
    val entryId: String,
    val entryDate: Long,
    val doneAt: Long,
    val isConfirmed: Boolean,
    val syncState: EntryEntity.SyncStateEntity
)
