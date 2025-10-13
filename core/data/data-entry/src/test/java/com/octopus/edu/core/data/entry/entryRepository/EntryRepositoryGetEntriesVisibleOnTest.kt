package com.octopus.edu.core.data.entry.entryRepository

import app.cash.turbine.test
import com.octopus.edu.core.common.toEpocMilliseconds
import com.octopus.edu.core.data.database.entity.EntryEntity
import com.octopus.edu.core.data.entry.EntryRepositoryImpl
import com.octopus.edu.core.data.entry.api.EntryApi
import com.octopus.edu.core.data.entry.store.EntryStore
import com.octopus.edu.core.data.entry.store.ReminderStore
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.Recurrence
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.repository.EntryRepository
import com.octopus.edu.core.testing.TestDispatchers
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap

@OptIn(ExperimentalCoroutinesApi::class)
class EntryRepositoryGetEntriesVisibleOnTest {
    private lateinit var testDispatchers: TestDispatchers
    private lateinit var entryStore: EntryStore
    private lateinit var entryApi: EntryApi
    private lateinit var reminderStore: ReminderStore
    private lateinit var repository: EntryRepository
    private val dbSemaphore = Semaphore(Int.MAX_VALUE)
    private val entryLocks = ConcurrentHashMap<String, Mutex>()

    private val testDate = LocalDate.of(2024, 1, 8) // Monday
    private val testDateMillis = testDate.toEpocMilliseconds()
    private val dayBeforeTestDate = testDate.minusDays(1)

    private val taskDueOnTestDateEntity =
        EntryEntity(
            id = "task1",
            type = EntryEntity.EntryType.TASK,
            title = "Task on Test Date",
            description = "",
            isDone = false,
            dueDate = testDate.toEpocMilliseconds(),
            recurrence = null,
            streakCount = null,
            lastCompletedDate = null,
            createdAt = dayBeforeTestDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            updatedAt = null,
            syncState = EntryEntity.SyncStateEntity.PENDING,
        )

    private val taskDueBeforeTestDateEntity =
        EntryEntity(
            id = "task2",
            type = EntryEntity.EntryType.TASK,
            title = "Task before Test Date",
            description = "",
            isDone = false,
            dueDate = dayBeforeTestDate.toEpocMilliseconds(),
            recurrence = null,
            streakCount = null,
            lastCompletedDate = null,
            createdAt =
                dayBeforeTestDate
                    .minusDays(1)
                    .atStartOfDay()
                    .toInstant(ZoneOffset.UTC)
                    .toEpochMilli(),
            updatedAt = null,
            syncState = EntryEntity.SyncStateEntity.PENDING,
        )

    private val dailyHabitEntity =
        EntryEntity(
            id = "habit1",
            type = EntryEntity.EntryType.HABIT,
            title = "Daily Habit",
            description = "",
            isDone = false,
            dueDate = null,
            recurrence = EntryEntity.Recurrence.DAILY,
            streakCount = 0,
            startDate = dayBeforeTestDate.toEpocMilliseconds(),
            lastCompletedDate = null,
            createdAt = dayBeforeTestDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            updatedAt = null,
            syncState = EntryEntity.SyncStateEntity.PENDING,
        )

    private val weeklyHabitSameDayAsTestDateEntity =
        EntryEntity(
            id = "habit2",
            type = EntryEntity.EntryType.HABIT,
            title = "Weekly Habit Monday",
            description = "",
            isDone = false,
            dueDate = null,
            recurrence = EntryEntity.Recurrence.WEEKLY,
            streakCount = 0,
            lastCompletedDate = null,
            startDate = testDate.minusWeeks(1).toEpocMilliseconds(),
            createdAt = dayBeforeTestDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            updatedAt = null,
            syncState = EntryEntity.SyncStateEntity.PENDING,
        )

