package com.octopus.edu.core.data.entry

import app.cash.turbine.test
import com.octopus.edu.core.common.AppClock
import com.octopus.edu.core.common.TransactionRunner
import com.octopus.edu.core.common.toEpochMilli
import com.octopus.edu.core.data.database.dao.DeletedEntryDao
import com.octopus.edu.core.data.database.dao.DoneEntryDao
import com.octopus.edu.core.data.database.dao.EntryDao
import com.octopus.edu.core.data.database.entity.DeletedEntryEntity
import com.octopus.edu.core.data.database.entity.DoneEntryEntity
import com.octopus.edu.core.data.database.entity.EntryEntity
import com.octopus.edu.core.data.database.entity.databaseView.DoneEntryView
import com.octopus.edu.core.data.entry.store.EntryStore
import com.octopus.edu.core.data.entry.store.EntryStoreImpl
import com.octopus.edu.core.testing.MainCoroutineRule
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class EntryStoreTest {
    @get:Rule
    val mainCoroutine = MainCoroutineRule()

    @get:Rule
    val mockkRule = MockKRule(this)

    private val entryDao: EntryDao = mockk()
    private val deletedEntryDao: DeletedEntryDao = mockk()
    private val doneEntryDao: DoneEntryDao = mockk()
    private val roomTransactionRunner: TransactionRunner = mockk()
    private val appClock: AppClock = mockk()
    private lateinit var entryStore: EntryStore

    private val testEntryId = "test-id"
    private val testSyncState = EntryEntity.SyncStateEntity.SYNCED
    private val testDate = LocalDate.now().toEpochMilli()
    private val testLocalDate = LocalDate.of(2205, 10, 10)
    private val testEntry =
        EntryEntity(
            id = testEntryId,
            type = EntryEntity.EntryType.TASK,
            title = "Test Entry",
            description = "Test Description",
            isDone = false,
            dueDate = testDate,
            recurrence = null,
            streakCount = null,
            lastCompletedDate = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = null,
            syncState = EntryEntity.SyncStateEntity.PENDING,
        )
    private val fakeEntryList = listOf(testEntry)

    @Before
    fun setUp() {
        entryStore =
            EntryStoreImpl(
                entryDao,
                doneEntryDao,
                deletedEntryDao,
                appClock,
                roomTransactionRunner,
            )

        coEvery { roomTransactionRunner.run<Unit>(any()) } coAnswers {
            val block = it.invocation.args[0] as suspend () -> Unit
            block.invoke()
        }

        every { appClock.nowLocalDate().toEpochMilli() } returns testLocalDate.toEpochMilli()
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
                        lastCompletedDate = LocalDate.now().toEpochMilli(),
                        createdAt = System.currentTimeMillis(),
                        updatedAt = null,
                        syncState = EntryEntity.SyncStateEntity.SYNCED,
                    ),
                )

            coEvery { entryDao.getHabits() } returns fakeHabits

            // When
            val result = entryStore.getHabits()

            // Then
            assertEquals(fakeHabits, result)
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
                        dueDate = LocalDate.now().plusDays(1).toEpochMilli(),
                        recurrence = null,
                        streakCount = null,
                        lastCompletedDate = null,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = null,
                        syncState = EntryEntity.SyncStateEntity.SYNCED,
                    ),
                )

            coEvery { entryDao.getTasks() } returns fakeTasks

            // When
            val result = entryStore.getTasks()

            // Then
            assertEquals(fakeTasks, result)
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

    @Test
    fun `getHabits should throw exception when dao throws exception`() =
        runTest {
            // Given
            val exception = RuntimeException("Database error")
            coEvery { entryDao.getHabits() } coAnswers { throw exception }

            // When & Then
            val thrownException =
                assertFailsWith<RuntimeException> {
                    entryStore.getHabits()
                }
            assertEquals(exception, thrownException)
            coVerify(exactly = 1) { entryDao.getHabits() }
        }

    @Test
    fun `getTasks should throw exception when dao throws exception`() =
        runTest {
            // Given
            val exception = RuntimeException("Database error")
            coEvery { entryDao.getTasks() } coAnswers { throw exception }

            // When & Then
            val thrownException =
                assertFailsWith<RuntimeException> {
                    entryStore.getTasks()
                }
            assertEquals(exception, thrownException)
            coVerify(exactly = 1) { entryDao.getTasks() }
        }

    @Test
    fun `getEntryById should throw exception when dao throws exception`() =
        runTest {
            // Given
            val exception = RuntimeException("Database error")
            coEvery { entryDao.getEntryById(testEntryId) } coAnswers { throw exception }

            // When & Then
            val thrownException =
                assertFailsWith<RuntimeException> {
                    entryStore.getEntryById(testEntryId)
                }
            assertEquals(exception, thrownException)
            coVerify(exactly = 1) { entryDao.getEntryById(testEntryId) }
        }

    @Test
    fun `getPendingEntries should throw exception when dao throws exception`() =
        runTest {
            // Given
            val exception = RuntimeException("Database error")
            coEvery { entryDao.getPendingEntries() } throws exception

            // When & Then
            val thrownException =
                assertFailsWith<RuntimeException> {
                    entryStore.getPendingEntries()
                }
            assertEquals(exception, thrownException)
            coVerify(exactly = 1) { entryDao.getPendingEntries() }
        }

    @Test
    fun `markEntryAsDone should call dao markEntryAsDone`() =
        runTest {
            // Given
            val doneAt = appClock.nowLocalDate().toEpochMilli()
            val doneEntry =
                DoneEntryEntity(
                    testEntryId,
                    testDate,
                    doneAt,
                    EntryEntity.SyncStateEntity.PENDING,
                )
            coEvery { entryDao.markEntryAsDone(testEntryId) } returns Unit
            coEvery { doneEntryDao.insert(doneEntry) } returns Unit

            // When
            entryStore.markEntryAsDone(testEntryId, testDate)

            // Then
            coVerify(exactly = 1) { entryDao.markEntryAsDone(testEntryId) }
            coVerify(exactly = 1) { doneEntryDao.insert(doneEntry) }
        }

    @Test
    fun `markEntryAsDone should throw exception when dao throws exception`() =
        runTest {
            // Given
            val doneAt = appClock.nowLocalDate().toEpochMilli()
            val doneEntry =
                DoneEntryEntity(
                    testEntryId,
                    testDate,
                    doneAt,
                    EntryEntity.SyncStateEntity.PENDING,
                )
            val exception = RuntimeException("Database error")
            coEvery { entryDao.markEntryAsDone(testEntryId) } throws exception
            coEvery { doneEntryDao.insert(doneEntry) } returns Unit

            // When & Then
            val thrownException =
                assertFailsWith<RuntimeException> {
                    entryStore.markEntryAsDone(testEntryId, testDate)
                }
            assertEquals(exception, thrownException)
            coVerify(exactly = 1) { entryDao.markEntryAsDone(testEntryId) }
        }

    @Test
    fun `saveEntry should call dao insert`() =
        runTest {
            // Given
            coJustRun { entryDao.insert(testEntry) }

            // When
            entryStore.saveEntry(testEntry)

            // Then
            coVerify(exactly = 1) { entryDao.insert(testEntry) }
        }

    @Test
    fun `saveEntry should throw exception when dao throws exception`() =
        runTest {
            // Given
            val exception = RuntimeException("Database error")
            coEvery { entryDao.insert(testEntry) } coAnswers { throw exception }

            // When & Then
            val thrownException =
                assertFailsWith<RuntimeException> {
                    entryStore.saveEntry(testEntry)
                }
            assertEquals(exception, thrownException)
            coVerify(exactly = 1) { entryDao.insert(testEntry) }
        }

    @Test
    fun `upsertIfNewest should call dao upsertIfNewest`() =
        runTest {
            // Given
            coJustRun { entryDao.upsertIfNewest(testEntry) }

            // When
            entryStore.upsertIfNewest(testEntry)

            // Then
            coVerify(exactly = 1) { entryDao.upsertIfNewest(testEntry) }
        }

    @Test
    fun `upsertIfNewest should throw exception when dao throws exception`() =
        runTest {
            // Given
            val exception = RuntimeException("Database error")
            coEvery { entryDao.upsertIfNewest(testEntry) } coAnswers { throw exception }

            // When & Then
            val thrownException =
                assertFailsWith<RuntimeException> {
                    entryStore.upsertIfNewest(testEntry)
                }

            assertEquals(exception, thrownException)
            coVerify(exactly = 1) { entryDao.upsertIfNewest(testEntry) }
        }

    @Test
    fun `deleteEntry should save deleted entry with correct ID and State`() =
        runTest {
            // Given
            val stateToSave = EntryEntity.SyncStateEntity.PENDING
            coEvery { roomTransactionRunner.run<Unit>(any()) } coAnswers {
                val block = it.invocation.args[0] as suspend () -> Unit
                block.invoke()
            }

            coJustRun { entryDao.delete(testEntryId) }
            val deletedEntrySlot = slot<DeletedEntryEntity>()
            coJustRun { deletedEntryDao.save(capture(deletedEntrySlot)) }

            // When
            entryStore.deleteEntry(testEntryId, stateToSave)

            // Then
            coVerify(exactly = 1) { roomTransactionRunner.run<Unit>(any()) }
            coVerify(exactly = 1) { entryDao.delete(testEntryId) }
            coVerify(exactly = 1) { deletedEntryDao.save(any()) }

            // Verify the captured entity
            assertEquals(testEntryId, deletedEntrySlot.captured.id)
            assertEquals(stateToSave, deletedEntrySlot.captured.syncState)
        }

    @Test
    fun `deleteEntry should throw exception and not save when dao throws exception`() =
        runTest {
            // Given
            val exception = RuntimeException("Database error")

            coEvery { roomTransactionRunner.run<Unit>(any()) } coAnswers {
                val block = it.invocation.args[0] as suspend () -> Unit
                block.invoke()
            }
            coEvery { entryDao.delete(testEntryId) } throws exception

            // When & Then
            val thrownException =
                assertFailsWith<RuntimeException> {
                    entryStore.deleteEntry(testEntryId, EntryEntity.SyncStateEntity.PENDING)
                }
            assertEquals(exception, thrownException)
            coVerify(exactly = 1) { entryDao.delete(testEntryId) }
            coVerify(exactly = 0) { deletedEntryDao.save(any()) } // should not be called if delete fails
        }

    @Test
    fun `updateEntrySyncState should call dao updateSyncState`() =
        runTest {
            // Given
            coJustRun { entryDao.updateSyncState(testEntryId, testSyncState) }

            // When
            entryStore.updateEntrySyncState(testEntryId, testSyncState)

            // Then
            coVerify(exactly = 1) { entryDao.updateSyncState(testEntryId, testSyncState) }
        }

    @Test
    fun `updateEntrySyncState should throw exception when dao throws exception`() =
        runTest {
            // Given
            val exception = RuntimeException("Database error")
            coEvery { entryDao.updateSyncState(testEntryId, testSyncState) } coAnswers { throw exception }

            // When & Then
            val thrownException =
                assertFailsWith<RuntimeException> {
                    entryStore.updateEntrySyncState(testEntryId, testSyncState)
                }
            assertEquals(exception, thrownException)
            coVerify(exactly = 1) { entryDao.updateSyncState(testEntryId, testSyncState) }
        }

    @Test
    fun `streamPendingEntries should return flow of pending entries from dao`() =
        runTest {
            // Given
            every { entryDao.streamPendingEntries() } returns flowOf(fakeEntryList)

            // When & Then
            entryStore.streamPendingEntries().test {
                assertEquals(fakeEntryList, awaitItem())
                awaitComplete()
            }
            verify(exactly = 1) { entryDao.streamPendingEntries() }
        }

    @Test
    fun `getAllEntriesByDateAndOrderedByTime should return flow of entries from dao`() =
        runTest {
            // Given
            every { entryDao.getAllEntriesByDateAndOrderedByTimeAsc(testDate) } returns flowOf(fakeEntryList)

            // When & Then
            entryStore.getAllEntriesByDateAndOrderedByTime(testDate).test {
                assertEquals(fakeEntryList, awaitItem())
                awaitComplete()
            }
            verify(exactly = 1) { entryDao.getAllEntriesByDateAndOrderedByTimeAsc(testDate) }
        }

    @Test
    fun `getEntriesBeforeOrOn should return flow of entries from dao`() =
        runTest {
            val doneEntryList =
                fakeEntryList.map { entry ->
                    DoneEntryView(
                        entry = entry,
                        doneDates = emptyList(),
                    )
                }
            // Given
            every { entryDao.getEntriesBeforeOrOn(testDate) } returns flowOf(doneEntryList)

            // When & Then
            entryStore.getEntriesBeforeOrOn(testDate).test {
                val entries = awaitItem()
                assertEquals(fakeEntryList, entries)
                awaitComplete()
            }
            verify(exactly = 1) { entryDao.getEntriesBeforeOrOn(testDate) }
        }

    fun `getEntriesBeforeOrOn with task done in same queried date should return flow of entries from dao with done entry`() =
        runTest {
            // Given
            val doneEntryList =
                fakeEntryList.map { entry ->
                    DoneEntryView(
                        entry = entry,
                        doneDates = listOf(testDate),
                    )
                }
            val expectedList =
                fakeEntryList
                    .filter { it.id == testEntryId }
                    .map { entry -> entry.copy(isDone = true) }

            every { entryDao.getEntriesBeforeOrOn(testDate) } returns flowOf(doneEntryList)

            // When
            entryStore.getEntriesBeforeOrOn(testDate).test {
                val entries = awaitItem()
                assertEquals(expectedList, entries)
                assertTrue { entries.first { it.id == testEntryId }.isDone }
                awaitComplete()
            }
            verify(exactly = 1) { entryDao.getEntriesBeforeOrOn(testDate) }
        }

    @Test
    fun `getEntriesBeforeOrOn with task done in a different queried date should return flow of entries from dao without done entry`() =
        runTest {
            // Given
            val doneEntryList =
                fakeEntryList.map { entry ->
                    DoneEntryView(
                        entry = entry,
                        doneDates = emptyList(),
                    )
                }
            val fakeEntryListWithoutDoneTask =
                every { entryDao.getEntriesBeforeOrOn(testDate) } returns flowOf(doneEntryList)

            // When
            entryStore.getEntriesBeforeOrOn(testDate).test {
                val entries = awaitItem()
                assertEquals(fakeEntryList, entries)
                assertFalse { entries.first { it.id == testEntryId }.isDone }
                awaitComplete()
            }
            verify(exactly = 1) { entryDao.getEntriesBeforeOrOn(testDate) }
        }
}
