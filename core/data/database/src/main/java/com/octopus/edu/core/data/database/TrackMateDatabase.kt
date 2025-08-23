package com.octopus.edu.core.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.octopus.edu.core.data.database.dao.EntryDao
import com.octopus.edu.core.data.database.dao.ReminderDao
import com.octopus.edu.core.data.database.entity.EntryEntity
import com.octopus.edu.core.data.database.entity.EntrySessionEntity
import com.octopus.edu.core.data.database.entity.ReminderEntity
import com.octopus.edu.core.data.database.entity.TagEntity
import com.octopus.edu.core.data.database.utils.Converters

private const val NAME = "trackmate.db"

@Database(
    entities = [
        EntryEntity::class,
        EntrySessionEntity::class,
        TagEntity::class,
        ReminderEntity::class,
    ],
    version = 3,
)
@TypeConverters(Converters::class)
abstract class TrackMateDatabase : RoomDatabase() {
    companion object {
        private val MIGRATION_1_2: Migration =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE entries ADD COLUMN startDate INTEGER")
                }
            }

        private val MIGRATION_2_3: Migration =
            object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "ALTER TABLE reminders ADD COLUMN type TEXT NOT NULL DEFAULT " +
                            "'NOTIFICATION'",
                    )
                }
            }

        fun create(context: Context): TrackMateDatabase =
            Room
                .databaseBuilder(
                    context,
                    TrackMateDatabase::class.java,
                    NAME,
                ).addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .build()
    }

    abstract fun entryDao(): EntryDao

    abstract fun reminderDao(): ReminderDao
}
