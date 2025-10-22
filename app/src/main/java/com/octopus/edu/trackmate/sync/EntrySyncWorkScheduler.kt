package com.octopus.edu.trackmate.sync

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.octopus.edu.core.domain.scheduler.EntrySyncScheduler
import com.octopus.edu.trackmate.workManager.sync.SyncDeletedEntryWorker
import com.octopus.edu.trackmate.workManager.sync.SyncEntryWorker
import com.octopus.edu.trackmate.workManager.sync.SyncPendingEntriesWorker
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class EntrySyncWorkScheduler
    @Inject
    constructor(
        private val workManager: WorkManager
    ) : EntrySyncScheduler {
        /**
         * Schedules a unique one-time background sync for the specified entry.
         *
         * The scheduled work requires network connectivity, uses exponential backoff on failures,
         * is tagged with the entry id, and replaces any existing work with the same unique name.
         *
         * @param entryId Identifier of the entry to sync; used to compose the unique work name and tag.
         */
        override fun scheduleEntrySync(entryId: String) {
            val workData = workDataOf(ENTRY_ID_EXTRA to entryId)
            val work =
                OneTimeWorkRequestBuilder<SyncEntryWorker>()
                    .setInputData(workData)
                    .setConstraints(
                        Constraints
                            .Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build(),
                    ).setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                    .addTag(TAG_ENTRY_PREFIX + entryId)
                    .build()

            workManager.enqueueUniqueWork(
                uniqueWorkName = UNIQUE_ENTRY_WORK_NAME_PREFIX + entryId,
                existingWorkPolicy = ExistingWorkPolicy.REPLACE,
                work,
            )
        }

        /**
         * Schedules a periodic background job to batch-sync pending entries every 30 minutes.
         *
         * The work requires network connectivity, uses exponential backoff with a 30-second initial delay,
         * is tagged for identification, and is enqueued as a unique periodic work that replaces any existing schedule.
         */
        override fun scheduleBatchSync() {
            val work =
                PeriodicWorkRequestBuilder<SyncPendingEntriesWorker>(Duration.ofMinutes(30))
                    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                    .addTag(TAG_BATCH)
                    .build()

            workManager.enqueueUniquePeriodicWork(
                UNIQUE_BATCH_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                work,
            )
        }

        /**
         * Schedules a one-time background sync to propagate a deleted entry identified by [entryId].
         *
         * The scheduled work requires network connectivity, uses exponential backoff, and is enqueued
         * as unique work that replaces any existing work for the same entry.
         *
         * @param entryId The identifier of the deleted entry to be synced. 
         */
        override fun scheduleDeletedEntrySync(entryId: String) {
            val data = workDataOf(ENTRY_ID_EXTRA to entryId)
            val work =
                OneTimeWorkRequestBuilder<SyncDeletedEntryWorker>()
                    .setInputData(data)
                    .setConstraints(
                        Constraints
                            .Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build(),
                    ).setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                    .addTag(TAG_DELETED_ENTRY_PREFIX + entryId)
                    .build()

            workManager.enqueueUniqueWork(
                uniqueWorkName = UNIQUE_DELETED_ENTRY_WORK_NAME_PREFIX + entryId,
                existingWorkPolicy = ExistingWorkPolicy.REPLACE,
                request = work,
            )
        }

        /**
         * Cancels any scheduled sync work associated with the specified entry.
         *
         * @param entryId The identifier of the entry whose scheduled sync work should be canceled.
         */
        override fun cancelEntrySync(entryId: String) {
            workManager.cancelUniqueWork(UNIQUE_ENTRY_WORK_NAME_PREFIX + entryId)
        }

        companion object {
            const val ENTRY_ID_EXTRA = "entry_id"
            private const val UNIQUE_ENTRY_WORK_NAME_PREFIX = "sync_entry_"
            private const val UNIQUE_DELETED_ENTRY_WORK_NAME_PREFIX = "sync_deleted_entry_"
            private const val UNIQUE_BATCH_WORK_NAME = "sync_pending_entries"
            private const val TAG_ENTRY_PREFIX = "sync-entry-"
            private const val TAG_DELETED_ENTRY_PREFIX = "sync-deleted-entry-"
            private const val TAG_BATCH = "sync-pending-entries"
        }
    }