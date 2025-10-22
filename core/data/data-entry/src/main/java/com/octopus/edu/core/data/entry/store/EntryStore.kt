package com.octopus.edu.core.data.entry.store

import com.octopus.edu.core.common.TransactionRunner
import com.octopus.edu.core.data.database.dao.DeletedEntryDao
import com.octopus.edu.core.data.database.dao.EntryDao
import com.octopus.edu.core.data.database.entity.DeletedEntryEntity
import com.octopus.edu.core.data.database.entity.EntryEntity
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface EntryStore {
    suspend fun getHabits(): List<EntryEntity>

    /**
 * Retrieves all entries classified as tasks.
 *
 * @return A list of EntryEntity objects representing tasks.
 */
suspend fun getTasks(): List<EntryEntity>

    /**
 * Persists the given entry in the entry store.
 *
 * @param entry The entry to persist.
 */
suspend fun saveEntry(entry: EntryEntity)

    /**
 * Inserts the provided entry or updates an existing entry with the same id only if the provided entry is newer.
 *
 * @param entry The entry to insert or update; the operation is a no-op if an equal-or-newer entry already exists.
 */
suspend fun upsertIfNewest(entry: EntryEntity)

    /**
 * Retrieve an entry by its unique identifier.
 *
 * @param id The unique identifier of the entry.
 * @return The matching EntryEntity, or `null` if no entry exists for the given id.
 */
suspend fun getEntryById(id: String): EntryEntity?

    /**
     * Removes the entry with the given id and records a deleted-entry record with the provided sync state and the current timestamp.
     *
     * @param entryId The id of the entry to delete.
     * @param state The synchronization state to assign to the recorded deleted entry.
     */
    suspend fun deleteEntry(
        entryId: String,
        state: SyncStateEntity
    )

    /**
 * Fetches the deleted entry record for the given entry id.
 *
 * @param entryId The identifier of the entry to look up.
 * @return `DeletedEntryEntity` if a deleted entry exists for the given id, `null` otherwise.
 */
suspend fun getDeletedEntry(entryId: String): DeletedEntryEntity?

    /**
     * Update the synchronization state for the entry with the given id.
     *
     * @param entryId The id of the entry whose sync state will be updated.
     * @param syncStateEntity The new synchronization state to apply to the entry.
     */
    suspend fun updateEntrySyncState(
        entryId: String,
        syncStateEntity: SyncStateEntity
    )

    /**
     * Update the synchronization state of a deleted entry identified by its id.
     *
     * @param entryId The id of the deleted entry whose sync state will be updated.
     * @param syncState The new synchronization state to assign to the deleted entry.
     */
    suspend fun updateDeletedEntrySyncState(
        entryId: String,
        syncState: SyncStateEntity
    )

    /**
 * Retrieve entries that are pending synchronization.
 *
 * @return A list of `EntryEntity` objects whose sync state indicates they are pending. 
 */
suspend fun getPendingEntries(): List<EntryEntity>

    /**
 * Stream entries for the specified date ordered by time.
 *
 * @param date Epoch milliseconds representing the target date.
 * @return A Flow that emits lists of EntryEntity for the given date ordered by time (ascending).
 */
fun getAllEntriesByDateAndOrderedByTime(date: Long): Flow<List<EntryEntity>>

    /**
 * Streams entries whose date is on or before the provided cutoff.
 *
 * @param date Cutoff date in milliseconds since the Unix epoch; entries with a stored date less than or equal to this value are included.
 * @return A Flow that emits lists of EntryEntity with dates on or before the given cutoff.
 */
fun getEntriesBeforeOrOn(date: Long): Flow<List<EntryEntity>>

    /**
 * Emits the current list of entries that are pending synchronization.
 *
 * @return A flow that emits the list of EntryEntity objects which are pending sync on each update.
 */
fun streamPendingEntries(): Flow<List<EntryEntity>>

    /**
 * Streams lists of deleted entries that are pending synchronization.
 *
 * @return A Flow that emits lists of DeletedEntryEntity representing entries marked as deleted and awaiting sync.
 */
fun streamPendingDeletedEntries(): Flow<List<DeletedEntryEntity>>
}

