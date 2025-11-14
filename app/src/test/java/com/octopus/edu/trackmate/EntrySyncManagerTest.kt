package com.octopus.edu.trackmate

import com.octopus.edu.core.common.RetryPolicy
import com.octopus.edu.core.domain.model.DoneEntry
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.model.common.ErrorType
import com.octopus.edu.core.domain.model.mockList
import com.octopus.edu.core.domain.repository.EntrySyncRepository
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
import java.time.Instant
import java.time.LocalDate

@ExperimentalCoroutinesApi
class EntrySyncManagerTest {
    private lateinit var syncRepository: EntrySyncRepository
    private lateinit var syncScheduler: EntrySyncScheduler
    private lateinit var errorClassifier: ErrorClassifier
    private lateinit var retryPolicy: RetryPolicy
    private lateinit var testDispatchers: TestDispatchers
    private lateinit var testScope: TestScope

    private lateinit var entrySyncManager: EntrySyncManager

    private lateinit var pendingEntriesFlow: MutableStateFlow<List<Entry>>
    private lateinit var deletedEntryIdsFlow: MutableStateFlow<List<String>>
    private lateinit var pendingDoneEntriesFlow: MutableStateFlow<List<DoneEntry>>

    @Before
    fun setUp() {
        syncRepository = mockk(relaxed = true)
        syncScheduler = mockk(relaxed = true)
        errorClassifier = mockk(relaxed = true)
        retryPolicy = mockk(relaxed = true)
        testDispatchers = TestDispatchers()
        testScope = TestScope(UnconfinedTestDispatcher())

        pendingEntriesFlow = MutableStateFlow(emptyList())
        deletedEntryIdsFlow = MutableStateFlow(emptyList())
        pendingDoneEntriesFlow = MutableStateFlow(emptyList())

        every { syncRepository.pendingEntries } returns pendingEntriesFlow
        every { syncRepository.deletedEntryIds } returns deletedEntryIdsFlow
        every { syncRepository.pendingEntriesMarkedAsDone } returns pendingDoneEntriesFlow

        entrySyncManager =
            EntrySyncManager(
                syncRepository,
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
    fun `schedules sync for new done entries`() =
        runTest {
            // Given
            val doneEntries =
                listOf(
                    DoneEntry("done-1", LocalDate.now(), Instant.now()),
                    DoneEntry("done-2", LocalDate.now().minusDays(1), Instant.now()),
                )
            entrySyncManager.start()

            // When
            pendingDoneEntriesFlow.value = doneEntries
            advanceUntilIdle()

            // Then
            verify(exactly = 1) {
                syncScheduler.scheduleEntryMarkedAsDoneSync(
                    doneEntries[0].id,
                    doneEntries[0].date,
                )
            }
            verify(exactly = 1) {
                syncScheduler.scheduleEntryMarkedAsDoneSync(
                    doneEntries[1].id,
                    doneEntries[1].date,
                )
            }
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
            every { syncRepository.pendingEntries } returns errorFlow
            every {
                errorClassifier.classify(transientError)
            } returns ErrorType.TransientError(transientError)
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
            every { syncRepository.pendingEntries } returns errorFlow
            every {
                errorClassifier.classify(permanentError)
            } returns ErrorType.PermanentError(permanentError)
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

    @Test
    fun `does not re-schedule sync for same deleted list`() =
        runTest {
            val deletedIds = listOf("del-1")
            entrySyncManager.start()

            deletedEntryIdsFlow.value = deletedIds
            advanceUntilIdle()
            deletedEntryIdsFlow.value = deletedIds // Emit same list
            advanceUntilIdle()

            verify(exactly = 1) { syncScheduler.scheduleDeletedEntrySync("del-1") }
        }

    @Test
    fun `retries deleted entries flow on transient error`() =
        runTest {
            val transientError = IOException("DB fail")
            val errorFlow = flow<List<String>> { throw transientError }
            every { syncRepository.deletedEntryIds } returns errorFlow
            every {
                errorClassifier.classify(transientError)
            } returns ErrorType.TransientError(transientError)
            coEvery { retryPolicy.shouldRetry(any(), 0L) } returns true
            coEvery { retryPolicy.shouldRetry(any(), 1L) } returns false

            entrySyncManager.start()
            advanceUntilIdle()

            coVerify(exactly = 1) { retryPolicy.shouldRetry(any(), 0L) }
            coVerify(exactly = 1) { retryPolicy.shouldRetry(any(), 1L) }
        }

    @Test
    fun `stops deleted entries flow on permanent error`() =
        runTest {
            val permanentError = IllegalStateException("Permanent DB error")
            val errorFlow = flow<List<String>> { throw permanentError }
            every { syncRepository.deletedEntryIds } returns errorFlow
            every {
                errorClassifier.classify(permanentError)
            } returns ErrorType.PermanentError(permanentError)

            entrySyncManager.start()
            advanceUntilIdle()

            coVerify(exactly = 1) { retryPolicy.shouldRetry(any(), 0L) }
        }

    @Test
    fun `continues scheduling other deleted entries after one fails`() =
        runTest {
            val deletedIds = listOf("del-1", "del-2", "del-3")
            val failureId = "del-2"
            val failureException = RuntimeException("Failed to schedule $failureId")
            every { syncScheduler.scheduleDeletedEntrySync(failureId) } throws failureException

            entrySyncManager.start()
            deletedEntryIdsFlow.value = deletedIds
            advanceUntilIdle()

            verify(exactly = 1) { syncScheduler.scheduleDeletedEntrySync("del-1") }
            verify(exactly = 1) { syncScheduler.scheduleDeletedEntrySync("del-2") } // The one that failed
            verify(exactly = 1) { syncScheduler.scheduleDeletedEntrySync("del-3") }
        }

    @Test
    fun `does not re-schedule sync for same done entry list`() =
        runTest {
            val doneEntries = listOf(DoneEntry("done-1", LocalDate.now(), Instant.now()))
            entrySyncManager.start()

            pendingDoneEntriesFlow.value = doneEntries
            advanceUntilIdle()
            pendingDoneEntriesFlow.value = doneEntries // Emit same list
            advanceUntilIdle()

            verify(exactly = 1) {
                syncScheduler.scheduleEntryMarkedAsDoneSync(doneEntries[0].id, doneEntries[0].date)
            }
        }

    @Test
    fun `retries done entries flow on transient error`() =
        runTest {
            val transientError = IOException("DB fail")
            val errorFlow = flow<List<DoneEntry>> { throw transientError }
            every { syncRepository.pendingEntriesMarkedAsDone } returns errorFlow
            every {
                errorClassifier.classify(transientError)
            } returns ErrorType.TransientError(transientError)
            coEvery { retryPolicy.shouldRetry(any(), 0L) } returns true
            coEvery { retryPolicy.shouldRetry(any(), 1L) } returns false

            entrySyncManager.start()
            advanceUntilIdle()

            coVerify(exactly = 1) { retryPolicy.shouldRetry(any(), 0L) }
            coVerify(exactly = 1) { retryPolicy.shouldRetry(any(), 1L) }
        }

    @Test
    fun `stops done entries flow on permanent error`() =
        runTest {
            val permanentError = IllegalStateException("Permanent DB error")
            val errorFlow = flow<List<DoneEntry>> { throw permanentError }
            every { syncRepository.pendingEntriesMarkedAsDone } returns errorFlow
            every {
                errorClassifier.classify(permanentError)
            } returns ErrorType.PermanentError(permanentError)

            entrySyncManager.start()
            advanceUntilIdle()

            coVerify(exactly = 1) { retryPolicy.shouldRetry(any(), 0L) }
        }

    @Test
    fun `continues scheduling other done entries after one fails`() =
        runTest {
            val doneEntries =
                listOf(
                    DoneEntry("done-1", LocalDate.now(), Instant.now()),
                    DoneEntry("done-2", LocalDate.now(), Instant.now()),
                    DoneEntry("done-3", LocalDate.now(), Instant.now()),
                )
            val failureId = "done-2"
            val failureDate = doneEntries[1].date
            val failureException = RuntimeException("Failed to schedule $failureId")
            every {
                syncScheduler.scheduleEntryMarkedAsDoneSync(failureId, failureDate)
            } throws failureException

            entrySyncManager.start()
            pendingDoneEntriesFlow.value = doneEntries
            advanceUntilIdle()

            verify(exactly = 1) {
                syncScheduler.scheduleEntryMarkedAsDoneSync(doneEntries[0].id, doneEntries[0].date)
            }
            verify(
                exactly = 1,
            ) { syncScheduler.scheduleEntryMarkedAsDoneSync(doneEntries[1].id, doneEntries[1].date) } // The one that failed
            verify(exactly = 1) {
                syncScheduler.scheduleEntryMarkedAsDoneSync(doneEntries[2].id, doneEntries[2].date)
            }
        }
}
