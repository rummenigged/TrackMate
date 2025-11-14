package com.octopus.edu.core.data.entry

import com.octopus.edu.core.common.DispatcherProvider
import com.octopus.edu.core.common.toEpochMilli
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity.PENDING
import com.octopus.edu.core.data.entry.di.DatabaseErrorClassifierQualifier
import com.octopus.edu.core.data.entry.di.EntryStoreQualifier
import com.octopus.edu.core.data.entry.store.EntryStore
import com.octopus.edu.core.data.entry.store.ReminderStore
import com.octopus.edu.core.data.entry.utils.EntryNotFoundException
import com.octopus.edu.core.data.entry.utils.getReminderAsEntity
import com.octopus.edu.core.data.entry.utils.toDomain
import com.octopus.edu.core.data.entry.utils.toEntity
import com.octopus.edu.core.data.entry.utils.toHabitOrNull
import com.octopus.edu.core.data.entry.utils.toTaskOrNull
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.model.appliesTo
import com.octopus.edu.core.domain.model.common.ErrorType.TransientError
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.repository.EntryRepository
import com.octopus.edu.core.domain.utils.ErrorClassifier
import com.octopus.edu.core.domain.utils.safeCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

internal class EntryRepositoryImpl
    @Inject
    constructor(
        @param:EntryStoreQualifier
        private val entryStore: EntryStore,
        private val reminderStore: ReminderStore,
        @param:DatabaseErrorClassifierQualifier
        private val databaseErrorClassifier: ErrorClassifier,
        private val dispatcherProvider: DispatcherProvider
    ) : EntryRepository {
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
                .getEntriesBeforeOrOn(date.toEpochMilli())
                .map { entries ->
                    ResultOperation.Success(
                        entries
                            .mapNotNull { entry -> entry.toDomain() }
                            .filter { entry -> entry is Task || (entry as Habit).appliesTo(date) },
                    ) as ResultOperation<List<Entry>>
                }.distinctUntilChanged()
                .catch { exception ->
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
                    isRetriableWhen = { exception ->
                        databaseErrorClassifier.classify(exception) is TransientError
                    },
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
                    ?: throw EntryNotFoundException("Invalid or missing entry with id $id")
            }

        override suspend fun deleteEntry(entryId: String): ResultOperation<Unit> =
            safeCall(
                dispatcher = dispatcherProvider.io,
                isRetriableWhen = { exception ->
                    databaseErrorClassifier.classify(exception) is TransientError
                },
            ) {
                entryStore.deleteEntry(entryId, PENDING)
            }

        override suspend fun markEntryAsDone(
            entryId: String,
            entryDate: LocalDate,
            isConfirmed: Boolean,
        ): ResultOperation<Unit> =
            safeCall(
                dispatcher = dispatcherProvider.io,
                isRetriableWhen = { exception ->
                    databaseErrorClassifier.classify(exception) is TransientError
                },
            ) {
                entryStore.markEntryAsDone(entryId, entryDate.toEpochMilli(), isConfirmed)
            }

        override suspend fun confirmEntryAsDone(
            entryId: String,
            entryDate: LocalDate
        ): ResultOperation<Unit> =
            safeCall(
                dispatcher = dispatcherProvider.io,
            ) {
                entryStore.confirmEntryAsDone(entryId, entryDate.toEpochMilli())
            }

        override suspend fun unmarkEntryAsDone(
            entryId: String,
            entryDate: LocalDate
        ): ResultOperation<Unit> =
            safeCall(
                dispatcher = dispatcherProvider.io,
            ) {
                entryStore.unmarkEntryAsDone(entryId, entryDate.toEpochMilli())
            }
    }
