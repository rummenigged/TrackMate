package com.octopus.edu.core.domain

import com.octopus.edu.core.domain.model.DoneEntry
import com.octopus.edu.core.domain.model.SyncState
import com.octopus.edu.core.domain.model.common.ErrorType
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.model.common.SyncResult
import com.octopus.edu.core.domain.repository.EntrySyncRepository
import com.octopus.edu.core.domain.useCase.SyncEntryMarkedAsDoneUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import kotlin.test.assertIs
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class SyncEntryMarkedAsDoneUseCaseTest {
    private lateinit var syncRepository: EntrySyncRepository
    private lateinit var useCase: SyncEntryMarkedAsDoneUseCase

    private val testEntryId = "test-id-123"
    private val testEntryDate = LocalDate.of(2024, 7, 27)
    private val testDoneEntry = DoneEntry(testEntryId, testEntryDate, Instant.now())

    @Before
    fun setUp() {
        syncRepository = mockk(relaxed = true)
        useCase = SyncEntryMarkedAsDoneUseCase(syncRepository)
    }

    @Test
    fun `invoke returns Success when all operations succeed`() =
        runTest {
            // Given
            coEvery { syncRepository.getDoneEntry(testEntryId, testEntryDate) } returns ResultOperation.Success(testDoneEntry)
            coEvery { syncRepository.pushDoneEntry(testDoneEntry) } returns ResultOperation.Success(Unit)
            coEvery { syncRepository.updateDoneEntrySyncState(testEntryId, testEntryDate, SyncState.SYNCED) } returns
                ResultOperation.Success(Unit)

            // When
            val result = useCase(testEntryId, testEntryDate)

            // Then
            assertIs<SyncResult.Success>(result)
            coVerify(exactly = 1) { syncRepository.getDoneEntry(testEntryId, testEntryDate) }
            coVerify(exactly = 1) { syncRepository.pushDoneEntry(testDoneEntry) }
            coVerify(exactly = 1) { syncRepository.updateDoneEntrySyncState(testEntryId, testEntryDate, SyncState.SYNCED) }
        }

    @Test
    fun `invoke returns PermanentError when getDoneEntry fails permanently`() =
        runTest {
            // Given
            val getException = Exception("DB fetch failed")
            coEvery { syncRepository.getDoneEntry(testEntryId, testEntryDate) } returns
                ResultOperation.Error(getException, isRetriable = false)
            coEvery { syncRepository.updateDoneEntrySyncState(testEntryId, testEntryDate, SyncState.FAILED) } returns
                ResultOperation.Success(Unit)

            // When
            val result = useCase(testEntryId, testEntryDate)

            // Then
            assertIs<SyncResult.Error>(result)
            assertIs<ErrorType.PermanentError>(result.type)
            coVerify(exactly = 1) { syncRepository.updateDoneEntrySyncState(testEntryId, testEntryDate, SyncState.FAILED) }
            coVerify(exactly = 0) { syncRepository.pushDoneEntry(any()) }
        }

    @Test
    fun `invoke returns Error from update when getDoneEntry fails and update fails`() =
        runTest {
            // Given
            val getException = Exception("DB fetch failed")
            val updateException = IOException("Update failed")
            coEvery { syncRepository.getDoneEntry(testEntryId, testEntryDate) } returns
                ResultOperation.Error(getException, isRetriable = false)
            coEvery { syncRepository.updateDoneEntrySyncState(testEntryId, testEntryDate, SyncState.FAILED) } returns
                ResultOperation.Error(updateException, isRetriable = true)

            // When
            val result = useCase(testEntryId, testEntryDate)

            // Then
            assertIs<SyncResult.Error>(result)
            assertIs<ErrorType.TransientError>(result.type)
            assertTrue(result.type.cause is IOException)
        }

    @Test
    fun `invoke returns TransientError when pushDoneEntry fails retriably`() =
        runTest {
            // Given
            val pushException = IOException("Network unavailable")
            coEvery { syncRepository.getDoneEntry(testEntryId, testEntryDate) } returns ResultOperation.Success(testDoneEntry)
            coEvery { syncRepository.pushDoneEntry(testDoneEntry) } returns ResultOperation.Error(pushException, isRetriable = true)

            // When
            val result = useCase(testEntryId, testEntryDate)

            // Then
            assertIs<SyncResult.Error>(result)
            assertIs<ErrorType.TransientError>(result.type)
            coVerify(exactly = 0) { syncRepository.updateDoneEntrySyncState(any(), any(), any()) }
        }

    @Test
    fun `invoke returns PermanentError and updates state when push fails permanently`() =
        runTest {
            // Given
            val pushException = Exception("API validation failed")
            coEvery { syncRepository.getDoneEntry(testEntryId, testEntryDate) } returns ResultOperation.Success(testDoneEntry)
            coEvery { syncRepository.pushDoneEntry(testDoneEntry) } returns ResultOperation.Error(pushException, isRetriable = false)
            coEvery { syncRepository.updateDoneEntrySyncState(testEntryId, testEntryDate, SyncState.FAILED) } returns
                ResultOperation.Success(Unit)

            // When
            val result = useCase(testEntryId, testEntryDate)

            // Then
            assertIs<SyncResult.Error>(result)
            assertIs<ErrorType.PermanentError>(result.type)
            coVerify(exactly = 1) { syncRepository.updateDoneEntrySyncState(testEntryId, testEntryDate, SyncState.FAILED) }
        }

    @Test
    fun `invoke returns Error from update when push fails permanently and update fails`() =
        runTest {
            // Given
            val pushException = Exception("API validation failed")
            val updateException = IOException("Update failed")
            coEvery { syncRepository.getDoneEntry(testEntryId, testEntryDate) } returns ResultOperation.Success(testDoneEntry)
            coEvery { syncRepository.pushDoneEntry(testDoneEntry) } returns ResultOperation.Error(pushException, isRetriable = false)
            coEvery { syncRepository.updateDoneEntrySyncState(testEntryId, testEntryDate, SyncState.FAILED) } returns
                ResultOperation.Error(updateException, isRetriable = true)

            // When
            val result = useCase(testEntryId, testEntryDate)

            // Then
            assertIs<SyncResult.Error>(result)
            assertIs<ErrorType.TransientError>(result.type)
            assertTrue(result.type.cause is IOException)
        }

    @Test
    fun `invoke returns Success when push succeeds but update to SYNCED fails`() =
        runTest {
            // Given
            val updateException = IOException("DB write failed")
            coEvery { syncRepository.getDoneEntry(testEntryId, testEntryDate) } returns ResultOperation.Success(testDoneEntry)
            coEvery { syncRepository.pushDoneEntry(testDoneEntry) } returns ResultOperation.Success(Unit)
            coEvery { syncRepository.updateDoneEntrySyncState(testEntryId, testEntryDate, SyncState.SYNCED) } returns
                ResultOperation.Error(updateException, isRetriable = true)

            // When
            val result = useCase(testEntryId, testEntryDate)

            // Then
            assertIs<SyncResult.Success>(result)
            coVerify(exactly = 1) { syncRepository.updateDoneEntrySyncState(testEntryId, testEntryDate, SyncState.SYNCED) }
        }
}
