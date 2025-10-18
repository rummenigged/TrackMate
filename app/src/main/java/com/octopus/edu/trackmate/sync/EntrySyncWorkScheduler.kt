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
