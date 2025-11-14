package com.octopus.edu.core.data.entry

import com.octopus.edu.core.common.DispatcherProvider
import com.octopus.edu.core.common.Logger
import com.octopus.edu.core.common.toEpochMilli
import com.octopus.edu.core.data.database.entity.EntryEntity
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity.CONFLICT
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity.PENDING
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity.SYNCED
import com.octopus.edu.core.data.entry.api.EntryApi
import com.octopus.edu.core.data.entry.api.dto.DeletedEntryDto
import com.octopus.edu.core.data.entry.api.dto.DoneEntryDto
import com.octopus.edu.core.data.entry.api.dto.EntryDto
import com.octopus.edu.core.data.entry.di.DatabaseErrorClassifierQualifier
import com.octopus.edu.core.data.entry.di.EntryStoreQualifier
import com.octopus.edu.core.data.entry.di.NetworkErrorClassifierQualifier
import com.octopus.edu.core.data.entry.store.EntryStore
import com.octopus.edu.core.data.entry.utils.EntryNotFoundException
import com.octopus.edu.core.data.entry.utils.toDomain
import com.octopus.edu.core.data.entry.utils.toEntity
import com.octopus.edu.core.domain.model.DeletedEntry
import com.octopus.edu.core.domain.model.DoneEntry
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.SyncState
import com.octopus.edu.core.domain.model.common.ErrorType.TransientError
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.repository.EntrySyncRepository
import com.octopus.edu.core.domain.utils.ErrorClassifier
import com.octopus.edu.core.domain.utils.safeCall
import com.octopus.edu.core.network.utils.NetworkResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class EntrySyncRepositoryImpl
    @Inject
    constructor(
        @param:EntryStoreQualifier
        private val entryStore: EntryStore,
        private val entryApi: EntryApi,
        private val dbSemaphore: Semaphore,
        private val entryLocks: ConcurrentHashMap<String, Mutex>,
        @param:DatabaseErrorClassifierQualifier
        private val databaseErrorClassifier: ErrorClassifier,
        @param:NetworkErrorClassifierQualifier
        private val networkErrorClassifier: ErrorClassifier,
        private val dispatcherProvider: DispatcherProvider
    ) : EntrySyncRepository {
        override val pendingEntries: Flow<List<Entry>>
            get() =
                entryStore
                    .streamPendingEntries()
                    .map { it.mapNotNull(EntryEntity::toDomain) }
                    .flowOn(dispatcherProvider.io)

        override val deletedEntryIds: Flow<List<String>>
            get() =
                entryStore
                    .streamPendingDeletedEntries()
                    .map { it.map { deletedEntry -> deletedEntry.id } }
                    .flowOn(dispatcherProvider.io)

        override val pendingEntriesMarkedAsDone: Flow<List<DoneEntry>>
            get() =
                entryStore
                    .streamPendingDoneEntries()
                    .map { it.map { entry -> entry.toDomain() } }
                    .flowOn(dispatcherProvider.io)

        override suspend fun syncEntries(): ResultOperation<Unit> =
            safeCall(
                dispatcher = dispatcherProvider.io,
                isRetriableWhen = { exception ->
                    networkErrorClassifier.classify(exception) is TransientError ||
                        databaseErrorClassifier.classify(exception) is TransientError
                },
            ) {
                coroutineScope {
                    val entriesDeferred = async { entryApi.fetchEntries() }
                    val deletedEntriesDeferred = async { entryApi.fetchDeletedEntries() }
                    val doneEntriesDeferred = async { entryApi.fetchDoneEntries() }

                    val entries = entriesDeferred.await()
                    val deletedEntries = deletedEntriesDeferred.await()
                    val doneEntries = doneEntriesDeferred.await()

                    when {
                        entries !is NetworkResponse.Success ->
                            throw Exception("Error fetching entries")

                        deletedEntries !is NetworkResponse.Success ->
                            throw Exception("Error fetching deleted entries")

                        doneEntries !is NetworkResponse.Success ->
                            throw Exception("Error fetching done entries")

                        else -> {
                            val deletedIds = deletedEntries.data.map { it.id }.toSet()

                            entries.data
                                .filterNot { it.id in deletedIds }
                                .map { entry -> async { syncEntrySafely(entry) } }
                                .awaitAll()

                            doneEntries.data
                                .filterNot { it.id in deletedIds }
                                .map { entry -> async { syncDoneEntriesSafely(entry) } }
                                .awaitAll()

                            deletedEntries.data
                                .map { entry -> async { syncDeletedEntrySafely(entry) } }
                                .awaitAll()
                        }
                    }
                }
            }

        override suspend fun pushEntry(entry: Entry): ResultOperation<Unit> =
            safeCall(
                dispatcher = dispatcherProvider.io,
                isRetriableWhen = { exception ->
                    networkErrorClassifier.classify(exception) is TransientError
                },
            ) {
                val result = entryApi.saveEntry(entry)
                if (result is NetworkResponse.Error) throw result.exception
            }

        override suspend fun updateEntrySyncState(
            entryId: String,
            syncState: SyncState
        ): ResultOperation<Unit> =
            safeCall(
                dispatcher = dispatcherProvider.io,
                isRetriableWhen = { exception ->
                    databaseErrorClassifier.classify(exception) is TransientError
                },
            ) {
                entryStore.updateEntrySyncState(entryId, syncState.toEntity())
            }

        override suspend fun getPendingEntries(): ResultOperation<List<Entry>> =
            safeCall(
                dispatcher = dispatcherProvider.io,
                onErrorReturn = { emptyList() },
            ) {
                entryStore.getPendingEntries().mapNotNull(EntryEntity::toDomain)
            }

        override suspend fun getDoneEntry(
            entryId: String,
            entryDate: LocalDate
        ): ResultOperation<DoneEntry> =
            safeCall(
                dispatcher = dispatcherProvider.io,
                isRetriableWhen = { exception ->
                    databaseErrorClassifier.classify(exception) is TransientError
                },
            ) {
                entryStore
                    .getDoneEntry(entryId, entryDate.toEpochMilli())
                    ?.toDomain()
                    ?: throw EntryNotFoundException("Invalid or missing done entry with id $entryId")
            }

        override suspend fun pushDoneEntry(entry: DoneEntry): ResultOperation<Unit> =
            safeCall(
                dispatcher = dispatcherProvider.io,
                isRetriableWhen = { exception ->
                    networkErrorClassifier.classify(exception) is TransientError
                },
            ) {
                val result = entryApi.pushDoneEntry(entry)
                if (result is NetworkResponse.Error) throw result.exception
            }

        override suspend fun updateDoneEntrySyncState(
            entryId: String,
            entryDate: LocalDate,
            syncState: SyncState
        ): ResultOperation<Unit> =
            safeCall(
                dispatcher = dispatcherProvider.io,
                isRetriableWhen =
                    { exception ->
                        databaseErrorClassifier.classify(exception) is TransientError
                    },
            ) {
                entryStore.updateDoneEntrySyncState(
                    entryId,
                    entryDate.toEpochMilli(),
                    syncState.toEntity(),
                )
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
                    ?: throw EntryNotFoundException("Invalid or missing deleted entry with id $entryId")
            }

        override suspend fun pushDeletedEntry(deletedEntry: DeletedEntry): ResultOperation<Unit> =
            safeCall(
                dispatcher = dispatcherProvider.io,
                isRetriableWhen = { exception ->
                    networkErrorClassifier.classify(exception) is TransientError
                },
            ) {
                val result = entryApi.pushDeletedEntry(deletedEntry)
                if (result is NetworkResponse.Error) throw result.exception
            }

        override suspend fun updateDeletedEntrySyncState(
            entryId: String,
            syncState: SyncState
        ): ResultOperation<Unit> =
            safeCall(
                dispatcher = dispatcherProvider.io,
                isRetriableWhen = { exception ->
                    databaseErrorClassifier.classify(exception) is TransientError
                },
            ) {
                entryStore.updateDeletedEntrySyncState(entryId, syncState.toEntity())
            }

        private suspend fun syncEntrySafely(entry: EntryDto) {
            dbSemaphore.withPermit {
                val mutex = entryLocks.computeIfAbsent(entry.id) { Mutex() }
                try {
                    mutex.withLock {
                        try {
                            entryStore.upsertIfNewest(entry.toEntity())
                        } catch (e: Exception) {
                            entryStore.updateEntrySyncState(entry.id, CONFLICT)
                            Logger.e(
                                message = "Error to save entry ${entry.id}",
                                throwable = e,
                            )
                        }
                    }
                } finally {
                    entryLocks.remove(entry.id, mutex)
                }
            }
        }

        private suspend fun syncDoneEntriesSafely(entry: DoneEntryDto) {
            dbSemaphore.withPermit {
                val mutex = entryLocks.computeIfAbsent(entry.id) { Mutex() }
                try {
                    mutex.withLock {
                        val doneEntryEntity = entry.toEntity()
                        try {
                            entryStore.upsertDoneEntryIfOldest(doneEntryEntity)
                        } catch (e: Exception) {
                            entryStore.updateDoneEntrySyncState(
                                doneEntryEntity.entryId,
                                doneEntryEntity.entryDate,
                                CONFLICT,
                            )
                            Logger.e(
                                message = "Error to save done entry ${entry.id}",
                                throwable = e,
                            )
                        }
                    }
                } finally {
                    entryLocks.remove(entry.id, mutex)
                }
            }
        }

        private suspend fun syncDeletedEntrySafely(entry: DeletedEntryDto) {
            dbSemaphore.withPermit {
                val mutex = entryLocks.computeIfAbsent(entry.id) { Mutex() }
                try {
                    mutex.withLock {
                        val localEntry = entryStore.getEntryById(entry.id)
                        if (localEntry != null && localEntry.syncState == PENDING) {
                            entryStore.updateEntrySyncState(entry.id, CONFLICT)
                            Logger.w(
                                message =
                                    "Entry ${entry.id} has pending changes but was " +
                                        "deleted remotely. Marked as CONFLICT",
                            )
                        } else if (localEntry != null) {
                            entryStore.deleteEntry(entry.id, SYNCED)
                        } else {
                            entryStore.updateDeletedEntrySyncState(entry.id, SYNCED)
                        }
                    }
                } catch (e: Exception) {
                    Logger.e(message = "Error syncing deleted entry ${entry.id}", throwable = e)
                } finally {
                    entryLocks.remove(entry.id, mutex)
                }
            }
        }
    }
