package com.octopus.edu.core.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.octopus.edu.core.data.database.dao.DeletedEntryDao
import com.octopus.edu.core.data.database.dao.EntryDao
import com.octopus.edu.core.data.database.dao.ReminderDao
import com.octopus.edu.core.data.database.entity.DeletedEntryEntity
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
        DeletedEntryEntity::class,
    ],
    version = 5,
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
                /**
                 * Adds a non-null `type` column to the `reminders` table with a default value of `'NOTIFICATION'`.
                 */
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "ALTER TABLE reminders ADD COLUMN type TEXT NOT NULL DEFAULT " +
                            "'NOTIFICATION'",
                    )
                }
            }

        private val MIGRATION_3_4: Migration =
            object : Migration(3, 4) {
                /**
                 * Adds the non-null `syncState` column (default value `'PENDING'`) to the `entries` table for the migration from database version 3 to 4.
                 *
                 * @param db The writable SQLite database on which migration SQL should be executed.
                 */
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "ALTER TABLE entries ADD COLUMN syncState TEXT NOT NULL DEFAULT 'PENDING'",
                    )
                }
            }

        private val MIGRATION_4_5: Migration =
            object : Migration(4, 5) {
                /**
                 * Creates the `deleted_entry` table if it does not already exist.
                 *
                 * The table has columns `id` (primary key), `deletedAt` (timestamp as INTEGER), and `syncState` (TEXT).
                 *
                 * @param db The SQLite database to apply the migration on.
                 */
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `deleted_entry` " +
                            "(`id` TEXT NOT NULL, `deletedAt` INTEGER NOT NULL, " +
                            "`syncState` TEXT NOT NULL, PRIMARY KEY(`id`))",
                    )
                }
            }

        /**
                 * Builds and returns the TrackMateDatabase instance configured with the app's persistent schema.
                 *
                 * @return A TrackMateDatabase configured with migrations for versions 1→2, 2→3, 3→4, and 4→5.
                 */
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
                .build()
    }

    /**
 * Provides the DAO used to access and manipulate entry records.
 *
 * @return The EntryDao for querying, inserting, updating, and deleting entries.
 */
abstract fun entryDao(): EntryDao

    /**
 * Accesses the DAO for reminders.
 *
 * @return The ReminderDao used to perform CRUD and query operations on reminder entities.
 */
abstract fun reminderDao(): ReminderDao

    /**
 * Provides the DAO for accessing deleted entry records.
 *
 * @return The `DeletedEntryDao` for performing queries and mutations on deleted entries.
 */
abstract fun deletedEntryDao(): DeletedEntryDao
}