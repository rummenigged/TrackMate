package com.octopus.edu.core.data.entry

import com.octopus.edu.core.common.DispatcherProvider
import com.octopus.edu.core.common.Logger
import com.octopus.edu.core.common.toEpocMilliseconds
import com.octopus.edu.core.data.database.entity.EntryEntity
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity.CONFLICT
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity.PENDING
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity.SYNCED
import com.octopus.edu.core.data.entry.api.EntryApi
import com.octopus.edu.core.data.entry.api.dto.DeletedEntryDto
import com.octopus.edu.core.data.entry.api.dto.EntryDto
import com.octopus.edu.core.data.entry.di.DatabaseErrorClassifierQualifier
import com.octopus.edu.core.data.entry.di.NetworkErrorClassifierQualifier
import com.octopus.edu.core.data.entry.store.EntryStore
import com.octopus.edu.core.data.entry.store.ReminderStore
import com.octopus.edu.core.data.entry.utils.getReminderAsEntity
import com.octopus.edu.core.data.entry.utils.toDomain
import com.octopus.edu.core.data.entry.utils.toEntity
import com.octopus.edu.core.data.entry.utils.toHabitOrNull
import com.octopus.edu.core.data.entry.utils.toTaskOrNull
import com.octopus.edu.core.domain.model.DeletedEntry
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.SyncState
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.model.appliesTo
import com.octopus.edu.core.domain.model.common.ErrorType.TransientError
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.repository.EntryRepository
import com.octopus.edu.core.domain.utils.ErrorClassifier
import com.octopus.edu.core.domain.utils.safeCall
import com.octopus.edu.core.network.utils.NetworkResponse
import jakarta.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

