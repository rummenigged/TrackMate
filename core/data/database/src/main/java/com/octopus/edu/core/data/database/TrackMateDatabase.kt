package com.octopus.edu.core.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.octopus.edu.core.data.database.dao.DeletedEntryDao
import com.octopus.edu.core.data.database.dao.DoneEntryDao
import com.octopus.edu.core.data.database.dao.EntryDao
import com.octopus.edu.core.data.database.dao.ReminderDao
import com.octopus.edu.core.data.database.entity.DeletedEntryEntity
import com.octopus.edu.core.data.database.entity.DoneEntryEntity
import com.octopus.edu.core.data.database.entity.EntryEntity
import com.octopus.edu.core.data.database.entity.EntrySessionEntity
import com.octopus.edu.core.data.database.entity.ReminderEntity
import com.octopus.edu.core.data.database.entity.TagEntity
import com.octopus.edu.core.data.database.entity.databaseView.DoneEntryView
import com.octopus.edu.core.data.database.utils.Converters

private const val NAME = "trackmate.db"

@Database(
    entities = [
        EntryEntity::class,
        EntrySessionEntity::class,
        TagEntity::class,
        ReminderEntity::class,
        DeletedEntryEntity::class,
        DoneEntryEntity::class,
    ],
    views = [
        DoneEntryView::class,
    ],
    version = 6,
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

        private val MIGRATION_3_4: Migration =
            object : Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "ALTER TABLE entries ADD COLUMN syncState TEXT NOT NULL DEFAULT 'PENDING'",
                    )
                }
            }

        private val MIGRATION_4_5: Migration =
            object : Migration(4, 5) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `deleted_entry` " +
                            "(`id` TEXT NOT NULL, `deletedAt` INTEGER NOT NULL, " +
                            "`syncState` TEXT NOT NULL DEFAULT 'PENDING', PRIMARY KEY(`id`))",
                    )
                }
            }

        private val MIGRATION_5_6: Migration =
            object : Migration(5, 6) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS done_entries " +
                            "(entryId TEXT NOT NULL, entryDate INTEGER NOT NULL" +
                            "doneAt INTEGER NOT NULL," +
                            "syncState TEXT NOT NULL," +
                            "PRIMARY KEY(entryId, doneAt)," +
                            "FOREIGN KEY(entryId) " +
                            "REFERENCES entries(id) ON DELETE CASCADE)",
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS index_done_entries_entryId " +
                            "ON done_entries(entryId)",
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
                .addMigrations(MIGRATION_3_4)
                .addMigrations(MIGRATION_4_5)
                .addMigrations(MIGRATION_5_6)
                .build()
    }

    abstract fun entryDao(): EntryDao

    abstract fun reminderDao(): ReminderDao

    abstract fun deletedEntryDao(): DeletedEntryDao

    abstract fun doneEntryDao(): DoneEntryDao
}
