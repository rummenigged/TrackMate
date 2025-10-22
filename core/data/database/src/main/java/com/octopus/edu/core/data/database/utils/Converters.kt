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

    /**
     * Converts a string name to the corresponding ReminderType.
     *
     * @param type The enum name of the ReminderType.
     * @return The matching ReminderType.
     */
    @TypeConverter
    fun toReminderType(type: String): ReminderType = ReminderType.valueOf(type)

    /**
     * Converts a SyncStateEntity value to its string name.
     *
     * @param state The SyncStateEntity to convert.
     * @return The enum constant's name as a String.
     */
    @TypeConverter
    fun fromSyncStateEntity(state: SyncStateEntity): String = state.name

    /**
     * Converts a string enum name to the corresponding SyncStateEntity.
     *
     * @param state The name of the SyncStateEntity enum constant.
     * @return The SyncStateEntity corresponding to `state`.
     */
    @TypeConverter
    fun toSyncStateEntity(state: String): SyncStateEntity = SyncStateEntity.valueOf(state)
}