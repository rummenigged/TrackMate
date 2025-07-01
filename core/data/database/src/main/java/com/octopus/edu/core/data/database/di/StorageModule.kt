package com.octopus.edu.core.data.database.di

import android.content.Context
import com.octopus.edu.core.data.database.TrackMateDatabase
import com.octopus.edu.core.data.database.dao.EntryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class StorageModule {
    @Provides
    @Singleton
    fun database(
        @ApplicationContext context: Context,
    ) = TrackMateDatabase.create(context)

    @Provides
    @Singleton
    fun entryDao(database: TrackMateDatabase): EntryDao = database.entryDao()
}
