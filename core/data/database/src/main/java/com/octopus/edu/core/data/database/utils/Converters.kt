package com.octopus.edu.core.data.database.utils

import androidx.room.TypeConverter
import com.octopus.edu.core.data.database.entity.EntryEntity.EntryType
import com.octopus.edu.core.data.database.entity.EntryEntity.Recurrence
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity
import com.octopus.edu.core.data.database.entity.ReminderType

internal class Converters {
    @TypeConverter
    fun fromEntryType(type: EntryType): String = type.name

    @TypeConverter
    fun toEntryType(name: String): EntryType = EntryType.valueOf(name)

    @TypeConverter
    fun fromRecurrence(type: Recurrence?): String? = type?.name

    @TypeConverter
    fun toRecurrence(name: String?): Recurrence? = name?.let { Recurrence.valueOf(it) }

    @TypeConverter
    fun fromReminderType(type: ReminderType): String = type.name

    @TypeConverter
    fun toReminderType(type: String): ReminderType = ReminderType.valueOf(type)

    @TypeConverter
    fun fromSyncStateEntity(state: SyncStateEntity): String = state.name

    @TypeConverter
    fun toSyncStateEntity(state: String): SyncStateEntity = SyncStateEntity.valueOf(state)

    @TypeConverter
    fun fromListOfLong(value: List<Long>?): String = value?.joinToString(",") ?: ""

    @TypeConverter
    fun toListOfLong(value: String): List<Long> =
        if (value.isEmpty()) {
            emptyList()
        } else {
            value.split(",").map { it.toLong() }
        }
}
