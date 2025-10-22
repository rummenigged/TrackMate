package com.octopus.edu.core.domain.scheduler

interface EntrySyncScheduler {
    /**
 * Schedules a synchronization task for the entry identified by [entryId].
 *
 * @param entryId The unique identifier of the entry to synchronize.
 */
fun scheduleEntrySync(entryId: String)

    /**
 * Schedule synchronization for the next batch of entries.
 *
 * Implementations determine when and how the batch synchronization is executed. 
 */
fun scheduleBatchSync()

    /**
 * Schedules synchronization for a deleted entry so its deletion is propagated to remote systems.
 *
 * @param entryId Identifier of the entry that was deleted.
 */
fun scheduleDeletedEntrySync(entryId: String)

    /**
 * Cancels a previously scheduled synchronization for the entry with the given identifier.
 *
 * @param entryId The identifier of the entry whose scheduled sync should be canceled.
 */
fun cancelEntrySync(entryId: String)
}