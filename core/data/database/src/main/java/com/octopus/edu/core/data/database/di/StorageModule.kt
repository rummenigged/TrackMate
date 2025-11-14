package com.octopus.edu.core.data.database.di

import android.content.Context
import com.octopus.edu.core.common.TransactionRunner
import com.octopus.edu.core.data.database.TrackMateDatabase
import com.octopus.edu.core.data.database.dao.DeletedEntryDao
import com.octopus.edu.core.data.database.dao.DoneEntryDao
import com.octopus.edu.core.data.database.dao.EntryDao
import com.octopus.edu.core.data.database.dao.ReminderDao
import com.octopus.edu.core.data.database.utils.RoomTransactionRunner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {
    @Provides
    @Singleton
    fun database(
        @ApplicationContext context: Context,
    ): TrackMateDatabase = TrackMateDatabase.create(context)

    @Provides
    @Singleton
    fun entryDao(database: TrackMateDatabase): EntryDao = database.entryDao()

    @Provides
    @Singleton
    fun reminderDao(database: TrackMateDatabase): ReminderDao = database.reminderDao()

    @Provides
    @Singleton
    fun deletedEntryDao(database: TrackMateDatabase): DeletedEntryDao = database.deletedEntryDao()

    @Provides
    @Singleton
    fun doneEntryDao(database: TrackMateDatabase): DoneEntryDao = database.doneEntryDao()

    @Provides
    @Singleton
    fun roomTransactionRunner(database: TrackMateDatabase): TransactionRunner = RoomTransactionRunner(database)
}
