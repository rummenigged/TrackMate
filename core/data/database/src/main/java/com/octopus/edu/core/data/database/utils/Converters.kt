package com.octopus.edu.core.data.database.utils

import androidx.room.TypeConverter
import com.octopus.edu.core.data.database.entity.EntryEntity.EntryType
import com.octopus.edu.core.data.database.entity.EntryEntity.Recurrence

internal class Converters {
    @TypeConverter
    fun fromEntryType(type: EntryType): String = type.name

    @TypeConverter
    fun toEntryType(name: String): EntryType = EntryType.valueOf(name)

    @TypeConverter
    fun fromRecurrence(type: Recurrence?): String? = type?.name

    @TypeConverter
    fun toRecurrence(name: String?): Recurrence? = name?.let { Recurrence.valueOf(it) }
}
