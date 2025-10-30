package com.octopus.edu.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "entries",
)
data class EntryEntity(
    @PrimaryKey val id: String,
    val type: EntryType,
    val title: String,
    val description: String?,
    val isDone: Boolean = false,
    val recurrence: Recurrence? = null,
    val streakCount: Int? = null,
    val lastCompletedDate: Long? = null,
    val isArchived: Boolean = false,
    val syncState: SyncStateEntity,
    val dueDate: Long? = null,
    val startDate: Long? = null,
    val time: Long? = null,
    val createdAt: Long,
    val updatedAt: Long? = null,
) {
    enum class EntryType { TASK, HABIT }

    enum class Recurrence {
        DAILY,
        WEEKLY,
        CUSTOM,
    }

    enum class SyncStateEntity { PENDING, SYNCED, FAILED, CONFLICT }
}
