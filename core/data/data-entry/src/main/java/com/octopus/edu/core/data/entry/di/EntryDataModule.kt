package com.octopus.edu.core.data.entry.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.octopus.edu.core.common.DispatcherProvider
import com.octopus.edu.core.common.TransactionRunner
import com.octopus.edu.core.data.database.dao.DeletedEntryDao
import com.octopus.edu.core.data.database.dao.EntryDao
import com.octopus.edu.core.data.database.dao.ReminderDao
import com.octopus.edu.core.data.entry.BuildConfig
import com.octopus.edu.core.data.entry.EntryRepositoryImpl
import com.octopus.edu.core.data.entry.UserPreferencesProvider
import com.octopus.edu.core.data.entry.UserPreferencesProviderImpl
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
    /**
     * Provides an EntryApi implementation that uses Firestore and user preferences.
     *
     * @param userPreferencesProvider Supplies user-specific settings used by the API.
     * @return An `EntryApi` implementation backed by the provided `FirebaseFirestore` instance and `UserPreferencesProvider`.
     */
    @Provides
    @Singleton
    fun provideEntryApi(
        api: FirebaseFirestore,
        userPreferencesProvider: UserPreferencesProvider
    ): EntryApi = EntryApiImpl(api, userPreferencesProvider)

    /**
         * Provides a singleton EntryRepository wired with persistence, remote API, reminders, concurrency controls, error classification, and dispatchers.
         *
         * @param dbSemaphore Semaphore that limits concurrent database synchronization operations.
         * @param databaseErrorClassifier Classifier used to interpret and map database-related errors.
         * @param networkErrorClassifier Classifier used to interpret and map network-related errors.
         * @param entryLocks Concurrent map of per-entry `Mutex` instances keyed by entry identifier for fine-grained synchronization.
         * @param dispatcherProvider Provides coroutine dispatchers used by repository operations.
         * @return An EntryRepository instance configured with the provided store, API, reminder store, concurrency primitives, error classifiers, and dispatcher provider.
         */
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

    /**
     * Provides an EntryStore implementation backed by Room DAOs and a transaction runner.
     *
     * @param entryDao DAO for accessing entry records.
     * @param deletedEntryDao DAO for accessing deleted entry records.
     * @param roomTransactionRunner Runner that executes Room transactions.
     * @return An EntryStore instance that uses the provided DAOs and transaction runner.
     */
    @Provides
    @Singleton
    fun providesEntryStore(
        entryDao: EntryDao,
        deletedEntryDao: DeletedEntryDao,
        roomTransactionRunner: TransactionRunner
    ): EntryStore = EntryStoreImpl(entryDao, deletedEntryDao, roomTransactionRunner)

    /**
     * Supplies a ReminderStore implementation backed by the given ReminderDao.
     *
     * @return A ReminderStore that uses the provided ReminderDao for persistence.
     */
    @Provides
    @Singleton
    fun providesReminderStore(reminderDao: ReminderDao): ReminderStore = ReminderStoreImpl(reminderDao)

    /**
     * Provides a Semaphore that limits concurrent database synchronization operations.
     *
     * @return A `Semaphore` initialized with `BuildConfig.DB_SYNC_CONCURRENCY` permits. 
     */
    @Provides
    @Singleton
    fun providesEntryDatabaseSemaphore(): Semaphore = Semaphore(BuildConfig.DB_SYNC_CONCURRENCY)

    /**
     * Thread-safe map holding `Mutex` locks keyed by entry identifiers.
     *
     * @return A `ConcurrentHashMap` where keys are entry IDs and values are `Mutex` instances used for per-entry synchronization.
     */
    @Provides
    @Singleton
    fun providesEntryLocks(): ConcurrentHashMap<String, Mutex> = ConcurrentHashMap()

    /**
     * Provides a singleton UserPreferencesProvider that exposes preferences for the current Firebase authenticated user.
     *
     * @return A UserPreferencesProvider backed by the supplied FirebaseAuth instance.
     */
    @Provides
    @Singleton
    fun provideUserPreferencesProvider(firebaseAuth: FirebaseAuth): UserPreferencesProvider = UserPreferencesProviderImpl(firebaseAuth)
}