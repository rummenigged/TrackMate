package com.octopus.edu.core.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.octopus.edu.core.data.database.dao.DeletedEntryDao
import com.octopus.edu.core.data.database.entity.DeletedEntryEntity
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class DeletedEntryDaoTest {
    private lateinit var database: TrackMateDatabase
    private lateinit var dao: DeletedEntryDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, TrackMateDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        dao = database.deletedEntryDao()
    }

    @After
    @Throws(IOException::class)
    fun teardown() {
        database.close()
    }

    @Test
    fun saveAndRetrieveDeletedEntry() =
        runTest {
            // Given
            val deletedEntry = createTestDeletedEntry(id = "d1")

            // When
            dao.save(deletedEntry)
            val retrieved = dao.getDeletedEntry("d1")

            // Then
            assertNotNull(retrieved)
            assertEquals("d1", retrieved.id)
            assertEquals(deletedEntry.deletedAt, retrieved.deletedAt)
        }

    @Test
    fun getDeletedEntry_returnsNull_forNonExistentId() =
        runTest {
            // When
            val retrieved = dao.getDeletedEntry("non-existent-id")

            // Then
            assertNull(retrieved)
        }

    @Test
    fun streamPendingDeletedEntries_emitsCorrectly() =
        runTest {
            // Given
            dao.save(createTestDeletedEntry("pending1", syncState = SyncStateEntity.PENDING))
            dao.save(createTestDeletedEntry("synced1", syncState = SyncStateEntity.SYNCED))
            dao.save(createTestDeletedEntry("pending2", syncState = SyncStateEntity.PENDING))

            // When
            val pendingEntries = dao.streamPendingDeletedEntries().first()

            // Then
            assertEquals(2, pendingEntries.size)
            assertTrue(pendingEntries.all { it.syncState == SyncStateEntity.PENDING })
        }

    @Test
    fun updateSyncState_updatesStateCorrectly() =
        runTest {
            // Given
            val entryId = "update_test"
            dao.save(createTestDeletedEntry(entryId, syncState = SyncStateEntity.PENDING))

            // When
            dao.updateSyncState(entryId, SyncStateEntity.SYNCED)

            // Then
            val updatedEntry = dao.getDeletedEntry(entryId)
            assertNotNull(updatedEntry)
            assertEquals(SyncStateEntity.SYNCED, updatedEntry.syncState)
        }

    @Test
    fun delete_removesEntry_fromDatabase() =
        runTest {
            // Given
            val entryId = "delete_test"
            dao.save(createTestDeletedEntry(entryId))
            assertNotNull(dao.getDeletedEntry(entryId), "Entry should exist before deletion")

            // When
            dao.delete(entryId)

            // Then
            assertNull(dao.getDeletedEntry(entryId), "Entry should not exist after deletion")
        }

    private fun createTestDeletedEntry(
        id: String,
        deletedAt: Long = System.currentTimeMillis(),
        syncState: SyncStateEntity = SyncStateEntity.PENDING
    ) = DeletedEntryEntity(id = id, deletedAt = deletedAt, syncState = syncState)
}
