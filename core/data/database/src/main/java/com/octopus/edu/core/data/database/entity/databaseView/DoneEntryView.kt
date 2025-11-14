package com.octopus.edu.core.data.database.entity.databaseView

import androidx.room.DatabaseView
import androidx.room.Embedded
import com.octopus.edu.core.data.database.entity.EntryEntity
import com.octopus.edu.core.data.database.entity.common.BaseEntryEntity

@DatabaseView(
    viewName = "done_entry_view",
    value = """
    SELECT e.*, 
           COALESCE(GROUP_CONCAT(d.entryDate), '') AS doneDates
    FROM entries e
    LEFT JOIN done_entries d ON e.id = d.entryId
    GROUP BY e.id
    """,
)
data class DoneEntryView(
    @Embedded val entry: EntryEntity,
    val doneDates: List<Long>
) : BaseEntryEntity by entry
