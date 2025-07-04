package com.octopus.edu.core.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.octopus.edu.core.data.database.dao.EntryDao
import com.octopus.edu.core.data.database.entity.EntryEntity
import com.octopus.edu.core.data.database.entity.EntrySessionEntity
import com.octopus.edu.core.data.database.entity.ReminderEntity
import com.octopus.edu.core.data.database.entity.TagEntity
import com.octopus.edu.core.data.database.utils.Converters

private const val NAME = "trackmatedb"

@Database(
    entities = [
        EntryEntity::class,
        EntrySessionEntity::class,
        TagEntity::class,
        ReminderEntity::class,
    ],
    version = 1,
)
@TypeConverters(Converters::class)
abstract class TrackMateDatabase : RoomDatabase() {
    companion object {
        fun create(context: Context): TrackMateDatabase =
            Room
                .databaseBuilder(
                    context,
                    TrackMateDatabase::class.java,
                    NAME,
                ).build()
    }

    abstract fun entryDao(): EntryDao
}
