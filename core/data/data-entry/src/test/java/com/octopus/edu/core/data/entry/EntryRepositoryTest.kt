package com.octopus.edu.core.data.entry

import com.octopus.edu.core.data.database.entity.EntryEntity
import com.octopus.edu.core.data.entry.store.EntryStore
import com.octopus.edu.core.data.entry.store.ReminderStore
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.repository.EntryRepository
import com.octopus.edu.core.testing.TestDispatchers
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class EntryRepositoryTest {
    private lateinit var testDispatchers: TestDispatchers
    private lateinit var entryStore: EntryStore
    private lateinit var reminderStore: ReminderStore
    private lateinit var repository: EntryRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        entryStore = mockk()
        reminderStore = mockk()
        testDispatchers = TestDispatchers()
        repository = EntryRepositoryImpl(entryStore, reminderStore, testDispatchers)
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
                        dueDate = LocalDate.now().toEpochDay(),
                        recurrence = null,
                        streakCount = null,
                        lastCompletedDate = null,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = null,
                    ),
                )
            coEvery { entryStore.getTasks() } returns entryEntities

            // When
            val result = repository.getTasks()

            // Then
            assert(result is ResultOperation.Success)
            val tasks = (result as ResultOperation.Success).data
            assert(tasks.size == 1)
            assert(tasks[0] is Task)
            assert(tasks[0].title == "Task 1")
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
                        lastCompletedDate = LocalDate.now().toEpochDay(),
                        createdAt = System.currentTimeMillis(),
                        updatedAt = null,
                    ),
                )
            coEvery { entryStore.getHabits() } returns entryEntities

            // When
            val result = repository.getHabits()

            // Then
            assert(result is ResultOperation.Success)
            val habits = (result as ResultOperation.Success).data
            assert(habits.size == 1)
            assert(habits[0] is Habit)
            assert(habits[0].title == "Habit 1")
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
                        dueDate = LocalDate.now().toEpochDay(),
                        recurrence = null,
                        streakCount = null,
                        lastCompletedDate = null,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = null,
                    ),
                    EntryEntity(
                        id = "2",
                        type = EntryEntity.EntryType.HABIT,
                        title = "Invalid Habit",
                        description = "",
                        isDone = true,
                        dueDate = null,
                        recurrence = EntryEntity.Recurrence.DAILY,
                        streakCount = 1,
                        lastCompletedDate = LocalDate.now().toEpochDay(),
                        createdAt = System.currentTimeMillis(),
                        updatedAt = null,
                    ),
                )
            coEvery { entryStore.getTasks() } returns entryEntities

            // When
            val result = repository.getTasks()

            // Then
            val tasks = (result as ResultOperation.Success).data
            assert(tasks.size == 1)
            assert(tasks[0].title == "Valid Task")
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
                        streakCount = 1,
                        lastCompletedDate = LocalDate.now().toEpochDay(),
                        createdAt = System.currentTimeMillis(),
                        updatedAt = null,
                    ),
                    EntryEntity(
                        id = "1",
                        type = EntryEntity.EntryType.TASK,
                        title = "Invalid Task",
                        description = "",
                        isDone = false,
                        dueDate = LocalDate.now().toEpochDay(),
                        recurrence = null,
                        streakCount = null,
                        lastCompletedDate = null,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = null,
                    ),
                )
            coEvery { entryStore.getHabits() } returns entryEntities

            // When
            val result = repository.getHabits()

            // Then
            val habits = (result as ResultOperation.Success).data
            assert(habits.size == 1)
            assert(habits[0].title == "Valid Habit")
        }
}
