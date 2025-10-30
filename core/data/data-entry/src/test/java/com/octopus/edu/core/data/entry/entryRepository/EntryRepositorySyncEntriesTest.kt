package com.octopus.edu.core.data.entry.entryRepository

import com.google.firebase.Timestamp
import com.octopus.edu.core.data.database.entity.EntryEntity
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity
import com.octopus.edu.core.data.entry.EntryRepositoryImpl
import com.octopus.edu.core.data.entry.api.EntryApi
import com.octopus.edu.core.data.entry.api.dto.DeletedEntryDto
import com.octopus.edu.core.data.entry.api.dto.EntryDto
import com.octopus.edu.core.data.entry.store.EntryStore
import com.octopus.edu.core.data.entry.store.ReminderStore
import com.octopus.edu.core.domain.model.common.ErrorType
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.utils.ErrorClassifier
import com.octopus.edu.core.network.utils.NetworkResponse
import com.octopus.edu.core.testing.TestDispatchers
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class EntryRepositorySyncEntriesTest {
    private lateinit var entryRepository: EntryRepositoryImpl
    private val entryStore: EntryStore = mockk()
    private val entryApi: EntryApi = mockk()
    private val reminderStore: ReminderStore = mockk(relaxed = true)
    private val dispatcherProvider = TestDispatchers()
    private val dbSemaphore = Semaphore(Int.MAX_VALUE)
    private val entryLocks = ConcurrentHashMap<String, Mutex>()
    private val databaseErrorClassifier: ErrorClassifier = mockk()
    private val networkErrorClassifier: ErrorClassifier = mockk()

    @Before
    fun setUp() {
        // Mocks for successful operations by default
        coEvery { entryStore.upsertIfNewest(any()) } just runs
        coEvery { entryStore.updateEntrySyncState(any(), any()) } just runs
        coJustRun { entryStore.deleteEntry(any(), any()) }
        coJustRun { entryStore.updateDeletedEntrySyncState(any(), any()) }
        coEvery { entryStore.getEntryById(any()) } returns null

        // Mock API calls to return empty lists by default to isolate tests
        coEvery { entryApi.fetchEntries() } returns NetworkResponse.Success(emptyList())
        coEvery { entryApi.fetchDeletedEntry() } returns NetworkResponse.Success(emptyList())

        entryRepository =
            EntryRepositoryImpl(
                entryStore = entryStore,
                entryApi = entryApi,
                reminderStore = reminderStore,
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
            val result = entryRepository.syncEntries()

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
            val result = entryRepository.syncEntries()

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
            entryRepository.syncEntries()

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
            entryRepository.syncEntries()

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
            coEvery { entryApi.fetchDeletedEntry() } returns NetworkResponse.Success(listOf(deletedDto))
            coEvery { entryStore.getEntryById("deleted-1") } returns localEntry

            // When
            entryRepository.syncEntries()

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
            coEvery { entryApi.fetchDeletedEntry() } returns NetworkResponse.Success(listOf(deletedDto))
            coEvery { entryStore.getEntryById("conflict-id") } returns localPendingEntry

            // When
            entryRepository.syncEntries()

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
            coEvery { entryApi.fetchDeletedEntry() } returns NetworkResponse.Success(listOf(deletedDto))
            coEvery { entryStore.getEntryById("deleted-1") } returns null

            // When
            entryRepository.syncEntries()

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
            coEvery { entryApi.fetchDeletedEntry() } returns NetworkResponse.Error(apiException)
            every { networkErrorClassifier.classify(any()) } returns ErrorType.TransientError(apiException)

            // When
            val result = entryRepository.syncEntries()

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
            coEvery { entryApi.fetchDeletedEntry() } returns NetworkResponse.Success(deletedDtos)
            coEvery { entryStore.getEntryById(any()) } returns createEntryEntity("any")
            coEvery { entryStore.deleteEntry("d1", SyncStateEntity.SYNCED) } throws IOException()

            // When
            entryRepository.syncEntries()

            // Then
            coVerify(exactly = 1) { entryStore.deleteEntry("d1", SyncStateEntity.SYNCED) } // attempted
            coVerify(exactly = 1) { entryStore.deleteEntry("d2", SyncStateEntity.SYNCED) } // still runs
        }
    // endregion

    // region Combined Sync Logic Tests
    @Test
    fun `syncEntries should handle both new and deleted entries in one run`() =
        runTest {
            // Given
            val newEntryDto = createEntryDto("new-1")
            val deletedEntryDto = createDeletedEntryDto("deleted-1")
            val localEntryForDeletion = createEntryEntity("deleted-1")

            coEvery { entryApi.fetchEntries() } returns NetworkResponse.Success(listOf(newEntryDto))
            coEvery { entryApi.fetchDeletedEntry() } returns NetworkResponse.Success(listOf(deletedEntryDto))
            coEvery { entryStore.getEntryById("deleted-1") } returns localEntryForDeletion

            // When
            entryRepository.syncEntries()

            // Then
            coVerify(exactly = 1) { entryStore.upsertIfNewest(match { it.id == "new-1" }) }
            coVerify(exactly = 1) { entryStore.deleteEntry("deleted-1", SyncStateEntity.SYNCED) }
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
            coEvery { entryApi.fetchDeletedEntry() } returns NetworkResponse.Success(listOf(deletedEntryDto))
            coEvery { entryStore.getEntryById(conflictingId) } returns localEntryForDeletion

            // When
            entryRepository.syncEntries()

            // Then
            // It should sync the new entry
            coVerify(exactly = 1) { entryStore.upsertIfNewest(match { it.id == "new-1" }) }
            // It should NOT sync the conflicting entry because it's in the deleted list
            coVerify(exactly = 0) { entryStore.upsertIfNewest(match { it.id == conflictingId }) }
            // It should process the deletion for the conflicting entry
            coVerify(exactly = 1) { entryStore.deleteEntry(conflictingId, SyncStateEntity.SYNCED) }
        }
    // endregion

    // region Concurrency Tests
    @Test
    fun `syncEntrySafely locks access to same entry and releases lock`() =
        runTest {
            // Given
            val entryId = "1"
            val remoteEntry = createEntryDto(entryId)
            coEvery { entryApi.fetchEntries() } returns NetworkResponse.Success(listOf(remoteEntry))

            coEvery { entryStore.upsertIfNewest(any()) } coAnswers {
                assertTrue(entryLocks.containsKey(entryId))
                val mutex = entryLocks[entryId]
                assertTrue(mutex?.isLocked == true, "Mutex for entry $entryId should be locked.")
            }

            // When
            entryRepository.syncEntries()

            // Then
            assertTrue(entryLocks.isEmpty(), "Entry locks map should be empty after sync.")
            coVerify(exactly = 1) { entryStore.upsertIfNewest(match { it.id == entryId }) }
        }

    @Test
    fun `syncEntries respects semaphore limit for concurrent DB access`() =
        runTest(dispatcherProvider.io) {
            // GIVEN
            val singlePermitSemaphore = Semaphore(1)
            val concurrentRepo =
                EntryRepositoryImpl(
                    entryStore = entryStore,
                    entryApi = entryApi,
                    reminderStore = reminderStore,
                    dbSemaphore = singlePermitSemaphore,
                    entryLocks = entryLocks,
                    databaseErrorClassifier,
                    networkErrorClassifier,
                    dispatcherProvider = dispatcherProvider,
                )

            val remoteEntries = listOf(createEntryDto("1"), createEntryDto("2"))
            coEvery { entryApi.fetchEntries() } returns NetworkResponse.Success(remoteEntries)

            val executionEvents = mutableListOf<String>()
            coEvery { entryStore.upsertIfNewest(match { it.id == "1" }) } coAnswers {
                executionEvents.add("start 1")
                delay(100)
                executionEvents.add("end 1")
            }
            coEvery { entryStore.upsertIfNewest(match { it.id == "2" }) } coAnswers {
                executionEvents.add("start 2")
                delay(50)
                executionEvents.add("end 2")
            }

            // WHEN
            concurrentRepo.syncEntries()

            // THEN
            val expectedEvents = listOf("start 1", "end 1", "start 2", "end 2")
            assertEquals(expectedEvents, executionEvents)
        }
    // endregion

    // region Helpers
    private fun createEntryDto(
        id: String,
        updatedAt: Timestamp = Timestamp.now()
    ): EntryDto =
        EntryDto(
            id = id,
            title = "Test DTO $id",
            description = "Description",
            isDone = false,
            time = null,
            type = "TASK",
            startDate = null,
            dueDate = System.currentTimeMillis(),
            recurrence = null,
            createdAt = Timestamp.now(),
            updatedAt = updatedAt,
        )

    private fun createDeletedEntryDto(id: String) = DeletedEntryDto(id, Timestamp.now())

    private fun createEntryEntity(
        id: String,
        syncState: SyncStateEntity = SyncStateEntity.SYNCED
    ) = EntryEntity(
        id = id,
        type = EntryEntity.EntryType.TASK,
        title = "Test",
        description = "",
        isDone = false,
        dueDate = 1L,
        createdAt = 1L,
        syncState = syncState,
    )
    // endregion
}
