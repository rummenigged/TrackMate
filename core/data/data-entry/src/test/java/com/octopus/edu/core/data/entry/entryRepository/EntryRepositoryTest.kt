package com.octopus.edu.core.data.entry.entryRepository

import com.octopus.edu.core.common.toEpocMilliseconds
import com.octopus.edu.core.data.database.entity.DeletedEntryEntity
import com.octopus.edu.core.data.database.entity.EntryEntity
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity
import com.octopus.edu.core.data.entry.EntryRepositoryImpl
import com.octopus.edu.core.data.entry.api.EntryApi
import com.octopus.edu.core.data.entry.store.EntryStore
import com.octopus.edu.core.data.entry.store.ReminderStore
import com.octopus.edu.core.data.entry.utils.getReminderAsEntity
import com.octopus.edu.core.data.entry.utils.toDomain
import com.octopus.edu.core.data.entry.utils.toEntity
import com.octopus.edu.core.domain.model.DeletedEntry
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.Reminder
import com.octopus.edu.core.domain.model.SyncState
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.model.common.ErrorType
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.repository.EntryRepository
import com.octopus.edu.core.domain.utils.ErrorClassifier
import com.octopus.edu.core.testing.TestDispatchers
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.sql.SQLTimeoutException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@OptIn(ExperimentalCoroutinesApi::class)
class EntryRepositoryTest {
    private lateinit var testDispatchers: TestDispatchers
    private lateinit var entryStore: EntryStore
    private lateinit var entryApi: EntryApi
    private lateinit var reminderStore: ReminderStore
    private lateinit var repository: EntryRepository
    private lateinit var databaseErrorClassifier: ErrorClassifier
    private lateinit var networkErrorClassifier: ErrorClassifier
    private val dbSemaphore = Semaphore(Int.MAX_VALUE)
    private val entryLocks = ConcurrentHashMap<String, Mutex>()

    private val testDate = LocalDate.of(2024, 1, 8)
    private val dayBeforeTestDate = testDate.minusDays(1)

    private val testTaskDomain =
        Task(
            id = "taskSave1",
            title = "Test Save Task",
            description = "Task to be saved",
            dueDate = testDate,
            isDone = false,
            time = LocalTime.now(),
            createdAt = dayBeforeTestDate.atStartOfDay().toInstant(ZoneOffset.UTC),
            updatedAt = null,
            syncState = SyncState.PENDING,
            reminder = Reminder.OnTime,
        )

    private lateinit var defaultRandomId: String

    private lateinit var defaultDateNow: LocalDateTime

    @Before
    fun setUp() {
        mockkStatic("com.octopus.edu.core.data.entry.utils.EntityMappingExtensionsKt")
        MockKAnnotations.init(this)
        entryStore = mockk()
        reminderStore = mockk(relaxed = true)
        entryApi = mockk(relaxed = true)
        testDispatchers = TestDispatchers()
        databaseErrorClassifier = mockk()
        networkErrorClassifier = mockk()
        repository =
            EntryRepositoryImpl(
                entryStore,
                entryApi,
                reminderStore,
                dbSemaphore,
                entryLocks,
                databaseErrorClassifier,
                networkErrorClassifier,
                testDispatchers,
            )

        defaultRandomId = UUID.randomUUID().toString()
        defaultDateNow = LocalDateTime.of(2025, 9, 15, 8, 0)
    }

    @Test
    fun `getTasks returns list of Task wrapped in Success`() =
        runTest {
            // Given
            val entryEntities =
                listOf(
                    EntryEntity(
                        id = "1",
                        type = EntryEntity.EntryType.TASK,
                        title = "Task 1",
                        description = "desc",
                        isDone = false,
                        dueDate = LocalDate.now().toEpocMilliseconds(),
                        recurrence = null,
                        streakCount = null,
                        lastCompletedDate = null,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = null,
                        syncState = SyncStateEntity.SYNCED,
                    ),
                )
            coEvery { entryStore.getTasks() } returns entryEntities

            // When
            val result = repository.getTasks()

            // Then
            assertTrue(result is ResultOperation.Success)
            val tasks = (result as ResultOperation.Success).data
            assertEquals(1, tasks.size)
            assertTrue(tasks[0] is Task)
            assertEquals("Task 1", tasks[0].title)
        }

