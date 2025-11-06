package com.octopus.edu.core.data.entry.entryRepository

import com.octopus.edu.core.common.toEpochMilli
import com.octopus.edu.core.data.database.entity.EntryEntity
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity
import com.octopus.edu.core.data.entry.EntryRepositoryImpl
import com.octopus.edu.core.data.entry.api.EntryApi
import com.octopus.edu.core.data.entry.store.EntryStore
import com.octopus.edu.core.data.entry.store.ReminderStore
import com.octopus.edu.core.data.entry.utils.getReminderAsEntity
import com.octopus.edu.core.data.entry.utils.toEntity
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
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.sql.SQLTimeoutException
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

    @After
    fun tearDown() {
        io.mockk.unmockkAll()
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
                        dueDate = LocalDate.now().toEpochMilli(),
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
                        lastCompletedDate = LocalDate.now().toEpochMilli(),
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
                        dueDate = LocalDate.now().toEpochMilli(),
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
                        dueDate = LocalDate.now().toEpochMilli(),
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
            every { databaseErrorClassifier.classify(any()) } returns
                ErrorType.TransientError(storeException)

            val result = repository.saveEntry(testTaskDomain)

            assertTrue(result is ResultOperation.Error)
            assertEquals(storeException, (result as ResultOperation.Error).throwable)
            assertTrue(result.isRetriable)
            coVerify(exactly = 1) { entryStore.saveEntry(expectedEntryEntity) }
            coVerify(exactly = 0) { reminderStore.saveReminder(any()) }
        }

    // --- Tests for markEntryAsDone ---
    @Test
    fun `markEntryAsDone returns Success on successful operation`() =
        runTest {
            // Given
            val entryId = "entry-to-mark-done"
            coJustRun { entryStore.markEntryAsDone(entryId) }

            // When
            val result = repository.markEntryAsDone(entryId)

            // Then
            assertTrue(result is ResultOperation.Success)
            coVerify(exactly = 1) { entryStore.markEntryAsDone(entryId) }
        }

    @Test
    fun `markEntryAsDone returns retriable Error on transient db failure`() =
        runTest {
            // Given
            val entryId = "entry-to-mark-done"
            val dbException = SQLTimeoutException("DB operation timed out")
            coEvery { entryStore.markEntryAsDone(entryId) } throws dbException
            every { databaseErrorClassifier.classify(dbException) } returns ErrorType.TransientError(dbException)

            // When
            val result = repository.markEntryAsDone(entryId)

            // Then
            assertTrue(result is ResultOperation.Error)
            assertEquals(dbException, (result as ResultOperation.Error).throwable)
            assertTrue(result.isRetriable)
        }

    @Test
    fun `markEntryAsDone returns permanent Error on non-retriable db failure`() =
        runTest {
            // Given
            val entryId = "entry-to-mark-done"
            val dbException = RuntimeException("Permanent DB error")
            coEvery { entryStore.markEntryAsDone(entryId) } throws dbException
            every { databaseErrorClassifier.classify(dbException) } returns ErrorType.PermanentError(dbException)

            // When
            val result = repository.markEntryAsDone(entryId)

            // Then
            assertTrue(result is ResultOperation.Error)
            assertEquals(dbException, (result as ResultOperation.Error).throwable)
            assertFalse(result.isRetriable)
        }
}
