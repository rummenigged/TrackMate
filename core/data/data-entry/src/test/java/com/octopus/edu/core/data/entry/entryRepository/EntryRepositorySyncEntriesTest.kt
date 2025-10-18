package com.octopus.edu.core.data.entry.entryRepository

import com.google.firebase.Timestamp
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity
import com.octopus.edu.core.data.entry.EntryRepositoryImpl
import com.octopus.edu.core.data.entry.api.EntryApi
import com.octopus.edu.core.data.entry.api.dto.EntryDto
import com.octopus.edu.core.data.entry.store.EntryStore
import com.octopus.edu.core.data.entry.store.ReminderStore
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.utils.ErrorClassifier
import com.octopus.edu.core.network.utils.NetworkResponse
import com.octopus.edu.core.testing.TestDispatchers
import io.mockk.coEvery
import io.mockk.coVerify
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
        }

    @Test
    fun `syncEntries should not upsert entries when api returns error`() =
        runTest {
            // Given
            val exception = Exception("Network error")
            coEvery { entryApi.fetchEntries() } returns NetworkResponse.Error(exception)

            // When
            val result = entryRepository.syncEntries()

            // Then
            assertTrue(result is ResultOperation.Success) // The outer safeCall contains the operation
            coVerify(exactly = 0) { entryStore.upsertIfNewest(any()) }
        }

    @Test
    fun `syncEntries should not upsert entries when api returns empty list`() =
        runTest {
            // Given
            coEvery { entryApi.fetchEntries() } returns NetworkResponse.Success(emptyList())

            // When
            val result = entryRepository.syncEntries()

            // Then
            assertTrue(result is ResultOperation.Success)
            coVerify(exactly = 0) { entryStore.upsertIfNewest(any()) }
        }

    @Test
    fun `syncEntries should update sync state to CONFLICT when upsert fails`() =
        runTest {
            // Given
            val remoteEntry = createEntryDto("1")
            coEvery { entryApi.fetchEntries() } returns NetworkResponse.Success(listOf(remoteEntry))
            coEvery { entryStore.upsertIfNewest(any()) } throws Exception("Database write failed")

            // When
            val result = entryRepository.syncEntries()

            // Then
            assertTrue(result is ResultOperation.Success)
            coVerify(exactly = 1) { entryStore.updateEntrySyncState("1", SyncStateEntity.CONFLICT) }
        }

    @Test
    fun `syncEntries processes other entries even if one fails`() =
        runTest {
            // Given
            val remoteEntries = listOf(createEntryDto("1"), createEntryDto("2"))
            coEvery { entryApi.fetchEntries() } returns NetworkResponse.Success(remoteEntries)
            coEvery { entryStore.upsertIfNewest(match { it.id == "1" }) } throws Exception("DB error")
            coEvery { entryStore.upsertIfNewest(match { it.id == "2" }) } just runs

            // When
            val result = entryRepository.syncEntries()

            // Then
            assertTrue(result is ResultOperation.Success)
            coVerify(exactly = 1) { entryStore.updateEntrySyncState("1", SyncStateEntity.CONFLICT) }
            coVerify(exactly = 1) { entryStore.upsertIfNewest(match { it.id == "2" }) }
        }

    @Test
    fun `syncEntrySafely locks access to same entry and releases lock`() =
        runTest {
            // Given
            val entryId = "1"
            val remoteEntry = createEntryDto(entryId)
            coEvery { entryApi.fetchEntries() } returns NetworkResponse.Success(listOf(remoteEntry))

            coEvery { entryStore.upsertIfNewest(any()) } coAnswers {
                // Check that the lock is held during the operation
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
}
