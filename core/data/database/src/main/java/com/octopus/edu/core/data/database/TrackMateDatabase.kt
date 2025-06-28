package com.octopus.edu.core.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.octopus.edu.core.data.database.entity.ReminderEntity
import com.octopus.edu.core.data.database.entity.TagEntity
import com.octopus.edu.core.data.database.entity.TaskEntity
import com.octopus.edu.core.data.database.entity.TaskSessionEntity
import com.octopus.edu.core.data.database.entity.UserEntity

private const val NAME = "trackmatedb"

@Database(
    entities = [
        UserEntity::class,
        TaskEntity::class,
        TaskSessionEntity::class,
        TagEntity::class,
        ReminderEntity::class,
    ],
    version = 1,
)
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
}
