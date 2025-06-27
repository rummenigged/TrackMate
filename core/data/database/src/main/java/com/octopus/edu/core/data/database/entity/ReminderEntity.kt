package com.octopus.edu.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("taskId")],
)
data class ReminderEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val triggerAtMillis: Long,
    val isRepeating: Boolean = false,
    val repeatIntervalMillis: Long? = null,
)
