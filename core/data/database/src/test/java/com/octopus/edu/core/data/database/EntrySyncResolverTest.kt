package com.octopus.edu.core.data.database

import com.octopus.edu.core.data.database.entity.DoneEntryEntity
import com.octopus.edu.core.data.database.entity.EntryEntity
import com.octopus.edu.core.data.database.utils.DoneEntrySyncResolver
import com.octopus.edu.core.data.database.utils.EntrySyncResolver
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EntrySyncResolverTest {
    @Test
    fun `shouldReplace returns true when new entry is newer`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val currentEntry = createEntryEntity(updatedAt = currentTime)
        val newEntry = createEntryEntity(updatedAt = currentTime + 1000)

        // When
        val result = EntrySyncResolver.shouldReplace(currentEntry, newEntry)

        // Then
        assertTrue(result)
    }

    @Test
    fun `shouldReplace returns false when current entry is newer`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val currentEntry = createEntryEntity(updatedAt = currentTime + 1000)
        val newEntry = createEntryEntity(updatedAt = currentTime)

        // When
        val result = EntrySyncResolver.shouldReplace(currentEntry, newEntry)

        // Then
        assertFalse(result)
    }

    @Test
    fun `shouldReplace returns false when timestamps are equal`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val currentEntry = createEntryEntity(updatedAt = currentTime)
        val newEntry = createEntryEntity(updatedAt = currentTime)

        // When
        val result = EntrySyncResolver.shouldReplace(currentEntry, newEntry)

        // Then
        assertFalse(result)
    }

    @Test
    fun `shouldReplace returns true when current entry updatedAt is null`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val currentEntry = createEntryEntity(updatedAt = null)
        val newEntry = createEntryEntity(updatedAt = currentTime)

        // When
        val result = EntrySyncResolver.shouldReplace(currentEntry, newEntry)

        // Then
        assertTrue(result)
    }

    @Test
    fun `shouldReplace returns false when new entry updatedAt is null`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val currentEntry = createEntryEntity(updatedAt = currentTime)
        val newEntry = createEntryEntity(updatedAt = null)

        // When
        val result = EntrySyncResolver.shouldReplace(currentEntry, newEntry)

        // Then
        assertFalse(result)
    }

    @Test
    fun `shouldReplace returns false when both updatedAt are null`() {
        // Given
        val currentEntry = createEntryEntity(updatedAt = null)
        val newEntry = createEntryEntity(updatedAt = null)

        // When
        val result = EntrySyncResolver.shouldReplace(currentEntry, newEntry)

        // Then
        assertFalse(result)
    }

    @Test
    fun `DoneEntrySyncResolver shouldReplace returns true when new entry is older`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val currentEntry = createDoneEntryEntity(doneAt = currentTime + 1000)
        val newEntry = createDoneEntryEntity(doneAt = currentTime)

        // When
        val result = DoneEntrySyncResolver.shouldReplace(currentEntry, newEntry)

        // Then
        assertTrue(result)
    }

    @Test
    fun `DoneEntrySyncResolver shouldReplace returns false when current entry is older`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val currentEntry = createDoneEntryEntity(doneAt = currentTime)
        val newEntry = createDoneEntryEntity(doneAt = currentTime + 1000)

        // When
        val result = DoneEntrySyncResolver.shouldReplace(currentEntry, newEntry)

        // Then
        assertFalse(result)
    }

    @Test
    fun `DoneEntrySyncResolver shouldReplace returns false when timestamps are equal`() {
        // Given
        val currentTime = System.currentTimeMillis()
        val currentEntry = createDoneEntryEntity(doneAt = currentTime)
        val newEntry = createDoneEntryEntity(doneAt = currentTime)

        // When
        val result = DoneEntrySyncResolver.shouldReplace(currentEntry, newEntry)

        // Then
        assertFalse(result)
    }

    private fun createEntryEntity(updatedAt: Long?): EntryEntity =
        EntryEntity(
            id = "1",
            type = EntryEntity.EntryType.TASK,
            title = "Test Entry",
            description = "Test Description",
            isDone = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = updatedAt,
            syncState = EntryEntity.SyncStateEntity.SYNCED,
        )

    private fun createDoneEntryEntity(doneAt: Long): DoneEntryEntity =
        DoneEntryEntity(
            entryId = "1",
            entryDate = 1L,
            doneAt = doneAt,
            isConfirmed = true,
            syncState = EntryEntity.SyncStateEntity.SYNCED,
        )
}
