package com.octopus.edu.core.data.entry

import com.octopus.edu.core.common.AppClock
import com.octopus.edu.core.common.TransactionRunner
import com.octopus.edu.core.data.database.dao.EntryDao
import com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity
import com.octopus.edu.core.data.entry.store.EntryStore
import com.octopus.edu.core.data.entry.store.decorator.TrackingEntryStoreDecorator
import io.mockk.Ordering
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExperimentalCoroutinesApi
class TrackingEntryStoreDecoratorTest {
    private lateinit var decoratedStore: EntryStore
    private lateinit var entryDao: EntryDao
    private lateinit var transactionRunner: TransactionRunner
    private lateinit var appClock: AppClock
    private lateinit var trackingDecorator: TrackingEntryStoreDecorator

    private val testEntryId = "test-id"
    private val testTimestamp = 123456789L

    @Before
    fun setUp() {
        decoratedStore = mockk()
        entryDao = mockk()
        transactionRunner = mockk()
        appClock = mockk()

        trackingDecorator =
            TrackingEntryStoreDecorator(
                entryStore = decoratedStore,
                entryDao = entryDao,
                roomTransactionRunner = transactionRunner,
                appClock = appClock,
            )

        // Mock the transaction runner to execute the block passed to it
        coEvery { transactionRunner.run<Unit>(any()) } coAnswers {
            val block = it.invocation.args[0] as suspend () -> Unit
            block.invoke()
        }

        every { appClock.nowEpocMillis() } returns testTimestamp
    }

    @Test
    fun `markEntryAsDone should run in transaction and update sync metadata`() =
        runTest {
            // Given
            coJustRun { decoratedStore.markEntryAsDone(testEntryId) }
            coJustRun { entryDao.updateSyncMetadata(any(), any(), any()) }

            // When
            trackingDecorator.markEntryAsDone(testEntryId)

            // Then
            coVerify(exactly = 1) { transactionRunner.run(any()) }
            coVerify(ordering = Ordering.SEQUENCE) {
                decoratedStore.markEntryAsDone(testEntryId)
                entryDao.updateSyncMetadata(
                    testEntryId,
                    SyncStateEntity.PENDING,
                    testTimestamp,
                )
            }
        }

    @Test
    fun `markEntryAsDone should propagate exception when decorated store fails`() =
        runTest {
            // Given
            val exception = RuntimeException("Decorated store failed")
            coEvery { decoratedStore.markEntryAsDone(testEntryId) } throws exception

            // When
            val thrownException =
                assertFailsWith<RuntimeException> {
                    trackingDecorator.markEntryAsDone(testEntryId)
                }

            // Then
            assertEquals(exception, thrownException)

            coVerify(exactly = 1) { transactionRunner.run(any()) }
            coVerify(exactly = 1) { decoratedStore.markEntryAsDone(testEntryId) }
            coVerify(exactly = 0) { entryDao.updateSyncMetadata(any(), any(), any()) }
        }

    @Test
    fun `markEntryAsDone should propagate exception when dao update fails`() =
        runTest {
            // Given
            val exception = RuntimeException("DAO update failed")
            coJustRun { decoratedStore.markEntryAsDone(testEntryId) }
            coEvery { entryDao.updateSyncMetadata(any(), any(), any()) } throws exception

            // When
            val thrownException =
                assertFailsWith<RuntimeException> {
                    trackingDecorator.markEntryAsDone(testEntryId)
                }

            assertEquals(exception, thrownException)

            // Then
            coVerify(exactly = 1) { transactionRunner.run(any()) }
            coVerify(exactly = 1) { decoratedStore.markEntryAsDone(testEntryId) }
            coVerify(
                exactly = 1,
            ) { entryDao.updateSyncMetadata(testEntryId, SyncStateEntity.PENDING, testTimestamp) }
        }
}