internal class EntryRepositoryImpl
    @Inject
    constructor(
        private val entryStore: EntryStore,
        private val entryApi: EntryApi,
        private val reminderStore: ReminderStore,
        private val dbSemaphore: Semaphore,
        private val entryLocks: ConcurrentHashMap<String, Mutex>,
        @field:DatabaseErrorClassifierQualifier
        private val databaseErrorClassifier: ErrorClassifier,
        @field:NetworkErrorClassifierQualifier
        private val networkErrorClassifier: ErrorClassifier,
        private val dispatcherProvider: DispatcherProvider
    ) : EntryRepository {
        override val pendingEntries: Flow<List<Entry>>
            get() =
                entryStore
                    .streamPendingEntries()
                    .map {
                        it.mapNotNull(EntryEntity::toDomain)
                    }.flowOn(dispatcherProvider.io)

        override val deletedEntryIds: Flow<List<String>>
            get() =
                entryStore
                    .streamPendingDeletedEntries()
                    .map {
                        it.map { deletedEntry -> deletedEntry.id }
                    }.flowOn(dispatcherProvider.io)

        /**
             * Retrieves the list of pending entries from the local store.
             *
             * The result contains domain `Entry` objects converted from stored entities; entries that cannot be converted are omitted.
             *
             * @return A `ResultOperation` holding the list of pending `Entry` objects. On error, the operation contains an empty list.
             */
            override suspend fun getPendingEntries(): ResultOperation<List<Entry>> =
            safeCall(
                dispatcher = dispatcherProvider.io,
                onErrorReturn = { emptyList() },
            ) {
                entryStore.getPendingEntries().mapNotNull(EntryEntity::toDomain)
            }

        /**
             * Fetches tasks stored locally and converts them to domain Task models.
             *
             * The operation ignores entries that cannot be converted to Task. If an error occurs,
             * the operation yields an empty list.
             *
             * @return A ResultOperation containing the list of retrieved Task objects; if retrieval fails, contains an empty list.
             */
            override suspend fun getTasks(): ResultOperation<List<Task>> =
            safeCall(
                dispatcher = dispatcherProvider.io,
                onErrorReturn = { emptyList() },
            ) {
                entryStore.getTasks().mapNotNull { task -> task.toTaskOrNull() }
            }

        override suspend fun getHabits(): ResultOperation<List<Habit>> =
            safeCall(
                dispatcher = dispatcherProvider.io,
                onErrorReturn = { emptyList() },
            ) {
                entryStore.getHabits().mapNotNull { task -> task.toHabitOrNull() }
            }

        /**
                 * Retrieve entries that are visible on the given date.
                 *
                 * Filters stored entries to domain models and returns only Task entries or Habit entries that apply to the provided date.
                 *
                 * @param date The date for which visible entries should be returned.
                 * @return `ResultOperation.Success` containing a list of matching domain entries, or `ResultOperation.Error` with the thrown throwable and `isRetriable` set to `true` when the database error classifier marks the exception as a transient error.
                 */
                override fun getEntriesVisibleOn(date: LocalDate): Flow<ResultOperation<List<Entry>>> =
            entryStore
                .getEntriesBeforeOrOn(date.toEpocMilliseconds())
                .map { entries ->
                    ResultOperation.Success(
                        entries
                            .mapNotNull { entry -> entry.toDomain() }
                            .filter { entry -> entry is Task || (entry as Habit).appliesTo(date) },
                    ) as ResultOperation<List<Entry>>
                }.catch { exception ->
                    val errorResult =
                        ResultOperation.Error(
                            throwable = exception,
                            isRetriable =
                                databaseErrorClassifier.classify(exception) is TransientError,
                        )
                    this.emit(errorResult)
                }.flowOn(dispatcherProvider.io)

        /**
         * Saves an entry and its reminder to local storage.
         *
         * @return A ResultOperation containing `Unit` on success, or an error describing the failure.
         */
        override suspend fun saveEntry(entry: Entry): ResultOperation<Unit> {
            val result =
                safeCall(
                    dispatcher = dispatcherProvider.io,
                ) {
                    entryStore.saveEntry(entry.toEntity())
                    reminderStore.saveReminder(entry.getReminderAsEntity())
                }
            return result
        }

        /**
             * Fetches the entry with the specified id from the local store.
             *
             * @param id The id of the entry to retrieve.
             * @return A ResultOperation containing the entry when successful.
             * @throws NoSuchElementException if no entry exists with the given id.
             */
            override suspend fun getEntryById(id: String): ResultOperation<Entry> =
            safeCall(
                dispatcher = dispatcherProvider.io,
                isRetriableWhen = { exception ->
                    databaseErrorClassifier.classify(exception) is TransientError
                },
            ) {
                entryStore
                    .getEntryById(id)
                    ?.toDomain()
                    ?: throw NoSuchElementException("Invalid or missing entry with id $id")
            }

        /**
             * Deletes the entry with the specified id from the local store by marking it with the PENDING sync state.
             *
             * @return `Unit` on success.
             */
            override suspend fun deleteEntry(entryId: String): ResultOperation<Unit> =
            safeCall(
                dispatcher = dispatcherProvider.io,
            ) {
                entryStore.deleteEntry(entryId, PENDING)
            }

        /**
             * Pushes the given entry to the remote API.
             *
             * Failures are considered retriable when the network error classifier classifies the exception as `TransientError`.
             *
             * @param entry The entry to send to the remote server.
             * @return `Unit` on success; on failure a `ResultOperation.Error` is returned (retriable when the classifier yields `TransientError`).
             */
            override suspend fun pushEntry(entry: Entry): ResultOperation<Unit> =
            safeCall(
                dispatcher = dispatcherProvider.io,
                isRetriableWhen = { exception ->
                    networkErrorClassifier.classify(exception) is TransientError
                },
            ) {
                entryApi.saveEntry(entry)
            }

        /**
             * Update the sync state of a local entry.
             *
             * @param entryId The identifier of the entry to update.
             * @param syncState The new sync state to set for the entry.
             * @return `Unit` on success.
             */
            override suspend fun updateEntrySyncState(
            entryId: String,
            syncState: SyncState
        ): ResultOperation<Unit> =
            safeCall(dispatcher = dispatcherProvider.io) {
                entryStore.updateEntrySyncState(entryId, syncState.toEntity())
            }

        /**
             * Synchronizes local data with the remote backend by concurrently fetching remote entries and deleted entries, then applying safe, concurrent local updates for new/updated entries and deletions.
             *
             * The function fetches both sets from the API in parallel; if both fetches succeed, it:
             * - updates or inserts remote entries that are not marked as deleted, and
             * - applies deletions for entries reported as deleted remotely.
             *
             * @return `Unit` on success.
             */
            override suspend fun syncEntries(): ResultOperation<Unit> =
            safeCall(
                dispatcher = dispatcherProvider.io,
            ) {
                coroutineScope {
                    val entriesDeferred = async { entryApi.fetchEntries() }
                    val deletedEntriesDeferred = async { entryApi.fetchDeletedEntry() }

                    val entries = entriesDeferred.await()
                    val deletedEntries = deletedEntriesDeferred.await()
                    if (entries is NetworkResponse.Success &&
                        deletedEntries is NetworkResponse.Success
                    ) {
                        val deletedIds = deletedEntries.data.map { it.id }.toSet()

                        entries.data
                            .filterNot { it.id in deletedIds }
                            .map { entry -> async { syncEntrySafely(entry) } }
                            .awaitAll()

                        deletedEntries.data
                            .map { entry -> async { syncDeletedEntrySafely(entry) } }
                            .awaitAll()
                    }
                }
            }

        /**
             * Retrieve the deleted entry with the given ID.
             *
             * @param entryId The identifier of the deleted entry to retrieve.
             * @return `ResultOperation.Success` containing the `DeletedEntry` when found, `ResultOperation.Error` if the entry is missing or a database error occurs.
             */
            override suspend fun getDeletedEntry(entryId: String): ResultOperation<DeletedEntry> =
            safeCall(
                dispatcher = dispatcherProvider.io,
                isRetriableWhen = { exception ->
                    databaseErrorClassifier.classify(exception) is TransientError
                },
            ) {
                entryStore
                    .getDeletedEntry(entryId)
                    ?.toDomain()
                    ?: throw NoSuchElementException("Invalid or missing deleted entry with id $entryId")
            }

        /**
             * Pushes a deleted entry to the remote API.
             *
             * The operation will be considered retriable when the network error classifier classifies an exception as `TransientError`.
             *
             * @param deletedEntry The deleted entry to push to the remote service.
             * @return `Unit` on success.
             */
            override suspend fun pushDeletedEntry(deletedEntry: DeletedEntry): ResultOperation<Unit> =
            safeCall(
                dispatcher = dispatcherProvider.io,
                isRetriableWhen = { exception ->
                    networkErrorClassifier.classify(exception) is TransientError
                },
            ) {
                entryApi.pushDeletedEntry(deletedEntry)
            }

        /**
             * Update the stored sync state for a deleted entry.
             *
             * @param entryId The id of the deleted entry to update.
             * @param syncState The target sync state to set for the deleted entry.
             * @return `Unit` on success.
             */
            override suspend fun updateDeletedEntrySyncState(
            entryId: String,
            syncState: SyncState
        ): ResultOperation<Unit> =
            safeCall(
                dispatcher = dispatcherProvider.io,
            ) {
                entryStore.updateDeletedEntrySyncState(entryId, syncState.toEntity())
            }

        /**
         * Persists a remote entry into the local store, marking it as conflicted if persistence fails.
         *
         * Attempts to upsert the provided `EntryDto` (using the store's "upsert if newest" semantics).
         * If any exception occurs while saving, updates the entry's sync state to `CONFLICT` and logs the error.
         *
         * @param entry The remote entry DTO to persist into the local store.
         */
        private suspend fun syncEntrySafely(entry: EntryDto) {
            dbSemaphore.withPermit {
                val mutex = entryLocks.computeIfAbsent(entry.id) { Mutex() }
                mutex.withLock {
                    try {
                        entryStore.upsertIfNewest(entry.toEntity())
                    } catch (e: Exception) {
                        entryStore.updateEntrySyncState(entry.id, CONFLICT)
                        Logger.e(
                            message = "Error to save entry ${entry.id}",
                            throwable = e,
                        )
                    } finally {
                        entryLocks.remove(entry.id)
                    }
                }
            }
        }

        /**
         * Synchronizes a deleted entry received from the remote source into the local store.
         *
         * If a corresponding local entry exists, it is removed and marked `SYNCED`; otherwise the deleted-entry record's
         * sync state is updated to `SYNCED`. Exceptions are caught and logged and do not propagate.
         *
         * @param entry The deleted entry DTO received from the remote source to be synchronized locally.
         */
        private suspend fun syncDeletedEntrySafely(entry: DeletedEntryDto) {
            dbSemaphore.withPermit {
                val mutex = entryLocks.computeIfAbsent(entry.id) { Mutex() }
                mutex.withLock {
                    try {
                        val localEntry = entryStore.getEntryById(entry.id)
                        if (localEntry != null) {
                            entryStore.deleteEntry(entry.id, SYNCED)
                        } else {
                            entryStore.updateDeletedEntrySyncState(entry.id, SYNCED)
                        }
                    } catch (e: Exception) {
                        Logger.e(message = "Error syncing deleted entry ${entry.id}", throwable = e)
                    } finally {
                        entryLocks.remove(entry.id)
                    }
                }
            }
        }
    }