    @Test
    fun `getHabits returns list of Habit wrapped in Success`() =
        runTest {
            // Given
            val entryEntities =
                listOf(
                    EntryEntity(
                        id = "2",
                        type = EntryEntity.EntryType.HABIT,
                        title = "Habit 1",
                        description = "desc",
                        isDone = true,
                        dueDate = null,
                        recurrence = EntryEntity.Recurrence.DAILY,
                        streakCount = 2,
                        lastCompletedDate = LocalDate.now().toEpocMilliseconds(),
                        createdAt = System.currentTimeMillis(),
                        updatedAt = null,
                        syncState = SyncStateEntity.SYNCED,
                    ),
                )
            coEvery { entryStore.getHabits() } returns entryEntities

            // When
            val result = repository.getHabits()

            // Then
            assertTrue(result is ResultOperation.Success)
            val habits = (result as ResultOperation.Success).data
            assertEquals(1, habits.size)
            assertTrue(habits[0] is Habit)
            assertEquals("Habit 1", habits[0].title)
        }

    @Test
    fun `getTasks filters out non-Task entries`() =
        runTest {
            // Given: 1 task + 1 habit (should filter out habit)
            val entryEntities =
                listOf(
                    EntryEntity(
                        id = "1",
                        type = EntryEntity.EntryType.TASK,
                        title = "Valid Task",
                        description = "",
                        isDone = false,
                        dueDate = LocalDate.now().toEpocMilliseconds(),
                        recurrence = null,
                        createdAt = System.currentTimeMillis(),
                        syncState = SyncStateEntity.SYNCED,
                    ),
                    EntryEntity(
                        id = "2",
                        type = EntryEntity.EntryType.HABIT,
                        title = "Invalid Habit",
                        description = "",
                        isDone = true,
                        dueDate = null,
                        recurrence = EntryEntity.Recurrence.DAILY,
                        createdAt = System.currentTimeMillis(),
                        syncState = SyncStateEntity.SYNCED,
                    ),
                )
            coEvery { entryStore.getTasks() } returns entryEntities

            // When
            val result = repository.getTasks()

            // Then
            val tasks = (result as ResultOperation.Success).data
            assertEquals(1, tasks.size)
            assertEquals("Valid Task", tasks[0].title)
        }

    @Test
    fun `getHabits filters out non-Habit entries`() =
        runTest {
            // Given: 1 habit + 1 task (should filter out task)
            val entryEntities =
                listOf(
                    EntryEntity(
                        id = "2",
                        type = EntryEntity.EntryType.HABIT,
                        title = "Valid Habit",
                        description = "",
                        isDone = true,
                        dueDate = null,
                        recurrence = EntryEntity.Recurrence.DAILY,
                        createdAt = System.currentTimeMillis(),
                        syncState = SyncStateEntity.SYNCED,
                    ),
                    EntryEntity(
                        id = "1",
                        type = EntryEntity.EntryType.TASK,
                        title = "Invalid Task",
                        description = "",
                        isDone = false,
                        dueDate = LocalDate.now().toEpocMilliseconds(),
                        createdAt = System.currentTimeMillis(),
                        syncState = SyncStateEntity.SYNCED,
                    ),
                )
            coEvery { entryStore.getHabits() } returns entryEntities

            // When
            val result = repository.getHabits()

            // Then
            val habits = (result as ResultOperation.Success).data
            assertEquals(1, habits.size)
            assertEquals("Valid Habit", habits[0].title)
        }

