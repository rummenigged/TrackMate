package com.octopus.edu.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "entry_sessions",
    indices = [Index("entryId")],
    foreignKeys = [
        ForeignKey(
            entity = EntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class EntrySessionEntity(
    @PrimaryKey val id: String,
    val entryId: String,
    val startedAt: Long,
    val endedAt: Long?,
    val notes: String?,
)
