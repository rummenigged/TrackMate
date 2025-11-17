package com.octopus.edu.core.domain

import com.octopus.edu.core.domain.model.DeletedEntry
import com.octopus.edu.core.domain.model.SyncState
import com.octopus.edu.core.domain.model.common.ErrorType
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.model.common.SyncResult
import com.octopus.edu.core.domain.repository.EntrySyncRepository
import com.octopus.edu.core.domain.useCase.SyncDeletedEntryUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.Instant
import kotlin.test.assertIs
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class SyncDeletedEntryUseCaseTest {
    private lateinit var syncRepository: EntrySyncRepository
    private lateinit var useCase: SyncDeletedEntryUseCase

    private val testEntryId = "deleted-id-1"
    private val testDeletedEntry = DeletedEntry(testEntryId, Instant.now())

    @Before
    fun setUp() {
        syncRepository = mockk(relaxed = true)
        useCase = SyncDeletedEntryUseCase(syncRepository)
    }

    @Test
    fun `invoke returns Success when all operations succeed`() =
        runTest {
            // Given
            coEvery { syncRepository.getDeletedEntry(testEntryId) } returns ResultOperation.Success(testDeletedEntry)
            coEvery { syncRepository.pushDeletedEntry(testDeletedEntry) } returns ResultOperation.Success(Unit)
            coEvery { syncRepository.updateDeletedEntrySyncState(testEntryId, SyncState.SYNCED) } returns ResultOperation.Success(Unit)

            // When
            val result = useCase(testEntryId)

            // Then
            assertIs<SyncResult.Success>(result)
            coVerify(exactly = 1) { syncRepository.getDeletedEntry(testEntryId) }
            coVerify(exactly = 1) { syncRepository.pushDeletedEntry(testDeletedEntry) }
            coVerify(exactly = 1) { syncRepository.updateDeletedEntrySyncState(testEntryId, SyncState.SYNCED) }
        }

    @Test
    fun `invoke returns TransientError when getDeletedEntry fails retriably`() =
        runTest {
            // Given
            val getException = IOException("DB unavailable")
            coEvery { syncRepository.getDeletedEntry(testEntryId) } returns ResultOperation.Error(getException, isRetriable = true)

            // When
            val result = useCase(testEntryId)

            // Then
            assertIs<SyncResult.Error>(result)
            assertIs<ErrorType.TransientError>(result.type)
            coVerify(exactly = 0) { syncRepository.pushDeletedEntry(any()) }
            coVerify(exactly = 0) { syncRepository.updateDeletedEntrySyncState(any(), any()) }
        }

    @Test
    fun `invoke returns PermanentError when getDeletedEntry fails permanently`() =
        runTest {
            // Given
            val getException = Exception("Entry not found")
            coEvery { syncRepository.getDeletedEntry(testEntryId) } returns ResultOperation.Error(getException, isRetriable = false)
            coEvery { syncRepository.updateDeletedEntrySyncState(testEntryId, SyncState.FAILED) } returns ResultOperation.Success(Unit)

            // When
            val result = useCase(testEntryId)

            // Then
            assertIs<SyncResult.Error>(result)
            assertIs<ErrorType.PermanentError>(result.type)
            coVerify(exactly = 1) { syncRepository.updateDeletedEntrySyncState(testEntryId, SyncState.FAILED) }
        }

    @Test
    fun `invoke returns Error from update when getDeletedEntry fails and update also fails`() =
        runTest {
            // Given
            val getException = Exception("Permanent get error")
            val updateException = IOException("Transient update error")
            coEvery { syncRepository.getDeletedEntry(testEntryId) } returns ResultOperation.Error(getException, isRetriable = false)
            coEvery { syncRepository.updateDeletedEntrySyncState(testEntryId, SyncState.FAILED) } returns
                ResultOperation.Error(updateException, isRetriable = true)

            // When
            val result = useCase(testEntryId)

            // Then
            assertIs<SyncResult.Error>(result)
            assertIs<ErrorType.TransientError>(result.type)
            assertTrue(result.type.cause is IOException)
        }

    @Test
    fun `invoke returns TransientError when pushDeletedEntry fails retriably`() =
        runTest {
            // Given
            val pushException = IOException("Network error")
            coEvery { syncRepository.getDeletedEntry(testEntryId) } returns ResultOperation.Success(testDeletedEntry)
            coEvery { syncRepository.pushDeletedEntry(testDeletedEntry) } returns ResultOperation.Error(pushException, isRetriable = true)

            // When
            val result = useCase(testEntryId)

            // Then
            assertIs<SyncResult.Error>(result)
            assertIs<ErrorType.TransientError>(result.type)
            coVerify(exactly = 0) { syncRepository.updateDeletedEntrySyncState(any(), any()) }
        }

    @Test
    fun `invoke returns PermanentError and updates state when push fails permanently`() =
        runTest {
            // Given
            val pushException = Exception("Permanent push error")
            coEvery { syncRepository.getDeletedEntry(testEntryId) } returns ResultOperation.Success(testDeletedEntry)
            coEvery { syncRepository.pushDeletedEntry(testDeletedEntry) } returns ResultOperation.Error(pushException, isRetriable = false)
            coEvery { syncRepository.updateDeletedEntrySyncState(testEntryId, SyncState.FAILED) } returns ResultOperation.Success(Unit)

            // When
            val result = useCase(testEntryId)

            // Then
            assertIs<SyncResult.Error>(result)
            assertIs<ErrorType.PermanentError>(result.type)
            coVerify(exactly = 1) { syncRepository.updateDeletedEntrySyncState(testEntryId, SyncState.FAILED) }
        }

    @Test
    fun `invoke returns Error from update when push fails permanently and update fails`() =
        runTest {
            // Given
            val pushException = Exception("Permanent push error")
            val updateException = IOException("Transient update error")
            coEvery { syncRepository.getDeletedEntry(testEntryId) } returns ResultOperation.Success(testDeletedEntry)
            coEvery { syncRepository.pushDeletedEntry(testDeletedEntry) } returns ResultOperation.Error(pushException, isRetriable = false)
            coEvery { syncRepository.updateDeletedEntrySyncState(testEntryId, SyncState.FAILED) } returns
                ResultOperation.Error(updateException, isRetriable = true)

            // When
            val result = useCase(testEntryId)

            // Then
            assertIs<SyncResult.Error>(result)
            assertIs<ErrorType.TransientError>(result.type)
            assertTrue(result.type.cause is IOException)
        }

    @Test
    fun `invoke returns Success when push succeeds but update to SYNCED fails`() =
        runTest {
            // Given
            val updateException = IOException("DB error")
            coEvery { syncRepository.getDeletedEntry(testEntryId) } returns ResultOperation.Success(testDeletedEntry)
            coEvery { syncRepository.pushDeletedEntry(testDeletedEntry) } returns ResultOperation.Success(Unit)
            coEvery { syncRepository.updateDeletedEntrySyncState(testEntryId, SyncState.SYNCED) } returns
                ResultOperation.Error(updateException, isRetriable = true)

            // When
            val result = useCase(testEntryId)

            // Then
            assertIs<SyncResult.Success>(result)
            coVerify(exactly = 1) { syncRepository.updateDeletedEntrySyncState(testEntryId, SyncState.SYNCED) }
        }
}