    // --- Tests for saveEntry ---
    @Test
    fun `saveEntry successfully saves entry and reminder`() =
        runTest {
            mockkStatic(UUID::class)
            mockkStatic(LocalDateTime::class)

            every { UUID.randomUUID().toString() } returns defaultRandomId
            every { LocalDateTime.now() } returns defaultDateNow

            val expectedEntryEntity = testTaskDomain.toEntity()
            val expectedReminderEntity = testTaskDomain.getReminderAsEntity()
            coJustRun { entryStore.saveEntry(expectedEntryEntity) }
            coJustRun { reminderStore.saveReminder(expectedReminderEntity) }

            val result = repository.saveEntry(testTaskDomain)

            assertTrue(result is ResultOperation.Success)
            assertEquals(Unit, (result as ResultOperation.Success).data)
            coVerify(exactly = 1) { entryStore.saveEntry(expectedEntryEntity) }
            coVerify(exactly = 1) { reminderStore.saveReminder(expectedReminderEntity) }
        }

    @Test
    fun `saveEntry returns error when entryStore fails`() =
        runTest {
            mockkStatic(UUID::class)
            mockkStatic(LocalDateTime::class)

            every { UUID.randomUUID().toString() } returns defaultRandomId
            every { LocalDateTime.now() } returns defaultDateNow

            val expectedEntryEntity = testTaskDomain.toEntity()
            val storeException = RuntimeException("EntryStore failed")
            coEvery { entryStore.saveEntry(expectedEntryEntity) } throws storeException

            val result = repository.saveEntry(testTaskDomain)

            assertTrue(result is ResultOperation.Error)
            assertEquals(storeException, (result as ResultOperation.Error).throwable)
            coVerify(exactly = 1) { entryStore.saveEntry(expectedEntryEntity) }
            coVerify(exactly = 0) { reminderStore.saveReminder(any()) } // Using any() as ReminderEntity is not defined in context
        }

    @Test
    fun `saveEntry returns error when reminderStore fails`() =
        runTest {
            mockkStatic(UUID::class)
            mockkStatic(LocalDateTime::class)

            every { UUID.randomUUID().toString() } returns defaultRandomId
            every { LocalDateTime.now() } returns defaultDateNow

            val expectedEntryEntity = testTaskDomain.toEntity()
            val expectedReminderEntity = testTaskDomain.getReminderAsEntity()
            val reminderException = RuntimeException("ReminderStore failed")

            coJustRun { entryStore.saveEntry(expectedEntryEntity) }
            coEvery { reminderStore.saveReminder(expectedReminderEntity) } throws reminderException

            val result = repository.saveEntry(testTaskDomain)

            assertTrue(result is ResultOperation.Error)
            assertEquals(reminderException, (result as ResultOperation.Error).throwable)
            coVerify(exactly = 1) { entryStore.saveEntry(expectedEntryEntity) }
            coVerify(exactly = 1) { reminderStore.saveReminder(expectedReminderEntity) }
        }

    // --- Tests for getEntryById ---
    @Test
    fun `getEntryById returns entry when found`() =
        runTest {
            val expectedEntryEntity = testTaskDomain.toEntity()
            coEvery { entryStore.getEntryById(expectedEntryEntity.id) } returns expectedEntryEntity

            val result = repository.getEntryById(expectedEntryEntity.id)

            assertTrue(result is ResultOperation.Success)
            assertEquals(expectedEntryEntity.toDomain(), (result as ResultOperation.Success).data)
            coVerify(exactly = 1) { entryStore.getEntryById(expectedEntryEntity.id) }
        }

    @Test
    fun `getEntryById throws NoSuchElementException when not found`() =
        runTest {
            val expectedEntryEntity = testTaskDomain.toEntity()
            every {
                databaseErrorClassifier.classify(any())
            } returns ErrorType.TransientError(NoSuchElementException())
            coEvery { entryStore.getEntryById(expectedEntryEntity.id) } returns null

            val result = repository.getEntryById(expectedEntryEntity.id)

            assertTrue(result is ResultOperation.Error)
            assertTrue((result as ResultOperation.Error).throwable is NoSuchElementException)
            coVerify(exactly = 1) { entryStore.getEntryById(expectedEntryEntity.id) }
        }

