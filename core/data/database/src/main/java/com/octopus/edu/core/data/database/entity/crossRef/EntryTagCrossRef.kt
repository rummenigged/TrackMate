package com.octopus.edu.core.data.database.entity.crossRef

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.octopus.edu.core.data.database.entity.EntryEntity
import com.octopus.edu.core.data.database.entity.TagEntity

@Entity(
    tableName = "entry_tag_cross_ref",
    primaryKeys = ["entryId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = EntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("taskId"), Index("tagId")],
)
data class EntryTagCrossRef(
    val entryId: String,
    val tagId: String,
)
