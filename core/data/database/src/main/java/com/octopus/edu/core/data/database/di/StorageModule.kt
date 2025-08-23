package com.octopus.edu.core.data.database.di

import android.content.Context
import com.octopus.edu.core.data.database.TrackMateDatabase
import com.octopus.edu.core.data.database.dao.EntryDao
import com.octopus.edu.core.data.database.dao.ReminderDao
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
}