    @Test
    fun `getEntryById returns retriable error when throws a SQLTimeoutException`() =
        runTest {
            val expectedEntryEntity = testTaskDomain.toEntity()
            val expectedException = SQLTimeoutException()
            coEvery { entryStore.getEntryById(expectedEntryEntity.id) } throws expectedException
            every {
                databaseErrorClassifier.classify(expectedException)
            } returns ErrorType.TransientError(expectedException)

            val result = repository.getEntryById(expectedEntryEntity.id)

            assertTrue(result is ResultOperation.Error)
            assertTrue((result as ResultOperation.Error).isRetriable)
            coVerify(exactly = 1) { entryStore.getEntryById(expectedEntryEntity.id) }
        }

    @Test
    fun `getEntryById returns non-retriable error when throws a RuntimeException`() =
        runTest {
            val expectedEntryEntity = testTaskDomain.toEntity()
            val expectedException = RuntimeException("Mapping failed")
            coEvery { entryStore.getEntryById(expectedEntryEntity.id) } throws expectedException
            every {
                databaseErrorClassifier.classify(expectedException)
            } returns ErrorType.PermanentError(expectedException)

            val result = repository.getEntryById(expectedEntryEntity.id)

            assertTrue(result is ResultOperation.Error)
            assertFalse((result as ResultOperation.Error).isRetriable)
            coVerify(exactly = 1) { entryStore.getEntryById(expectedEntryEntity.id) }
        }

    @Test
    fun `getEntryById returns non-retriable error when throws an exception while mapping to domain`() =
        runTest {
            val expectedEntryEntity = testTaskDomain.toEntity()
            val expectedException = RuntimeException("Mapping failed")
            every { expectedEntryEntity.toDomain() } throws expectedException
            every {
                databaseErrorClassifier.classify(expectedException)
            } returns ErrorType.PermanentError(expectedException)
            coEvery { entryStore.getEntryById(expectedEntryEntity.id) } returns expectedEntryEntity

            val result = repository.getEntryById(expectedEntryEntity.id)

            assertTrue(result is ResultOperation.Error)
            assertFalse((result as ResultOperation.Error).isRetriable)
            coVerify(exactly = 1) { entryStore.getEntryById(expectedEntryEntity.id) }
        }

    @Test
    fun `deleteEntry returns Unit when successful`() =
        runTest {
            val entryId = "testId"
            coJustRun { entryStore.deleteEntry(entryId, SyncStateEntity.PENDING) }

            val result = repository.deleteEntry(entryId)

            assertTrue(result is ResultOperation.Success)
            coVerify(exactly = 1) { entryStore.deleteEntry(entryId, SyncStateEntity.PENDING) }
        }

    @Test
    fun `deleteEntry returns error when deleteEntry fails`() =
        runTest {
            val entryId = "testId"
            coEvery {
                entryStore.deleteEntry(entryId, SyncStateEntity.PENDING)
            } throws RuntimeException()

            val result = repository.deleteEntry(entryId)

            assertTrue(result is ResultOperation.Error)
            coVerify(exactly = 1) { entryStore.deleteEntry(entryId, SyncStateEntity.PENDING) }
        }

    // --- Tests for updateEntrySyncState ---
    @Test
    fun `updateEntrySyncState successfully updates sync state in store`() =
        runTest {
            val entryId = "test-entry-id-sync"
            val syncStateToSet = SyncState.SYNCED
            val expectedSyncStateEntity = syncStateToSet.toEntity()

            // Mock the store behavior
            coJustRun { entryStore.updateEntrySyncState(entryId, expectedSyncStateEntity) }

            // Call the repository method
            val result = repository.updateEntrySyncState(entryId, syncStateToSet)

            // Assert success and verify store interaction
            assertTrue(result is ResultOperation.Success)
            assertEquals(Unit, (result as ResultOperation.Success).data)
            coVerify(exactly = 1) { entryStore.updateEntrySyncState(entryId, expectedSyncStateEntity) }
        }

