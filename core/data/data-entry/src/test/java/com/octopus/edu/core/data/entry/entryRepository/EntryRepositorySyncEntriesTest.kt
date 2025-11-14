package com.octopus.edu.core.data.entry.entryRepository

import com.google.firebase.Timestamp
import com.octopus.edu.core.common.toInstant
import com.octopus.edu.core.data.database.entity.EntryEntity
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity
import com.octopus.edu.core.data.entry.EntrySyncRepositoryImpl
import com.octopus.edu.core.data.entry.api.EntryApi
import com.octopus.edu.core.data.entry.api.dto.DeletedEntryDto
import com.octopus.edu.core.data.entry.api.dto.DoneEntryDto
import com.octopus.edu.core.data.entry.api.dto.EntryDto
import com.octopus.edu.core.data.entry.store.EntryStore
import com.octopus.edu.core.domain.model.common.ErrorType
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.repository.EntrySyncRepository
import com.octopus.edu.core.domain.utils.ErrorClassifier
import com.octopus.edu.core.network.utils.NetworkResponse
import com.octopus.edu.core.testing.TestDispatchers
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class EntryRepositorySyncEntriesTest {
    private lateinit var syncRepository: EntrySyncRepository
    private val entryStore: EntryStore = mockk()
    private val entryApi: EntryApi = mockk()
    private val dispatcherProvider = TestDispatchers()
    private val dbSemaphore = Semaphore(Int.MAX_VALUE)
    private val entryLocks = ConcurrentHashMap<String, Mutex>()
    private val databaseErrorClassifier: ErrorClassifier = mockk()
    private val networkErrorClassifier: ErrorClassifier = mockk()

    @Before
    fun setUp() {
        mockkStatic("com.octopus.edu.core.data.entry.utils.EntityMappingExtensionsKt")
        // Mocks for successful operations by default
        coEvery { entryStore.upsertIfNewest(any()) } just runs
        coEvery { entryStore.upsertDoneEntryIfOldest(any()) } just runs
        coEvery { entryStore.updateEntrySyncState(any(), any()) } just runs
        coEvery { entryStore.updateDoneEntrySyncState(any(), any(), any()) } just runs
        coJustRun { entryStore.deleteEntry(any(), any()) }
        coJustRun { entryStore.updateDeletedEntrySyncState(any(), any()) }
        coEvery { entryStore.getEntryById(any()) } returns null

        // Mock API calls to return empty lists by default to isolate tests
        coEvery { entryApi.fetchEntries() } returns NetworkResponse.Success(emptyList())
        coEvery { entryApi.fetchDeletedEntries() } returns NetworkResponse.Success(emptyList())
        coEvery { entryApi.fetchDoneEntries() } returns NetworkResponse.Success(emptyList())

        syncRepository =
            EntrySyncRepositoryImpl(
                entryStore = entryStore,
                entryApi = entryApi,
                dbSemaphore = dbSemaphore,
                entryLocks = entryLocks,
                databaseErrorClassifier,
                networkErrorClassifier,
                dispatcherProvider = dispatcherProvider,
            )
    }

    // region Sync Remote Entries Tests
    @Test
    fun `syncEntries should upsert entries when api returns success`() =
        runTest {
            // Given
            val remoteEntries = listOf(createEntryDto("1"), createEntryDto("2"))
            coEvery { entryApi.fetchEntries() } returns NetworkResponse.Success(remoteEntries)

            // When
            val result = syncRepository.syncEntries()

            // Then
            assertTrue(result is ResultOperation.Success)
            coVerify(exactly = 1) { entryStore.upsertIfNewest(match { it.id == "1" }) }
            coVerify(exactly = 1) { entryStore.upsertIfNewest(match { it.id == "2" }) }
            coVerify(exactly = 0) { entryStore.deleteEntry(any(), any()) }
        }

    @Test
    fun `syncEntries returns Error when fetchEntries fails`() =
        runTest {
            // Given
            val apiException = IOException()
            coEvery { entryApi.fetchEntries() } returns NetworkResponse.Error(apiException)
            every { networkErrorClassifier.classify(any()) } returns ErrorType.TransientError(apiException)

            // When
            val result = syncRepository.syncEntries()

            // Then
            assertTrue(result is ResultOperation.Error)
            assertTrue(result.isRetriable)
            coVerify(exactly = 0) { entryStore.upsertIfNewest(any()) }
            coVerify(exactly = 0) { entryStore.deleteEntry(any(), any()) }
        }

    @Test
    fun `syncEntries should update sync state to CONFLICT when upsert fails`() =
        runTest {
            // Given
            val remoteEntry = createEntryDto("1")
            coEvery { entryApi.fetchEntries() } returns NetworkResponse.Success(listOf(remoteEntry))
            coEvery { entryStore.upsertIfNewest(any()) } throws Exception("Database write failed")

            // When
            syncRepository.syncEntries()

            // Then
            coVerify(exactly = 1) { entryStore.updateEntrySyncState("1", SyncStateEntity.CONFLICT) }
        }

    @Test
    fun `syncEntries processes other entries even if one fails`() =
        runTest {
            // Given
            val remoteEntries = listOf(createEntryDto("1"), createEntryDto("2"))
            coEvery { entryApi.fetchEntries() } returns NetworkResponse.Success(remoteEntries)
            coEvery { entryStore.upsertIfNewest(match { it.id == "1" }) } throws Exception("DB error")

            // When
            syncRepository.syncEntries()

            // Then
            coVerify(exactly = 1) { entryStore.updateEntrySyncState("1", SyncStateEntity.CONFLICT) }
            coVerify(exactly = 1) { entryStore.upsertIfNewest(match { it.id == "2" }) }
        }
    // endregion

    // region Sync Remote Deleted Entries Tests
    @Test
    fun `syncEntries should delete local entry when remote deleted entry is found`() =
        runTest {
            // Given
            val deletedDto = createDeletedEntryDto("deleted-1")
            val localEntry = createEntryEntity("deleted-1")
            coEvery { entryApi.fetchDeletedEntries() } returns NetworkResponse.Success(listOf(deletedDto))
            coEvery { entryStore.getEntryById("deleted-1") } returns localEntry

            // When
            syncRepository.syncEntries()

            // Then
            coVerify(exactly = 1) { entryStore.deleteEntry("deleted-1", SyncStateEntity.SYNCED) }
            coVerify(exactly = 0) { entryStore.updateDeletedEntrySyncState(any(), any()) }
        }

    @Test
    fun `syncEntries should mark local entry as CONFLICT if it has PENDING changes`() =
        runTest {
            // Given
            val deletedDto = createDeletedEntryDto("conflict-id")
            val localPendingEntry = createEntryEntity("conflict-id", syncState = SyncStateEntity.PENDING)
            coEvery { entryApi.fetchDeletedEntries() } returns NetworkResponse.Success(listOf(deletedDto))
            coEvery { entryStore.getEntryById("conflict-id") } returns localPendingEntry

            // When
            syncRepository.syncEntries()

            // Then
            coVerify(exactly = 1) { entryStore.updateEntrySyncState("conflict-id", SyncStateEntity.CONFLICT) }
            coVerify(exactly = 0) { entryStore.deleteEntry(any(), any()) }
            coVerify(exactly = 0) { entryStore.updateDeletedEntrySyncState(any(), any()) }
        }

    @Test
    fun `syncEntries should update remote deleted entry state if not found locally`() =
        runTest {
            // Given
            val deletedDto = createDeletedEntryDto("deleted-1")
            coEvery { entryApi.fetchDeletedEntries() } returns NetworkResponse.Success(listOf(deletedDto))
            coEvery { entryStore.getEntryById("deleted-1") } returns null

            // When
            syncRepository.syncEntries()

            // Then
            coVerify(exactly = 1) { entryStore.updateDeletedEntrySyncState("deleted-1", SyncStateEntity.SYNCED) }
            coVerify(exactly = 0) { entryStore.deleteEntry(any(), any()) }
        }

    @Test
    fun `syncEntries returns Error when fetchDeletedEntry fails`() =
        runTest {
            // Given
            val apiException = IOException()
            coEvery { entryApi.fetchEntries() } returns NetworkResponse.Success(listOf(createEntryDto("1")))
            coEvery { entryApi.fetchDeletedEntries() } returns NetworkResponse.Error(apiException)
            every { networkErrorClassifier.classify(any()) } returns ErrorType.TransientError(apiException)

            // When
            val result = syncRepository.syncEntries()

            // Then
            assertTrue(result is ResultOperation.Error)
            assertTrue(result.isRetriable)
            coVerify(exactly = 0) { entryStore.upsertIfNewest(any()) }
            coVerify(exactly = 0) { entryStore.deleteEntry(any(), any()) }
        }

    @Test
    fun `syncEntries continues deleting other entries if one fails`() =
        runTest {
            // Given
            val deletedDtos = listOf(createDeletedEntryDto("d1"), createDeletedEntryDto("d2"))
            coEvery { entryApi.fetchDeletedEntries() } returns NetworkResponse.Success(deletedDtos)
            coEvery { entryStore.getEntryById(any()) } returns createEntryEntity("any")
            coEvery { entryStore.deleteEntry("d1", SyncStateEntity.SYNCED) } throws IOException()

            // When
            syncRepository.syncEntries()

            // Then
            coVerify(exactly = 1) { entryStore.deleteEntry("d1", SyncStateEntity.SYNCED) } // attempted
            coVerify(exactly = 1) { entryStore.deleteEntry("d2", SyncStateEntity.SYNCED) } // still runs
        }
    // endregion

    // region Sync Remote Done Entries Tests
    @Test
    fun `syncEntries should upsert done entries when api returns success`() =
        runTest {
            // Given
            val remoteDoneEntries = listOf(createDoneEntryDto("d1"), createDoneEntryDto("d2"))
            coEvery { entryApi.fetchDoneEntries() } returns NetworkResponse.Success(remoteDoneEntries)

            // When
            val result = syncRepository.syncEntries()

            // Then
            assertTrue(result is ResultOperation.Success)
            coVerify(exactly = 1) { entryStore.upsertDoneEntryIfOldest(match { it.entryId == "d1" }) }
            coVerify(exactly = 1) { entryStore.upsertDoneEntryIfOldest(match { it.entryId == "d2" }) }
        }

    @Test
    fun `syncEntries returns Error when fetchDoneEntries fails`() =
        runTest {
            // Given
            val apiException = IOException()
            coEvery { entryApi.fetchDoneEntries() } returns NetworkResponse.Error(apiException)
            every { networkErrorClassifier.classify(any()) } returns ErrorType.TransientError(apiException)

            // When
            val result = syncRepository.syncEntries()

            // Then
            assertTrue(result is ResultOperation.Error)
            assertTrue(result.isRetriable)
            coVerify(exactly = 0) { entryStore.upsertIfNewest(any()) }
            coVerify(exactly = 0) { entryStore.deleteEntry(any(), any()) }
            coVerify(exactly = 0) { entryStore.upsertDoneEntryIfOldest(any()) }
        }

    @Test
    fun `syncEntries should update done entry sync state to CONFLICT when upsert fails`() =
        runTest {
            // Given
            val remoteDoneEntry = createDoneEntryDto("d1")
            coEvery { entryApi.fetchDoneEntries() } returns NetworkResponse.Success(listOf(remoteDoneEntry))
            coEvery { entryStore.upsertDoneEntryIfOldest(any()) } throws Exception("DB write failed")

            // When
            syncRepository.syncEntries()

            // Then
            coVerify(exactly = 1) {
                entryStore.updateDoneEntrySyncState(
                    "d1",
                    remoteDoneEntry.date!!.toInstant().toEpochMilli(),
                    SyncStateEntity.CONFLICT,
                )
            }
        }

    @Test
    fun `syncEntries should not sync done entry that is also marked as deleted`() =
        runTest {
            // Given
            val conflictingId = "conflict-id"
            val doneEntryDto = createDoneEntryDto(conflictingId)
            val deletedEntryDto = createDeletedEntryDto(conflictingId)

            coEvery { entryApi.fetchDoneEntries() } returns NetworkResponse.Success(listOf(doneEntryDto))
            coEvery { entryApi.fetchDeletedEntries() } returns NetworkResponse.Success(listOf(deletedEntryDto))

            // When
            syncRepository.syncEntries()

            // Then
            coVerify(exactly = 0) { entryStore.upsertDoneEntryIfOldest(any()) }
            coVerify(exactly = 1) { entryStore.getEntryById(conflictingId) } // To check for deletion
        }
    // endregion

    // region Combined Sync Logic Tests
    @Test
    fun `syncEntries should handle all entry types in one run`() =
        runTest {
            // Given
            val newEntryDto = createEntryDto("new-1")
            val deletedEntryDto = createDeletedEntryDto("deleted-1")
            val doneEntryDto = createDoneEntryDto("done-1")
            val localEntryForDeletion = createEntryEntity("deleted-1")

            coEvery { entryApi.fetchEntries() } returns NetworkResponse.Success(listOf(newEntryDto))
            coEvery { entryApi.fetchDeletedEntries() } returns NetworkResponse.Success(listOf(deletedEntryDto))
            coEvery { entryApi.fetchDoneEntries() } returns NetworkResponse.Success(listOf(doneEntryDto))
            coEvery { entryStore.getEntryById("deleted-1") } returns localEntryForDeletion

            // When
            syncRepository.syncEntries()

            // Then
            coVerify(exactly = 1) { entryStore.upsertIfNewest(match { it.id == "new-1" }) }
            coVerify(exactly = 1) { entryStore.deleteEntry("deleted-1", SyncStateEntity.SYNCED) }
            coVerify(exactly = 1) { entryStore.upsertDoneEntryIfOldest(match { it.entryId == "done-1" }) }
        }

    @Test
    fun `syncEntries should not sync entry that is also marked as deleted`() =
        runTest {
            // Given
            val conflictingId = "conflict-id"
            val newEntryDto = createEntryDto("new-1")
            val conflictingEntryDto = createEntryDto(conflictingId)
            val deletedEntryDto = createDeletedEntryDto(conflictingId)
            val localEntryForDeletion = createEntryEntity(conflictingId)

            coEvery { entryApi.fetchEntries() } returns NetworkResponse.Success(listOf(newEntryDto, conflictingEntryDto))
            coEvery { entryApi.fetchDeletedEntries() } returns NetworkResponse.Success(listOf(deletedEntryDto))
            coEvery { entryStore.getEntryById(conflictingId) } returns localEntryForDeletion

            // When
            syncRepository.syncEntries()

            // Then
            // It should sync the new entry
            coVerify(exactly = 1) { entryStore.upsertIfNewest(match { it.id == "new-1" }) }
            // It should NOT sync the conflicting entry because it's in the deleted list
            coVerify(exactly = 0) { entryStore.upsertIfNewest(match { it.id == conflictingId }) }
            // It should process the deletion for the conflicting entry
            coVerify(exactly = 1) { entryStore.deleteEntry(conflictingId, SyncStateEntity.SYNCED) }
        }
    // endregion

    private fun createEntryDto(
        id: String,
        updatedAt: Timestamp = Timestamp.now()
    ): EntryDto {
        return EntryDto(id = id, title = "Title $id", updatedAt = updatedAt)
    }

    private fun createEntryEntity(
        id: String,
        updatedAt: Long? = 0,
        syncState: SyncStateEntity = SyncStateEntity.SYNCED
    ): EntryEntity {
        return EntryEntity(
            id = id,
            title = "Title $id",
            description = "",
            isDone = false,
            type = EntryEntity.EntryType.TASK,
            createdAt = 0,
            updatedAt = updatedAt,
            syncState = syncState,
            dueDate = null,
            startDate = null,
            isArchived = false,
            lastCompletedDate = null,
            recurrence = null,
            streakCount = null,
            time = null,
        )
    }

    private fun createDeletedEntryDto(
        id: String,
        deletedAt: Timestamp = Timestamp.now()
    ): DeletedEntryDto {
        return DeletedEntryDto(id = id, deletedAt = deletedAt)
    }

    private fun createDoneEntryDto(
        id: String,
        date: Timestamp = Timestamp.now(),
        doneAt: Timestamp = Timestamp.now()
    ): DoneEntryDto {
        return DoneEntryDto(id = id, date = date, doneAt = doneAt)
    }
}
