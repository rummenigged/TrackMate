package com.octopus.edu.core.data.entry.di

import com.octopus.edu.core.common.DispatcherProvider
import com.octopus.edu.core.data.database.dao.EntryDao
import com.octopus.edu.core.data.database.dao.ReminderDao
import com.octopus.edu.core.data.entry.EntryRepositoryImpl
import com.octopus.edu.core.data.entry.store.EntryStore
import com.octopus.edu.core.data.entry.store.EntryStoreImpl
import com.octopus.edu.core.data.entry.store.ReminderStore
import com.octopus.edu.core.data.entry.store.ReminderStoreImpl
import com.octopus.edu.core.domain.repository.EntryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object EntryDataModule {
    @Provides
    fun providesEntryRepository(
        entryStore: EntryStore,
        reminderStore: ReminderStore,
        dispatcherProvider: DispatcherProvider
    ): EntryRepository = EntryRepositoryImpl(entryStore, reminderStore, dispatcherProvider)

    @Provides
    fun providesEntryStore(entryDao: EntryDao): EntryStore = EntryStoreImpl(entryDao)

    @Provides
    fun providesReminderStore(reminderDao: ReminderDao): ReminderStore = ReminderStoreImpl(reminderDao)
}
