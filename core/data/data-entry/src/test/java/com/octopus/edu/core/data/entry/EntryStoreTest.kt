package com.octopus.edu.core.data.entry

import com.octopus.edu.core.data.database.dao.EntryDao
import com.octopus.edu.core.data.database.entity.EntryEntity
import com.octopus.edu.core.data.entry.store.EntryStore
import com.octopus.edu.core.data.entry.store.EntryStoreImpl
import com.octopus.edu.core.testing.MainCoroutineRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class EntryStoreTest {
    @get:Rule
    val mainCoroutine = MainCoroutineRule()

    @get:Rule
    val mockkRule = MockKRule(this)

    private val entryDao: EntryDao = mockk()
    private lateinit var entryStore: EntryStore

    @Before
    fun setUp() {
        entryStore = EntryStoreImpl(entryDao)
    }

    @Test
    fun `getHabits should return list of habits from dao`() =
        runTest {
            // Given
            val fakeHabits =
                listOf(
                    EntryEntity(
                        id = "habit-1",
                        type = EntryEntity.EntryType.HABIT,
                        title = "Drink Water",
                        description = "Stay hydrated",
                        isDone = false,
                        dueDate = null,
                        recurrence = EntryEntity.Recurrence.DAILY,
                        streakCount = 3,
                        lastCompletedDate = LocalDate.now().toEpochDay(),
                        createdAt = System.currentTimeMillis(),
                        updatedAt = null,
                    ),
                )

            coEvery { entryDao.getHabits() } returns fakeHabits

            // When
            val result = entryStore.getHabits()

            // Then
            assert(result == fakeHabits)
            coVerify(exactly = 1) { entryDao.getHabits() }
        }

    @Test
    fun `getTasks should return list of tasks from dao`() =
        runTest {
            // Given
            val fakeTasks =
                listOf(
                    EntryEntity(
                        id = "task-1",
                        type = EntryEntity.EntryType.TASK,
                        title = "Submit report",
                        description = "Project summary",
                        isDone = true,
                        dueDate = LocalDate.now().plusDays(1).toEpochDay(),
                        recurrence = null,
                        streakCount = null,
                        lastCompletedDate = null,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = null,
                    ),
                )

            coEvery { entryDao.getTasks() } returns fakeTasks

            // When
            val result = entryStore.getTasks()

            // Then
            assert(result == fakeTasks)
            coVerify(exactly = 1) { entryDao.getTasks() }
        }

    @Test
    fun `getHabits should return empty list if no habits exist`() =
        runTest {
            coEvery { entryDao.getHabits() } returns emptyList()

            val result = entryStore.getHabits()

            assert(result.isEmpty())
            coVerify(exactly = 1) { entryDao.getHabits() }
        }

    @Test
    fun `getTasks should return empty list if no task exist`() =
        runTest {
            coEvery { entryDao.getTasks() } returns emptyList()

            val result = entryStore.getTasks()

            assert(result.isEmpty())
            coVerify(exactly = 1) { entryDao.getTasks() }
        }
}
