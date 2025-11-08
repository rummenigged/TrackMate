package com.octopus.edu.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.octopus.edu.core.data.database.entity.common.BaseEntryEntity

@Entity(
    tableName = "entries",
)
data class EntryEntity(
    @PrimaryKey override val id: String,
    override val type: EntryType,
    override val title: String,
    override val description: String?,
    override val recurrence: Recurrence? = null,
    override val isDone: Boolean = false,
    override val streakCount: Int? = null,
    override val lastCompletedDate: Long? = null,
    override val isArchived: Boolean = false,
    override val syncState: SyncStateEntity,
    override val dueDate: Long? = null,
    override val startDate: Long? = null,
    override val time: Long? = null,
    override val createdAt: Long,
    override val updatedAt: Long? = null,
) : BaseEntryEntity {
    enum class EntryType { TASK, HABIT }

    enum class Recurrence {
        DAILY,
        WEEKLY,
        CUSTOM,
    }

    enum class SyncStateEntity { PENDING, SYNCED, FAILED, CONFLICT }
}
