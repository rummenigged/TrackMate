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
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity
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
import java.time.Instant
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
    private val testSyncState = SyncStateEntity.SYNCED
    private val testDate = LocalDate.now().toEpochMilli()
    private val testNowTimestamp = 123456789L
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
            syncState = SyncStateEntity.PENDING,
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

        every { appClock.nowInstant() } returns Instant.ofEpochMilli(testNowTimestamp)
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
                        syncState = SyncStateEntity.SYNCED,
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
                        syncState = SyncStateEntity.SYNCED,
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
    fun `markEntryAsDone should call dao with correct confirmed status`() =
        runTest {
            val captor = slot<DoneEntryEntity>()
            coJustRun { doneEntryDao.insert(capture(captor)) }

            // Test for isConfirmed = true
            entryStore.markEntryAsDone(testEntryId, testDate, isConfirmed = true)
            assertTrue(captor.captured.isConfirmed)

            // Test for isConfirmed = false
            entryStore.markEntryAsDone(testEntryId, testDate, isConfirmed = false)
            assertFalse(captor.captured.isConfirmed)
        }

    @Test
    fun `markEntryAsDone should call dao with correct pending state and timestamp`() =
        runTest {
            val captor = slot<DoneEntryEntity>()
            coJustRun { doneEntryDao.insert(capture(captor)) }

            entryStore.markEntryAsDone(testEntryId, testDate, isConfirmed = false)

            assertEquals(testEntryId, captor.captured.entryId)
            assertEquals(testDate, captor.captured.entryDate)
            assertEquals(testNowTimestamp, captor.captured.doneAt)
            assertEquals(SyncStateEntity.PENDING, captor.captured.syncState)

            coVerify(exactly = 1) { doneEntryDao.insert(any()) }
        }

    @Test
    fun `markEntryAsDone should propagate exception when dao throws`() =
        runTest {
            val exception = RuntimeException("DB Error")
            coEvery { doneEntryDao.insert(any()) } throws exception

            val thrown =
                assertFailsWith<RuntimeException> {
                    entryStore.markEntryAsDone(testEntryId, testDate, false)
                }
            assertEquals(exception, thrown)
        }

    @Test
    fun `unmarkEntryAsDone should call dao delete`() =
        runTest {
            coJustRun { doneEntryDao.delete(testEntryId, testDate) }

            entryStore.unmarkEntryAsDone(testEntryId, testDate)

            coVerify(exactly = 1) { doneEntryDao.delete(testEntryId, testDate) }
        }

    @Test
    fun `unmarkEntryAsDone should propagate exception when dao throws`() =
        runTest {
            val exception = RuntimeException("DB Error")
            coEvery { doneEntryDao.delete(any(), any()) } throws exception

            val thrown =
                assertFailsWith<RuntimeException> {
                    entryStore.unmarkEntryAsDone(testEntryId, testDate)
                }
            assertEquals(exception, thrown)
        }

    @Test
    fun `confirmEntryAsDone should call dao updateIsConfirmed`() =
        runTest {
            coJustRun { doneEntryDao.updateIsConfirmed(testEntryId, testDate, true) }

            entryStore.confirmEntryAsDone(testEntryId, testDate)

            coVerify(exactly = 1) { doneEntryDao.updateIsConfirmed(testEntryId, testDate, true) }
        }

    @Test
    fun `confirmEntryAsDone should propagate exception when dao throws`() =
        runTest {
            val exception = RuntimeException("DB Error")
            coEvery { doneEntryDao.updateIsConfirmed(any(), any(), any()) } throws exception

            val thrown =
                assertFailsWith<RuntimeException> {
                    entryStore.confirmEntryAsDone(testEntryId, testDate)
                }
            assertEquals(exception, thrown)
        }

    @Test
    fun `getDoneEntry returns entry from DAO`() =
        runTest {
            val expected = mockk<DoneEntryEntity>()
            coEvery { doneEntryDao.getDoneEntry(testEntryId, testDate) } returns expected

            val result = entryStore.getDoneEntry(testEntryId, testDate)

            assertEquals(expected, result)
            coVerify(exactly = 1) { doneEntryDao.getDoneEntry(testEntryId, testDate) }
        }

    @Test
    fun `updateDoneEntrySyncState calls DAO with correct parameters`() =
        runTest {
            coJustRun { doneEntryDao.updateSyncState(any(), any(), any()) }

            entryStore.updateDoneEntrySyncState(testEntryId, testDate, SyncStateEntity.SYNCED)

            coVerify(exactly = 1) { doneEntryDao.updateSyncState(testEntryId, testDate, SyncStateEntity.SYNCED) }
        }

    @Test
    fun `streamPendingDoneEntries returns flow from DAO`() =
        runTest {
            val fakeFlow = flowOf(listOf(mockk<DoneEntryEntity>()))
            every { doneEntryDao.streamPendingEntriesMarkedAsDone() } returns fakeFlow

            val result = entryStore.streamPendingDoneEntries()

            assertEquals(fakeFlow, result)
            verify(exactly = 1) { doneEntryDao.streamPendingEntriesMarkedAsDone() }
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
            val stateToSave = SyncStateEntity.PENDING

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

            coEvery { entryDao.delete(testEntryId) } throws exception

            // When & Then
            val thrownException =
                assertFailsWith<RuntimeException> {
                    entryStore.deleteEntry(testEntryId, SyncStateEntity.PENDING)
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
    fun `streamPendingDeletedEntries returns flow from DAO`() =
        runTest {
            // Given
            val fakeFlow = flowOf(listOf(mockk<DeletedEntryEntity>()))
            every { deletedEntryDao.streamPendingDeletedEntries() } returns fakeFlow

            // When
            val result = entryStore.streamPendingDeletedEntries()

            // Then
            assertEquals(fakeFlow, result)
            verify(exactly = 1) { deletedEntryDao.streamPendingDeletedEntries() }
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
    fun `getEntriesBeforeOrOn should map isDone correctly based on done dates`() =
        runTest {
            // Given
            val dateToTest = 100L
            val entry1 = testEntry.copy(id = "1")
            val entry2 = testEntry.copy(id = "2")

            val doneEntryView1 = DoneEntryView(entry = entry1, doneDates = listOf(dateToTest, 101L)) // isDone should be true
            val doneEntryView2 = DoneEntryView(entry = entry2, doneDates = listOf(99L, 101L)) // isDone should be false

            val fakeDoneEntryViewList = listOf(doneEntryView1, doneEntryView2)
            every { entryDao.getEntriesBeforeOrOn(dateToTest) } returns flowOf(fakeDoneEntryViewList)

            // Expected result after mapping
            val expectedEntry1 = entry1.copy(isDone = true)
            val expectedEntry2 = entry2.copy(isDone = false)
            val expectedList = listOf(expectedEntry1, expectedEntry2)

            // When & Then
            entryStore.getEntriesBeforeOrOn(dateToTest).test {
                val result = awaitItem()
                assertEquals(expectedList, result)
                awaitComplete()
            }

            verify(exactly = 1) { entryDao.getEntriesBeforeOrOn(dateToTest) }
        }
}