internal class EntryStoreImpl
    @Inject
    constructor(
        private val entryDao: EntryDao,
        private val deletedEntryDao: DeletedEntryDao,
        private val roomTransactionRunner: TransactionRunner
    ) : EntryStore {
        /**
 * Fetches all entries categorized as habits.
 *
 * @return A list of `EntryEntity` objects representing habit entries.
 */
override suspend fun getHabits(): List<EntryEntity> = entryDao.getHabits()

        /**
 * Retrieves all entries classified as tasks.
 *
 * @return A list of EntryEntity objects representing task entries.
 */
override suspend fun getTasks(): List<EntryEntity> = entryDao.getTasks()

        /**
 * Retrieves all entries currently marked as pending synchronization.
 *
 * @return A list of EntryEntity items that are pending synchronization.
 */
override suspend fun getPendingEntries(): List<EntryEntity> = entryDao.getPendingEntries()

        /**
 * Retrieves the entry with the given id.
 *
 * @param id The unique identifier of the entry to retrieve.
 * @return The EntryEntity with the matching id, or `null` if none exists.
 */
override suspend fun getEntryById(id: String) = entryDao.getEntryById(id)

        /**
 * Persists the provided entry into the entry store.
 *
 * @param entry The entry to save.
 */
override suspend fun saveEntry(entry: EntryEntity) = entryDao.insert(entry)

        /**
 * Inserts the given entry or updates the stored entry when the provided entry is newer.
 *
 * @param entry The EntryEntity to insert or update; applied only if it is newer than the existing record.
 */
override suspend fun upsertIfNewest(entry: EntryEntity) = entryDao.upsertIfNewest(entry)

        /**
         * Update the stored synchronization state for the entry with the given id.
         *
         * @param entryId The identifier of the entry to update.
         * @param syncStateEntity The new synchronization state to apply to the entry.
         */
        override suspend fun updateEntrySyncState(
            entryId: String,
            syncStateEntity: SyncStateEntity
        ) = entryDao.updateSyncState(entryId, syncStateEntity)

        /**
         * Updates the synchronization state of a deleted entry identified by the given id.
         *
         * @param entryId The id of the deleted entry to update.
         * @param syncState The new synchronization state to assign to the deleted entry.
         */
        override suspend fun updateDeletedEntrySyncState(
            entryId: String,
            syncState: SyncStateEntity
        ) {
            deletedEntryDao.updateSyncState(entryId, syncState)
        }

        /**
         * Deletes the entry with the given id and records a deleted-entry marker with the provided sync state atomically.
         *
         * @param entryId The id of the entry to delete.
         * @param state The sync state to assign to the recorded DeletedEntryEntity; the recorded marker will include the current timestamp.
         */
        override suspend fun deleteEntry(
            entryId: String,
            state: SyncStateEntity
        ) {
            roomTransactionRunner.run {
                entryDao.delete(entryId)
                deletedEntryDao.save(
                    DeletedEntryEntity(
                        id = entryId,
                        deletedAt = System.currentTimeMillis(),
                        syncState = state,
                    ),
                )
            }
        }

        /**
 * Retrieves the deleted entry with the given id.
 *
 * @param entryId The id of the entry to retrieve.
 * @return The corresponding DeletedEntryEntity, or `null` if no deleted entry exists for the id.
 */
override suspend fun getDeletedEntry(entryId: String): DeletedEntryEntity? = deletedEntryDao.getDeletedEntry(entryId)

        /**
 * Streams lists of entries that are pending synchronization.
 *
 * @return A Flow that emits the current list of pending EntryEntity items whenever it changes.
 */
override fun streamPendingEntries(): Flow<List<EntryEntity>> = entryDao.streamPendingEntries()

        /**
 * Observes deleted entries that are pending synchronization.
 *
 * @return A Flow that emits lists of DeletedEntryEntity representing entries pending sync.
 */
override fun streamPendingDeletedEntries(): Flow<List<DeletedEntryEntity>> = deletedEntryDao.streamPendingDeletedEntries()

        /**
             * Retrieve entries for the given date ordered by time ascending.
             *
             * @param date Milliseconds since epoch representing the date to query (UTC).
             * @return A Flow of lists of entries for the given date ordered by time ascending.
             */
            override fun getAllEntriesByDateAndOrderedByTime(date: Long): Flow<List<EntryEntity>> =
            entryDao.getAllEntriesByDateAndOrderedByTimeAsc(date)

        /**
 * Streams entries whose date is less than or equal to the given date.
 *
 * @param date Timestamp in milliseconds since Unix epoch to compare entry dates against.
 * @return A Flow that emits lists of EntryEntity with entry date less than or equal to `date`.
 */
override fun getEntriesBeforeOrOn(date: Long): Flow<List<EntryEntity>> = entryDao.getEntriesBeforeOrOn(date)
    }