    @Test
    fun `updateEntrySyncState returns error when store update fails`() =
        runTest {
            val entryId = "test-entry-id-sync-fail"
            val syncStateToSet = SyncState.PENDING
            val expectedSyncStateEntity = syncStateToSet.toEntity()
            val storeException = RuntimeException("Store failed to update sync state")

            // Mock the store to throw an exception
            coEvery { entryStore.updateEntrySyncState(entryId, expectedSyncStateEntity) } throws storeException

            // Call the repository method
            val result = repository.updateEntrySyncState(entryId, syncStateToSet)

            // Assert error and verify store interaction
            assertTrue(result is ResultOperation.Error)
            assertEquals(storeException, (result as ResultOperation.Error).throwable)
            coVerify(exactly = 1) { entryStore.updateEntrySyncState(entryId, expectedSyncStateEntity) }
        }

    @Test
    fun `getDeletedEntry returns DeletedEntry on success`() =
        runTest {
            // Given
            val entryId = "deleted-1"
            val deletedEntity = DeletedEntryEntity(entryId, System.currentTimeMillis(), SyncStateEntity.PENDING)
            coEvery { entryStore.getDeletedEntry(entryId) } returns deletedEntity

            // When
            val result = repository.getDeletedEntry(entryId)

            // Then
            kotlin.test.assertTrue(result is ResultOperation.Success)
            val deletedEntry = result.data
            kotlin.test.assertEquals(entryId, deletedEntry.id)
            kotlin.test.assertEquals(deletedEntity.deletedAt, deletedEntry.deletedAt.toEpochMilli())
        }

    @Test
    fun `getDeletedEntry returns error when entry not found`() =
        runTest {
            // Given
            val entryId = "non-existent"
            every {
                databaseErrorClassifier.classify(any())
            } returns ErrorType.TransientError(NoSuchElementException())
            coEvery { entryStore.getDeletedEntry(entryId) } returns null

            // When
            val result = repository.getDeletedEntry(entryId)

            // Then
            kotlin.test.assertTrue(result is ResultOperation.Error)
            kotlin.test.assertTrue(result.throwable is NoSuchElementException)
        }

    @Test
    fun `pushDeletedEntry returns success when api call succeeds`() =
        runTest {
            // Given
            val deletedEntry = DeletedEntry("deleted-1", Instant.now())
            coJustRun { entryApi.pushDeletedEntry(deletedEntry) }

            // When
            val result = repository.pushDeletedEntry(deletedEntry)

            // Then
            kotlin.test.assertTrue(result is ResultOperation.Success)
            coVerify(exactly = 1) { entryApi.pushDeletedEntry(deletedEntry) }
        }

    @Test
    fun `pushDeletedEntry returns error when api call fails`() =
        runTest {
            // Given
            val deletedEntry = DeletedEntry("deleted-1", Instant.now())
            val apiException = IOException("Network failed")
            coEvery { entryApi.pushDeletedEntry(deletedEntry) } throws apiException
            every { networkErrorClassifier.classify(apiException) } returns ErrorType.TransientError(apiException)

            // When
            val result = repository.pushDeletedEntry(deletedEntry)

            // Then
            kotlin.test.assertTrue(result is ResultOperation.Error)
            kotlin.test.assertTrue(result.isRetriable)
            kotlin.test.assertEquals(apiException, result.throwable)
        }

    @Test
    fun `updateDeletedEntrySyncState completes successfully`() =
        runTest {
            // Given
            val entryId = "deleted-1"
            val syncState = SyncState.SYNCED
            coJustRun { entryStore.updateDeletedEntrySyncState(entryId, syncState.toEntity()) }

            // When
            val result = repository.updateDeletedEntrySyncState(entryId, syncState)

            // Then
            kotlin.test.assertTrue(result is ResultOperation.Success)
            coVerify(exactly = 1) { entryStore.updateDeletedEntrySyncState(entryId, syncState.toEntity()) }
        }
}
