package com.octopus.edu.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    indices = [Index("userId")],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val color: String?,
    val isArchived: Boolean = false,
    val createdAt: Long,
    val userId: String,
)
