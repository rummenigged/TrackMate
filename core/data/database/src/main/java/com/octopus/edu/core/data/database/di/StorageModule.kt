package com.octopus.edu.core.data.database.di

import android.content.Context
import com.octopus.edu.core.common.TransactionRunner
import com.octopus.edu.core.data.database.TrackMateDatabase
import com.octopus.edu.core.data.database.dao.DeletedEntryDao
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

    /**
     * Provides the singleton ReminderDao instance from the TrackMateDatabase.
     *
     * @return The ReminderDao retrieved from the provided database.
     */
    @Provides
    @Singleton
    fun reminderDao(database: TrackMateDatabase): ReminderDao = database.reminderDao()

    /**
     * Provides the singleton DeletedEntryDao from the given TrackMateDatabase.
     *
     * @param database The TrackMateDatabase used to obtain the DAO.
     * @return The DeletedEntryDao instance.
     */
    @Provides
    @Singleton
    fun deletedEntryDao(database: TrackMateDatabase): DeletedEntryDao = database.deletedEntryDao()

    /**
     * Provides a TransactionRunner that executes transactions against the application database.
     *
     * @param database The TrackMateDatabase instance used to perform transactions.
     * @return A TransactionRunner that executes database transactions using the provided database.
     */
    @Provides
    @Singleton
    fun roomTransactionRunner(database: TrackMateDatabase): TransactionRunner = RoomTransactionRunner(database)
}