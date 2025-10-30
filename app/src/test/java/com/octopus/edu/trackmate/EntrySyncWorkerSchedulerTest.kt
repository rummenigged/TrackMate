package com.octopus.edu.trackmate

import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.octopus.edu.trackmate.sync.EntrySyncWorkScheduler
import com.octopus.edu.trackmate.workManager.sync.SyncDeletedEntryWorker
import com.octopus.edu.trackmate.workManager.sync.SyncEntryWorker
import com.octopus.edu.trackmate.workManager.sync.SyncPendingEntriesWorker
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Config.OLDEST_SDK])
class EntrySyncWorkerSchedulerTest {
    private lateinit var workManagerMock: WorkManager
    private lateinit var scheduler: EntrySyncWorkScheduler

    @Before
    fun setup() {
        workManagerMock = mockk(relaxed = true)
        scheduler = EntrySyncWorkScheduler(workManagerMock)
    }

    @Test
    fun `scheduleEntrySync enqueues unique work with correct parameters`() {
        // Given
        val entryId = "test-entry-1"
        val uniqueWorkName = "sync_entry_$entryId"
        val workRequestSlot = slot<OneTimeWorkRequest>()

        // When
        scheduler.scheduleEntrySync(entryId)

        // Then
        verify {
            workManagerMock.enqueueUniqueWork(
                uniqueWorkName,
                ExistingWorkPolicy.REPLACE,
                capture(workRequestSlot),
            )
        }
        val workSpec = workRequestSlot.captured.workSpec
        assertEquals(SyncEntryWorker::class.java.name, workSpec.workerClassName)
        assertEquals(entryId, workSpec.input.getString(EntrySyncWorkScheduler.ENTRY_ID_EXTRA))
        assertEquals(NetworkType.CONNECTED, workSpec.constraints.requiredNetworkType)
        assertEquals(BackoffPolicy.EXPONENTIAL, workSpec.backoffPolicy)
        assertEquals(10000, workSpec.backoffDelayDuration)
        assertTrue(workRequestSlot.captured.tags.contains("sync-entry-$entryId"))
    }

    @Test
    fun `scheduleBatchSync enqueues unique periodic work`() {
        // Given
        val uniqueWorkName = "sync_pending_entries"
        val periodicWorkRequestSlot = slot<PeriodicWorkRequest>()

        // When
        scheduler.scheduleBatchSync()

        // Then
        verify {
            workManagerMock.enqueueUniquePeriodicWork(
                uniqueWorkName,
                ExistingPeriodicWorkPolicy.REPLACE,
                capture(periodicWorkRequestSlot),
            )
        }

        val workSpec = periodicWorkRequestSlot.captured.workSpec
        assertEquals(SyncPendingEntriesWorker::class.java.name, workSpec.workerClassName)
        assertEquals(NetworkType.CONNECTED, workSpec.constraints.requiredNetworkType)
        assertEquals(BackoffPolicy.EXPONENTIAL, workSpec.backoffPolicy)
        assertEquals(30000, workSpec.backoffDelayDuration)
        assertEquals(TimeUnit.MINUTES.toMillis(30), workSpec.intervalDuration)
        assertTrue(periodicWorkRequestSlot.captured.tags.contains("sync-pending-entries"))
    }

    @Test
    fun `scheduleDeletedEntrySync enqueues unique work with correct parameters`() {
        // Given
        val entryId = "deleted-entry-1"
        val uniqueWorkName = "sync_deleted_entry_$entryId"
        val workRequestSlot = slot<OneTimeWorkRequest>()

        // When
        scheduler.scheduleDeletedEntrySync(entryId)

        // Then
        verify {
            workManagerMock.enqueueUniqueWork(
                uniqueWorkName,
                ExistingWorkPolicy.REPLACE,
                capture(workRequestSlot),
            )
        }

        val workSpec = workRequestSlot.captured.workSpec
        assertEquals(SyncDeletedEntryWorker::class.java.name, workSpec.workerClassName)
        assertEquals(entryId, workSpec.input.getString(EntrySyncWorkScheduler.ENTRY_ID_EXTRA))
        assertEquals(NetworkType.CONNECTED, workSpec.constraints.requiredNetworkType)
        assertEquals(BackoffPolicy.EXPONENTIAL, workSpec.backoffPolicy)
        assertEquals(10000, workSpec.backoffDelayDuration)
        assertTrue(workRequestSlot.captured.tags.contains("sync-deleted-entry-$entryId"))
    }

    @Test
    fun `cancelEntrySync cancels the correct work`() {
        // Given
        val entryId = "cancel-me"
        val uniqueWorkName = "sync_entry_$entryId" // The name used for enqueueing
        val workNameSlot = slot<String>()

        // When
        scheduler.cancelEntrySync(entryId)

        // Then
        verify { workManagerMock.cancelUniqueWork(capture(workNameSlot)) }
        assertEquals(uniqueWorkName, workNameSlot.captured)
    }
}