    private val weeklyHabitDayAfterTestDateEntity =
        EntryEntity(
            // Should not apply on testDate (Monday)
            id = "habit3",
            type = EntryEntity.EntryType.HABIT,
            title = "Weekly Habit Tuesday",
            description = "",
            isDone = false,
            dueDate = null,
            recurrence = EntryEntity.Recurrence.WEEKLY,
            streakCount = 0,
            lastCompletedDate = null,
            startDate = testDate.minusWeeks(1).plusDays(1).toEpocMilliseconds(),
            createdAt = dayBeforeTestDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            updatedAt = null,
            syncState = EntryEntity.SyncStateEntity.PENDING,
        )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        entryStore = mockk()
        reminderStore = mockk(relaxed = true) // relaxed for saveEntry calls if not directly tested here
        entryApi = mockk()
        testDispatchers = TestDispatchers()
        repository =
            EntryRepositoryImpl(
                entryStore,
                entryApi,
                reminderStore,
                dbSemaphore,
                entryLocks,
                testDispatchers,
            )
    }

    @Test
    fun `getEntriesVisibleOn returns empty list when store emits empty`() =
        runTest {
            every { entryStore.getEntriesBeforeOrOn(testDateMillis) } returns flowOf(emptyList())

            repository.getEntriesVisibleOn(testDate).test {
                val result = awaitItem()
                Assert.assertTrue(result is ResultOperation.Success)
                Assert.assertTrue((result as ResultOperation.Success).data.isEmpty())
                awaitComplete()
            }
        }

    @Test
    fun `getEntriesVisibleOn returns only tasks due on or before date`() =
        runTest {
            val entities = listOf(taskDueOnTestDateEntity, taskDueBeforeTestDateEntity)
            every { entryStore.getEntriesBeforeOrOn(testDateMillis) } returns flowOf(entities)

            repository.getEntriesVisibleOn(testDate).test {
                val result = awaitItem()
                Assert.assertTrue(result is ResultOperation.Success)
                val entries = (result as ResultOperation.Success).data
                Assert.assertEquals(2, entries.size)
                Assert.assertTrue(entries.any { it.id == "task1" && it is Task })
                Assert.assertTrue(entries.any { it.id == "task2" && it is Task })
                awaitComplete()
            }
        }

    @Test
    fun `getEntriesVisibleOn returns tasks and applicable daily habits`() =
        runTest {
            val entities = listOf(taskDueOnTestDateEntity, dailyHabitEntity)
            every { entryStore.getEntriesBeforeOrOn(testDateMillis) } returns flowOf(entities)

            repository.getEntriesVisibleOn(testDate).test {
                val result = awaitItem()
                Assert.assertTrue(result is ResultOperation.Success)
                val entries = (result as ResultOperation.Success).data
                Assert.assertEquals(2, entries.size)
                Assert.assertTrue(entries.any { it.id == "task1" && it is Task })
                Assert.assertTrue(entries.any { it.id == "habit1" && it is Habit })
                awaitComplete()
            }
        }

    @Test
    fun `getEntriesVisibleOn returns tasks and applicable weekly habits`() =
        runTest {
            val entities = listOf(taskDueOnTestDateEntity, weeklyHabitSameDayAsTestDateEntity)
            every { entryStore.getEntriesBeforeOrOn(testDateMillis) } returns flowOf(entities)

            repository.getEntriesVisibleOn(testDate).test {
                val result = awaitItem()
                Assert.assertTrue(result is ResultOperation.Success)
                val entries = (result as ResultOperation.Success).data
                Assert.assertEquals(2, entries.size)
                Assert.assertTrue(entries.any { it.id == "task1" && it is Task })
                Assert.assertTrue(
                    entries.any {
                        it.id == "habit2" &&
                            it is Habit && it.recurrence == Recurrence.Weekly
                    },
                )
                awaitComplete()
            }
        }

    @Test
    fun `getEntriesVisibleOn filters out non-applicable weekly habits`() =
        runTest {
            val entities = listOf(taskDueOnTestDateEntity, weeklyHabitSameDayAsTestDateEntity, weeklyHabitDayAfterTestDateEntity)
            every { entryStore.getEntriesBeforeOrOn(testDateMillis) } returns flowOf(entities)

            repository.getEntriesVisibleOn(testDate).test {
                val result = awaitItem()
                Assert.assertTrue(result is ResultOperation.Success)
                val entries = (result as ResultOperation.Success).data
                Assert.assertEquals(2, entries.size) // Task + Monday Habit
                Assert.assertTrue(entries.any { it.id == "task1" && it is Task })
                Assert.assertTrue(entries.any { it.id == "habit2" && it is Habit }) // Monday habit
                Assert.assertFalse(entries.any { it.id == "habit3" }) // Tuesday habit should be filtered out
                awaitComplete()
            }
        }

    @Test
    fun `getEntriesVisibleOn combines tasks and various applicable habits correctly`() =
        runTest {
            val entities =
                listOf(
                    taskDueOnTestDateEntity,
                    taskDueBeforeTestDateEntity,
                    dailyHabitEntity,
                    weeklyHabitSameDayAsTestDateEntity,
                    weeklyHabitDayAfterTestDateEntity,
                )
            every { entryStore.getEntriesBeforeOrOn(testDateMillis) } returns flowOf(entities)

            repository.getEntriesVisibleOn(testDate).test {
                val result = awaitItem()
                Assert.assertTrue(result is ResultOperation.Success)
                val entries = (result as ResultOperation.Success).data
                Assert.assertEquals(4, entries.size) // 2 Tasks, 1 Daily Habit, 1 Monday Weekly Habit
                Assert.assertTrue(entries.count { it is Task } == 2)
                Assert.assertTrue(entries.count { it is Habit } == 2)
                Assert.assertTrue(entries.any { it.id == "task1" })
                Assert.assertTrue(entries.any { it.id == "task2" })
                Assert.assertTrue(entries.any { it.id == "habit1" }) // Daily
                Assert.assertTrue(entries.any { it.id == "habit2" }) // Weekly Monday
                Assert.assertFalse(entries.any { it.id == "habit3" }) // Weekly Tuesday
                awaitComplete()
            }
        }

    @Test
    fun `getEntriesVisibleOn emits retriable error on IOException from store`() =
        runTest {
            val ioException = IOException("Network error")
            every { entryStore.getEntriesBeforeOrOn(testDateMillis) } returns flow { throw ioException }

            repository.getEntriesVisibleOn(testDate).test {
                val result = awaitItem()
                Assert.assertTrue(result is ResultOperation.Error)
                val errorResult = result as ResultOperation.Error
                Assert.assertEquals(ioException, errorResult.throwable)
                Assert.assertTrue(errorResult.isRetriable)
                awaitComplete()
            }
        }

    @Test
    fun `getEntriesVisibleOn emits non-retriable error on other Exception from store`() =
        runTest {
            val runtimeException = RuntimeException("Some other DB error")
            every { entryStore.getEntriesBeforeOrOn(testDateMillis) } returns flow { throw runtimeException }

            repository.getEntriesVisibleOn(testDate).test {
                val result = awaitItem()
                Assert.assertTrue(result is ResultOperation.Error)
                val errorResult = result as ResultOperation.Error
                Assert.assertEquals(runtimeException, errorResult.throwable)
                // The default for isRetriable is false unless IOException or SQLiteException
                Assert.assertFalse(errorResult.isRetriable)
                awaitComplete()
            }
        }
}
