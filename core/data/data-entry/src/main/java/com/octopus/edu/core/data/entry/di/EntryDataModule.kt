package com.octopus.edu.core.data.entry.di

import com.google.firebase.firestore.FirebaseFirestore
import com.octopus.edu.core.common.DispatcherProvider
import com.octopus.edu.core.data.database.dao.EntryDao
import com.octopus.edu.core.data.database.dao.ReminderDao
import com.octopus.edu.core.data.entry.EntryRepositoryImpl
import com.octopus.edu.core.data.entry.api.EntryApi
import com.octopus.edu.core.data.entry.api.EntryApiImpl
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
    fun provideEntryApi(api: FirebaseFirestore): EntryApi = EntryApiImpl(api)

    @Provides
    fun providesEntryRepository(
        entryStore: EntryStore,
        entryApi: EntryApi,
        reminderStore: ReminderStore,
        dispatcherProvider: DispatcherProvider
    ): EntryRepository = EntryRepositoryImpl(entryStore, entryApi, reminderStore, dispatcherProvider)

    @Provides
    fun providesEntryStore(entryDao: EntryDao): EntryStore = EntryStoreImpl(entryDao)

    @Provides
    fun providesReminderStore(reminderDao: ReminderDao): ReminderStore = ReminderStoreImpl(reminderDao)
}
