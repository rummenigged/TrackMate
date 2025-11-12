package com.octopus.edu.core.data.entry

import com.octopus.edu.core.common.DispatcherProvider
import com.octopus.edu.core.common.toEpochMilli
import com.octopus.edu.core.data.entry.api.EntryApi
import com.octopus.edu.core.data.entry.di.DatabaseErrorClassifierQualifier
import com.octopus.edu.core.data.entry.di.EntryStoreQualifier
import com.octopus.edu.core.data.entry.di.NetworkErrorClassifierQualifier
import com.octopus.edu.core.data.entry.store.EntryStore
import com.octopus.edu.core.data.entry.utils.EntryNotFoundException
import com.octopus.edu.core.data.entry.utils.toDomain
import com.octopus.edu.core.data.entry.utils.toEntity
import com.octopus.edu.core.domain.model.DoneEntry
import com.octopus.edu.core.domain.model.SyncState
import com.octopus.edu.core.domain.model.common.ErrorType.TransientError
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.repository.EntrySyncRepository
import com.octopus.edu.core.domain.utils.ErrorClassifier
import com.octopus.edu.core.domain.utils.safeCall
import com.octopus.edu.core.network.utils.NetworkResponse
import java.time.LocalDate
import javax.inject.Inject

class EntrySyncRepositoryImpl
    @Inject
    constructor(
        @param:EntryStoreQualifier
        private val entryStore: EntryStore,
        private val entryApi: EntryApi,
        @param:DatabaseErrorClassifierQualifier
        private val databaseErrorClassifier: ErrorClassifier,
        @param:NetworkErrorClassifierQualifier
        private val networkErrorClassifier: ErrorClassifier,
        private val dispatcherProvider: DispatcherProvider
    ) : EntrySyncRepository {
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
    }
