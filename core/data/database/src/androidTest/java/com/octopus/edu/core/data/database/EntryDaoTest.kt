package com.octopus.edu.core.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.octopus.edu.core.data.database.dao.EntryDao
import com.octopus.edu.core.data.database.entity.EntryEntity
import com.octopus.edu.core.data.database.entity.EntryEntity.EntryType
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class EntryDaoTest {
    private lateinit var database: TrackMateDatabase
    private lateinit var dao: EntryDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, TrackMateDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        dao = database.entryDao()
    }

    @After
    @Throws(IOException::class)
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveHabitsAndTasks() =
        runTest {
            // Given
            val habit = createTestEntry(id = "h1", type = EntryType.HABIT)
            val task = createTestEntry(id = "t1", type = EntryType.TASK)

            // When
            dao.insert(habit)
            dao.insert(task)

            val habits = dao.getHabits()
            val tasks = dao.getTasks()

            // Then
            assertEquals(1, habits.size)
            assertEquals(1, tasks.size)
            assertEquals("h1", habits[0].id)
            assertEquals("t1", tasks[0].id)
        }

    @Test
    fun getEntryById_returnsCorrectEntry() =
        runTest {
            // Given
            val entry = createTestEntry(id = "entry1")
            dao.insert(entry)

            // When
            val retrievedEntry = dao.getEntryById("entry1")

            // Then
            assertNotNull(retrievedEntry)
            assertEquals("entry1", retrievedEntry.id)
        }

    @Test
    fun getEntryById_returnsNull_forNonExistentId() =
        runTest {
            // When
            val retrievedEntry = dao.getEntryById("non-existent")

            // Then
            assertNull(retrievedEntry)
        }

    @Test
    fun delete_removesEntry_fromDatabase() =
        runTest {
            // Given
            val entry = createTestEntry(id = "entryToDelete")
            dao.insert(entry)
            assertNotNull(dao.getEntryById("entryToDelete"))

            // When
            dao.delete("entryToDelete")

            // Then
            assertNull(dao.getEntryById("entryToDelete"))
        }

    @Test
    fun updateSyncState_updatesStateCorrectly() =
        runTest {
            // Given
            val entry = createTestEntry(id = "sync_test", syncState = SyncStateEntity.PENDING)
            dao.insert(entry)

            // When
            dao.updateSyncState("sync_test", SyncStateEntity.SYNCED)

            // Then
            val updatedEntry = dao.getEntryById("sync_test")
            assertNotNull(updatedEntry)
            assertEquals(SyncStateEntity.SYNCED, updatedEntry.syncState)
        }

    @Test
    fun getPendingEntries_returnsOnlyPending() =
        runTest {
            // Given
            dao.insert(createTestEntry(id = "1", syncState = SyncStateEntity.PENDING))
            dao.insert(createTestEntry(id = "2", syncState = SyncStateEntity.SYNCED))
            dao.insert(createTestEntry(id = "3", syncState = SyncStateEntity.PENDING))
            dao.insert(createTestEntry(id = "4", syncState = SyncStateEntity.FAILED))

            // When
            val pendingEntries = dao.getPendingEntries()

            // Then
            assertEquals(2, pendingEntries.size)
            assertTrue(pendingEntries.all { it.syncState == SyncStateEntity.PENDING })
            assertEquals(listOf("1", "3"), pendingEntries.map { it.id }.sorted())
        }

    @Test
    fun streamPendingEntries_emitsCorrectly() =
        runTest {
            // Given
            dao.insert(createTestEntry(id = "1", syncState = SyncStateEntity.PENDING))
            dao.insert(createTestEntry(id = "2", syncState = SyncStateEntity.SYNCED))

            // When
            val pendingEntries = dao.streamPendingEntries().first()

            // Then
            assertEquals(1, pendingEntries.size)
            assertEquals("1", pendingEntries.first().id)
        }

    @Test
    fun upsertIfNewest_inserts_whenNoLocalEntryExists() =
        runTest {
            // Given
            val newEntry = createTestEntry(id = "upsert_new")

            // When
            dao.upsertIfNewest(newEntry)

            // Then
            val retrieved = dao.getEntryById("upsert_new")
            assertNotNull(retrieved)
            assertEquals("upsert_new", retrieved.id)
        }

    @Test
    fun upsertIfNewest_replaces_whenIncomingEntryIsNewer() =
        runTest {
            // Given
            val currentTime = System.currentTimeMillis()
            val localEntry = createTestEntry(id = "upsert_replace", updatedAt = currentTime, title = "Old Title")
            dao.insert(localEntry)

            val newerEntry = createTestEntry(id = "upsert_replace", updatedAt = currentTime + 1000, title = "New Title")

            // When
            dao.upsertIfNewest(newerEntry)

            // Then
            val retrieved = dao.getEntryById("upsert_replace")
            assertNotNull(retrieved)
            assertEquals("New Title", retrieved.title)
        }

    @Test
    fun upsertIfNewest_doesNotReplace_whenLocalEntryIsNewer() =
        runTest {
            // Given
            val currentTime = System.currentTimeMillis()
            val localEntry = createTestEntry(id = "upsert_keep", updatedAt = currentTime + 1000, title = "Current Title")
            dao.insert(localEntry)

            val olderEntry = createTestEntry(id = "upsert_keep", updatedAt = currentTime, title = "Old Title")

            // When
            dao.upsertIfNewest(olderEntry)

            // Then
            val retrieved = dao.getEntryById("upsert_keep")
            assertNotNull(retrieved)
            assertEquals("Current Title", retrieved.title)
        }

    private fun createTestEntry(
        id: String,
        type: EntryType = EntryType.TASK,
        title: String = "Test Entry",
        updatedAt: Long? = System.currentTimeMillis(),
        syncState: SyncStateEntity = SyncStateEntity.SYNCED
    ): EntryEntity =
        EntryEntity(
            id = id,
            type = type,
            title = title,
            description = "Description for $id",
            isDone = false,
            dueDate = if (type == EntryType.TASK) LocalDate.now().plusDays(2).toEpochDay() else null,
            startDate = if (type == EntryType.HABIT) LocalDate.now().toEpochDay() else null,
            time = null,
            recurrence = if (type == EntryType.HABIT) EntryEntity.Recurrence.DAILY else null,
            streakCount = if (type == EntryType.HABIT) 5 else null,
            lastCompletedDate = if (type == EntryType.HABIT) LocalDate.now().toEpochDay() else null,
            isArchived = false,
            createdAt = System.currentTimeMillis() - 10000,
            updatedAt = updatedAt,
            syncState = syncState,
        )
}
