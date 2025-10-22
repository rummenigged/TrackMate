package com.octopus.edu.core.domain.repository

import com.octopus.edu.core.domain.model.DeletedEntry
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.SyncState
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.model.common.ResultOperation
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface EntryRepository {
    val pendingEntries: Flow<List<Entry>>

    val deletedEntryIds: Flow<List<String>>

    /**
 * Retrieves all tasks available in the repository.
 *
 * @return A ResultOperation containing the list of tasks on success, or an error state on failure.
 */
suspend fun getTasks(): ResultOperation<List<Task>>

    /**
 * Fetches all habit entries available to the repository.
 *
 * @return A ResultOperation containing the list of habits on success, or an error state on failure.
 */
suspend fun getHabits(): ResultOperation<List<Habit>>

    suspend fun saveEntry(entry: Entry): ResultOperation<Unit>

    suspend fun getEntryById(id: String): ResultOperation<Entry>

    /**
 * Observes entries visible on a given date.
 *
 * @param date The date used to determine visibility; defaults to the current date.
 * @return A flow that emits a `ResultOperation` wrapping the list of entries visible on the specified date.
 */
fun getEntriesVisibleOn(date: LocalDate = LocalDate.now()): Flow<ResultOperation<List<Entry>>>

    /**
 * Deletes the entry with the given identifier.
 *
 * @param entryId The unique identifier of the entry to delete.
 * @return A ResultOperation containing `Unit` on success, or an error describing the failure.
 */
suspend fun deleteEntry(entryId: String): ResultOperation<Unit>

    /**
 * Pushes the given entry to the remote synchronization target.
 *
 * @param entry The entry to push.
 * @return A ResultOperation containing `Unit` on success or an error describing the failure.
 */
suspend fun pushEntry(entry: Entry): ResultOperation<Unit>

    /**
     * Update the synchronization state of the entry identified by the given ID.
     *
     * @param entryId The identifier of the entry whose sync state will be updated.
     * @param syncState The new synchronization state to assign to the entry.
     * @return A ResultOperation containing `Unit` on success, or an error on failure.
     */
    suspend fun updateEntrySyncState(
        entryId: String,
        syncState: SyncState
    ): ResultOperation<Unit>

    /**
 * Retrieves entries that are currently pending (for example, awaiting synchronization or finalization).
 *
 * @return A ResultOperation wrapping the list of pending entries, or an error if the retrieval fails.
 */
suspend fun getPendingEntries(): ResultOperation<List<Entry>>

    /**
 * Synchronizes entries between local storage and the remote backend.
 *
 * Performs synchronization for pending and deleted entries and updates their sync states as needed.
 *
 * @return A ResultOperation containing `Unit` on success, or an error describing why synchronization failed.
 */
suspend fun syncEntries(): ResultOperation<Unit>

    /**
 * Retrieves a deleted entry by its identifier.
 *
 * @param entryId The identifier of the deleted entry to retrieve.
 * @return A ResultOperation containing the matching DeletedEntry on success, or an error result otherwise.
 */
suspend fun getDeletedEntry(entryId: String): ResultOperation<DeletedEntry>

    /**
 * Pushes a deleted entry to the remote store for synchronization.
 *
 * @param deletedEntry The deleted entry to push and synchronize.
 * @return A ResultOperation containing `Unit` on success or an error describing the failure.
 */
suspend fun pushDeletedEntry(deletedEntry: DeletedEntry): ResultOperation<Unit>

    /**
     * Sets the synchronization state for a deleted entry identified by its ID.
     *
     * @param entryId The ID of the deleted entry to update.
     * @param syncState The synchronization state to assign to the deleted entry.
     * @return A ResultOperation<Unit> indicating whether the update succeeded or failed.
     */
    suspend fun updateDeletedEntrySyncState(
        entryId: String,
        syncState: SyncState
    ): ResultOperation<Unit>
}