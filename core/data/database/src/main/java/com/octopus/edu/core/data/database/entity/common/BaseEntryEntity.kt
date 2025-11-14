package com.octopus.edu.core.data.database.entity.common

import com.octopus.edu.core.data.database.entity.EntryEntity.EntryType
import com.octopus.edu.core.data.database.entity.EntryEntity.Recurrence
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity

interface BaseEntryEntity {
    val id: String
    val type: EntryType
    val title: String
    val description: String?
    val isDone: Boolean
    val recurrence: Recurrence?
    val streakCount: Int?
    val lastCompletedDate: Long?
    val isArchived: Boolean
    val syncState: SyncStateEntity
    val dueDate: Long?
    val startDate: Long?
    val time: Long?
    val createdAt: Long
    val updatedAt: Long?
}
