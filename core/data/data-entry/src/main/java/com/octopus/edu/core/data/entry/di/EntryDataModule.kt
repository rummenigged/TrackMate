package com.octopus.edu.core.data.entry.di

import com.google.firebase.firestore.FirebaseFirestore
import com.octopus.edu.core.common.DispatcherProvider
import com.octopus.edu.core.common.TransactionRunner
import com.octopus.edu.core.data.database.dao.DeletedEntryDao
import com.octopus.edu.core.data.database.dao.EntryDao
import com.octopus.edu.core.data.database.dao.ReminderDao
import com.octopus.edu.core.data.entry.BuildConfig
import com.octopus.edu.core.data.entry.EntryRepositoryImpl
import com.octopus.edu.core.data.entry.api.EntryApi
import com.octopus.edu.core.data.entry.api.EntryApiImpl
import com.octopus.edu.core.data.entry.store.EntryStore
import com.octopus.edu.core.data.entry.store.EntryStoreImpl
import com.octopus.edu.core.data.entry.store.ReminderStore
import com.octopus.edu.core.data.entry.store.ReminderStoreImpl
import com.octopus.edu.core.domain.repository.EntryRepository
import com.octopus.edu.core.domain.utils.ErrorClassifier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EntryDataModule {
    @Provides
    @Singleton
    fun provideEntryApi(api: FirebaseFirestore): EntryApi = EntryApiImpl(api)

    @Provides
    @Singleton
    fun providesEntryRepository(
        entryStore: EntryStore,
        entryApi: EntryApi,
        reminderStore: ReminderStore,
        dbSemaphore: Semaphore,
        @DatabaseErrorClassifierQualifier databaseErrorClassifier: ErrorClassifier,
        @NetworkErrorClassifierQualifier networkErrorClassifier: ErrorClassifier,
        entryLocks: ConcurrentHashMap<String, Mutex>,
        dispatcherProvider: DispatcherProvider
    ): EntryRepository =
        EntryRepositoryImpl(
            entryStore,
            entryApi,
            reminderStore,
            dbSemaphore,
            entryLocks,
            databaseErrorClassifier,
            networkErrorClassifier,
            dispatcherProvider,
        )

    @Provides
    @Singleton
    fun providesEntryStore(
        entryDao: EntryDao,
        deletedEntryDao: DeletedEntryDao,
        roomTransactionRunner: TransactionRunner
    ): EntryStore = EntryStoreImpl(entryDao, deletedEntryDao, roomTransactionRunner)

    @Provides
    @Singleton
    fun providesReminderStore(reminderDao: ReminderDao): ReminderStore = ReminderStoreImpl(reminderDao)

    @Provides
    @Singleton
    fun providesEntryDatabaseSemaphore(): Semaphore = Semaphore(BuildConfig.DB_SYNC_CONCURRENCY)

    @Provides
    @Singleton
    fun providesEntryLocks(): ConcurrentHashMap<String, Mutex> = ConcurrentHashMap()
}
