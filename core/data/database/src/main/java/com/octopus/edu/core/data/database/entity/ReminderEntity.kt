package com.octopus.edu.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = EntryEntity::class,
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
    val type: ReminderType,
    val isRepeating: Boolean = false,
    val repeatIntervalMillis: Long? = null,
)

enum class ReminderType {
    NOTIFICATION,
    ALARM
}
