package com.octopus.edu.core.data.database.entity.crossRef

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.octopus.edu.core.data.database.entity.TagEntity
import com.octopus.edu.core.data.database.entity.TaskEntity

@Entity(
    tableName = "task_tag_cross_ref",
    primaryKeys = ["taskId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(entity = TagEntity::class, parentColumns = ["id"], childColumns = ["tagId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("taskId"), Index("tagId")],
)
data class TaskTagCrossRef(
    val taskId: String,
    val tagId: String,
)
