package com.octopus.edu.trackmate

import com.octopus.edu.core.common.RetryPolicy
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.model.common.ErrorType
import com.octopus.edu.core.domain.model.mockList
import com.octopus.edu.core.domain.repository.EntryRepository
import com.octopus.edu.core.domain.scheduler.EntrySyncScheduler
import com.octopus.edu.core.domain.utils.ErrorClassifier
import com.octopus.edu.core.testing.TestDispatchers
import com.octopus.edu.trackmate.sync.EntrySyncManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException

@ExperimentalCoroutinesApi
class EntrySyncManagerTest {
    private lateinit var entryRepository: EntryRepository
    private lateinit var syncScheduler: EntrySyncScheduler
    private lateinit var errorClassifier: ErrorClassifier
    private lateinit var retryPolicy: RetryPolicy
    private lateinit var testDispatchers: TestDispatchers
    private lateinit var testScope: TestScope

    private lateinit var entrySyncManager: EntrySyncManager

    private lateinit var pendingEntriesFlow: MutableStateFlow<List<Entry>>
    private lateinit var deletedEntryIdsFlow: MutableStateFlow<List<String>>

    @Before
    fun setUp() {
        entryRepository = mockk(relaxed = true)
        syncScheduler = mockk(relaxed = true)
        errorClassifier = mockk(relaxed = true)
        retryPolicy = mockk(relaxed = true)
        testDispatchers = TestDispatchers()
        testScope = TestScope(UnconfinedTestDispatcher())

        pendingEntriesFlow = MutableStateFlow(emptyList())
        deletedEntryIdsFlow = MutableStateFlow(emptyList())

        every { entryRepository.pendingEntries } returns pendingEntriesFlow
        every { entryRepository.deletedEntryIds } returns deletedEntryIdsFlow

        entrySyncManager =
            EntrySyncManager(
                entryRepository,
                syncScheduler,
                errorClassifier,
                retryPolicy,
                testDispatchers,
                testScope,
            )
    }

    @Test
    fun `start schedules initial batch sync`() =
        runTest {
            // When
            entrySyncManager.start()
            advanceUntilIdle()

            // Then
            verify(exactly = 1) { syncScheduler.scheduleBatchSync() }
        }

    @Test
    fun `schedules sync for new pending entries`() =
        runTest {
            // Given
            val pending = Task.mockList(2)
            entrySyncManager.start()

            // When
            pendingEntriesFlow.value = pending
            advanceUntilIdle()

            // Then
            verify(exactly = 1) { syncScheduler.scheduleEntrySync(pending[0].id) }
            verify(exactly = 1) { syncScheduler.scheduleEntrySync(pending[1].id) }
        }

    @Test
    fun `schedules sync for new deleted entries`() =
        runTest {
            // Given
            val deletedIds = listOf("deleted-1", "deleted-2")
            entrySyncManager.start()

            // When
            deletedEntryIdsFlow.value = deletedIds
            advanceUntilIdle()

            // Then
            verify(exactly = 1) { syncScheduler.scheduleDeletedEntrySync("deleted-1") }
            verify(exactly = 1) { syncScheduler.scheduleDeletedEntrySync("deleted-2") }
        }

    @Test
    fun `does not re-schedule sync when same pending list is emitted`() =
        runTest {
            // Given
            val pending = Task.mockList(1)
            entrySyncManager.start()

            // When
            pendingEntriesFlow.value = pending
            advanceUntilIdle()
            pendingEntriesFlow.value = pending // Emit same list again
            advanceUntilIdle()

            // Then
            verify(exactly = 1) { syncScheduler.scheduleEntrySync(pending[0].id) }
        }

    @Test
    fun `retries flow collection on transient error`() =
        runTest {
            // Given
            val transientError = IOException("DB connection failed")
            val errorFlow = flow<List<Entry>> { throw transientError }
            every { entryRepository.pendingEntries } returns errorFlow
            every { errorClassifier.classify(transientError) } returns ErrorType.TransientError(transientError)
            coEvery { retryPolicy.shouldRetry(any(), 0L) } returns true // Retry on first attempt
            coEvery { retryPolicy.shouldRetry(any(), 1L) } returns false // Stop on second

            // When
            entrySyncManager.start()
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { retryPolicy.shouldRetry(any(), 0L) }
            coVerify(exactly = 1) { retryPolicy.shouldRetry(any(), 1L) }
        }

    @Test
    fun `stops flow collection on permanent error`() =
        runTest {
            // Given
            val permanentError = IllegalStateException("Permanent DB error")
            val errorFlow = flow<List<Entry>> { throw permanentError }
            every { entryRepository.pendingEntries } returns errorFlow
            every { errorClassifier.classify(permanentError) } returns ErrorType.PermanentError(permanentError)
            coEvery { retryPolicy.shouldRetry(any(), any()) } returns false

            // When
            entrySyncManager.start()
            advanceUntilIdle()

            // Then (should only be called once for the first attempt)
            coVerify(exactly = 1) { retryPolicy.shouldRetry(any(), 0L) }
        }

    @Test
    fun `continues scheduling other entries after one fails`() =
        runTest {
            // Given
            val entries = Task.mockList(3)
            val failureId = entries[1].id
            val failureException = RuntimeException("Failed to schedule $failureId")

            every { syncScheduler.scheduleEntrySync(failureId) } throws failureException

            // When
            entrySyncManager.start()
            pendingEntriesFlow.value = entries
            advanceUntilIdle()

            // Then (verify all schedule calls were still attempted)
            verify(exactly = 1) { syncScheduler.scheduleEntrySync(entries[0].id) }
            verify(exactly = 1) { syncScheduler.scheduleEntrySync(entries[1].id) } // The one that failed
            verify(exactly = 1) { syncScheduler.scheduleEntrySync(entries[2].id) }
        }
}
