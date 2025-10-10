package com.octopus.edu.core.data.entry

import android.database.sqlite.SQLiteException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreException.Code.DEADLINE_EXCEEDED
import com.google.firebase.firestore.FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED
import com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE
import com.octopus.edu.core.common.DispatcherProvider
import com.octopus.edu.core.common.toEpocMilliseconds
import com.octopus.edu.core.data.entry.api.EntryApi
import com.octopus.edu.core.data.entry.store.EntryStore
import com.octopus.edu.core.data.entry.store.ReminderStore
import com.octopus.edu.core.data.entry.utils.getReminderAsEntity
import com.octopus.edu.core.data.entry.utils.toDomain
import com.octopus.edu.core.data.entry.utils.toEntity
import com.octopus.edu.core.data.entry.utils.toHabitOrNull
import com.octopus.edu.core.data.entry.utils.toTaskOrNull
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.SyncState
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.model.appliesTo
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.repository.EntryRepository
import com.octopus.edu.core.domain.utils.safeCall
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalDate

internal class EntryRepositoryImpl
    @Inject
    constructor(
        private val entryStore: EntryStore,
        private val entryApi: EntryApi,
        private val reminderStore: ReminderStore,
        private val dispatcherProvider: DispatcherProvider
    ) : EntryRepository {
        override val pendingEntries: Flow<List<Entry>>
            get() =
                entryStore
                    .streamPendingEntries()
                    .map { entries ->
                        entries.mapNotNull { entry -> entry.toDomain() }
                    }

        override suspend fun getPendingEntries(): ResultOperation<List<Entry>> =
            safeCall(
                dispatcher = dispatcherProvider.io,
                onErrorReturn = { emptyList() },
            ) {
                entryStore.getPendingEntries().mapNotNull { entry -> entry.toDomain() }
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
                            isRetriable = exception is IOException || exception is SQLiteException,
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
                    exception is IOException || exception is SQLiteException
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
                entryStore.deleteEntry(entryId)
            }

        override suspend fun pushEntry(entry: Entry): ResultOperation<Unit> =
            safeCall(
                dispatcher = dispatcherProvider.io,
                isRetriableWhen = { exception ->
                    exception is FirebaseFirestoreException &&
                        (
                            exception.code == UNAVAILABLE ||
                                exception.code == DEADLINE_EXCEEDED ||
                                exception.code == RESOURCE_EXHAUSTED
                        )
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
    }
