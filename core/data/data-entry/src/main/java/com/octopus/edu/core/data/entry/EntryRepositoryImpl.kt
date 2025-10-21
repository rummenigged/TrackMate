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

        override suspend fun getPendingEntries(): ResultOperation<List<Entry>> =
            safeCall(
                dispatcher = dispatcherProvider.io,
                onErrorReturn = { emptyList() },
            ) {
                entryStore.getPendingEntries().mapNotNull(EntryEntity::toDomain)
            }

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

        override suspend fun deleteEntry(entryId: String): ResultOperation<Unit> =
            safeCall(
                dispatcher = dispatcherProvider.io,
            ) {
                entryStore.deleteEntry(entryId, PENDING)
            }

        override suspend fun pushEntry(entry: Entry): ResultOperation<Unit> =
            safeCall(
                dispatcher = dispatcherProvider.io,
                isRetriableWhen = { exception ->
                    networkErrorClassifier.classify(exception) is TransientError
                },
            ) {
                entryApi.saveEntry(entry)
            }

        override suspend fun updateEntrySyncState(
            entryId: String,
            syncState: SyncState
        ): ResultOperation<Unit> =
            safeCall(dispatcher = dispatcherProvider.io) {
                entryStore.updateEntrySyncState(entryId, syncState.toEntity())
            }

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

        override suspend fun pushDeletedEntry(deletedEntry: DeletedEntry): ResultOperation<Unit> =
            safeCall(
                dispatcher = dispatcherProvider.io,
                isRetriableWhen = { exception ->
                    networkErrorClassifier.classify(exception) is TransientError
                },
            ) {
                entryApi.pushDeletedEntry(deletedEntry)
            }

        override suspend fun updateDeletedEntrySyncState(
            entryId: String,
            syncState: SyncState
        ): ResultOperation<Unit> =
            safeCall(
                dispatcher = dispatcherProvider.io,
            ) {
                entryStore.updateDeletedEntrySyncState(entryId, syncState.toEntity())
            }

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
