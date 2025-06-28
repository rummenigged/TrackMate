package com.octopus.edu.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "task_sessions",
    indices = [Index("taskId")],
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class TaskSessionEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val startedAt: Long,
    val endedAt: Long?,
    val notes: String?,
)
