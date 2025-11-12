package com.octopus.edu.core.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.octopus.edu.core.data.database.dao.DoneEntryDao
import com.octopus.edu.core.data.database.dao.EntryDao
import com.octopus.edu.core.data.database.entity.DoneEntryEntity
import com.octopus.edu.core.data.database.entity.EntryEntity
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class DoneEntryDaoTest {
    private lateinit var database: TrackMateDatabase
    private lateinit var dao: DoneEntryDao
    private lateinit var entryDao: EntryDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, TrackMateDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        dao = database.doneEntryDao()
        entryDao = database.entryDao()
    }

    @After
    @Throws(IOException::class)
    fun teardown() {
        database.clearAllTables()
        database.close()
    }

    @Test
    fun insertAndRetrieveDoneEntry() =
        runTest {
            // Given
            val entryId = "id1"
            val entryDate = 100L
            val parentEntry = createEntryEntity(id = entryId)
            val doneEntry = createDoneEntry(entryId = entryId, entryDate = entryDate)

            // When
            entryDao.insert(parentEntry)
            dao.insert(doneEntry)
            val retrieved = dao.getDoneEntry(entryId, entryDate)

            // Then
            assertNotNull(retrieved)
            assertEquals(doneEntry, retrieved)
        }

    @Test
    fun getDoneEntry_returnsNull_forNonExistentEntry() =
        runTest {
            // When
            val retrieved = dao.getDoneEntry("non-existent", 0L)

            // Then
            assertNull(retrieved)
        }

    @Test
    fun updateIsConfirmed_updatesTheConfirmationStatus() =
        runTest {
            // Given
            val entryId = "id2"
            val entryDate = 100L
            val parentEntry = createEntryEntity(id = entryId)
            val entry = createDoneEntry(entryId = entryId, entryDate = entryDate, isConfirmed = false)
            entryDao.insert(parentEntry)
            dao.insert(entry)

            // When
            dao.updateIsConfirmed(entryId, entryDate, isConfirmed = true)

            // Then
            val updated = dao.getDoneEntry(entryId, entryDate)
            assertNotNull(updated)
            assertTrue(updated.isConfirmed)
        }

    @Test
    fun delete_removesTheEntry() =
        runTest {
            // Given
            val entryId = "id-to-delete"
            val entryDate = 200L
            val parentEntry = createEntryEntity(id = entryId)
            val entry = createDoneEntry(entryId = entryId, entryDate = entryDate)
            entryDao.insert(parentEntry)
            dao.insert(entry)
            assertNotNull(dao.getDoneEntry(entryId, entryDate))

            // When
            dao.delete(entryId, entryDate)

            // Then
            assertNull(dao.getDoneEntry(entryId, entryDate))
        }

    @Test
    fun updateSyncState_updatesTheState() =
        runTest {
            // Given
            val entryId = "id_sync"
            val entryDate = 300L
            val parentEntry = createEntryEntity(id = entryId)
            val entry = createDoneEntry(entryId = entryId, entryDate = entryDate, syncState = SyncStateEntity.PENDING)
            entryDao.insert(parentEntry)
            dao.insert(entry)
            assertEquals(SyncStateEntity.PENDING, dao.getDoneEntry(entryId, entryDate)?.syncState)

            // When
            dao.updateSyncState(entryId, entryDate, SyncStateEntity.SYNCED)

            // Then
            val updated = dao.getDoneEntry(entryId, entryDate)
            assertNotNull(updated)
            assertEquals(SyncStateEntity.SYNCED, updated.syncState)
        }

    @Test
    fun streamPendingEntriesMarkedAsDone_returnsOnlyConfirmedAndPending() =
        runTest {
            // Given
            entryDao.insert(createEntryEntity(id = "e1"))
            entryDao.insert(createEntryEntity(id = "e2"))
            entryDao.insert(createEntryEntity(id = "e3"))
            entryDao.insert(createEntryEntity(id = "e4"))

            val pendingAndConfirmed =
                createDoneEntry(
                    entryId = "e1",
                    entryDate = 1L,
                    isConfirmed = true,
                    syncState = SyncStateEntity.PENDING,
                )
            val syncedAndConfirmed =
                createDoneEntry(
                    entryId = "e2",
                    entryDate = 2L,
                    isConfirmed = true,
                    syncState = SyncStateEntity.SYNCED,
                )
            val pendingAndNotConfirmed =
                createDoneEntry(
                    entryId = "e3",
                    entryDate = 3L,
                    isConfirmed = false,
                    syncState = SyncStateEntity.PENDING,
                )
            val anotherPendingAndConfirmed =
                createDoneEntry(
                    entryId = "e4",
                    entryDate = 4L,
                    isConfirmed = true,
                    syncState = SyncStateEntity.PENDING,
                )

            dao.insert(pendingAndConfirmed)
            dao.insert(syncedAndConfirmed)
            dao.insert(pendingAndNotConfirmed)
            dao.insert(anotherPendingAndConfirmed)

            // When
            val result = dao.streamPendingEntriesMarkedAsDone().first()

            // Then
            assertEquals(2, result.size)
            assertTrue(result.any { it.entryId == "e1" })
            assertTrue(result.any { it.entryId == "e4" })
            assertFalse(result.any { it.entryId == "e2" })
            assertFalse(result.any { it.entryId == "e3" })
        }

    private fun createEntryEntity(
        id: String,
        type: EntryEntity.EntryType = EntryEntity.EntryType.TASK,
        title: String = "Test Entry",
        updatedAt: Long? = System.currentTimeMillis(),
        syncState: SyncStateEntity = SyncStateEntity.SYNCED,
    ): EntryEntity =
        EntryEntity(
            id = id,
            type = type,
            title = title,
            description = "Description for $id",
            isDone = false,
            dueDate = if (type == EntryEntity.EntryType.TASK) System.currentTimeMillis() else null,
            startDate = if (type == EntryEntity.EntryType.HABIT) System.currentTimeMillis() else null,
            time = null,
            recurrence = if (type == EntryEntity.EntryType.HABIT) EntryEntity.Recurrence.DAILY else null,
            streakCount = if (type == EntryEntity.EntryType.HABIT) 5 else null,
            lastCompletedDate = if (type == EntryEntity.EntryType.HABIT) System.currentTimeMillis() else null,
            isArchived = false,
            createdAt = System.currentTimeMillis() - 10000,
            updatedAt = updatedAt,
            syncState = syncState,
        )

    private fun createDoneEntry(
        entryId: String = "test-id",
        entryDate: Long = 12345L,
        doneAt: Long = 98765L,
        isConfirmed: Boolean = false,
        syncState: SyncStateEntity = SyncStateEntity.PENDING,
    ) = DoneEntryEntity(
        entryId = entryId,
        entryDate = entryDate,
        doneAt = doneAt,
        isConfirmed = isConfirmed,
        syncState = syncState,
    )
}
