package com.octopus.edu.core.domain

import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.SyncState
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.model.common.ErrorType
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.model.common.SyncResult
import com.octopus.edu.core.domain.model.mock
import com.octopus.edu.core.domain.repository.EntryRepository
import com.octopus.edu.core.domain.repository.EntrySyncRepository
import com.octopus.edu.core.domain.useCase.SyncPendingEntryUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertIs

@ExperimentalCoroutinesApi
class SyncPendingEntryUseCaseTest {
    private lateinit var entryRepository: EntryRepository
    private lateinit var syncRepository: EntrySyncRepository
    private lateinit var useCase: SyncPendingEntryUseCase

    private val testEntryId = "1"
    private val testEntry: Entry = Task.mock(testEntryId)

    @Before
    fun setUp() {
        entryRepository = mockk(relaxed = true)
        syncRepository = mockk(relaxed = true)
        useCase = SyncPendingEntryUseCase(entryRepository, syncRepository)
    }

    @Test
    fun `invoke returns Success when all operations succeed`() =
        runTest {
            // Given
            coEvery { entryRepository.getEntryById(testEntryId) } returns ResultOperation.Success(testEntry)
            coEvery { syncRepository.pushEntry(testEntry) } returns ResultOperation.Success(Unit)
            coEvery { syncRepository.updateEntrySyncState(testEntryId, SyncState.SYNCED) } returns ResultOperation.Success(Unit)

            // When
            val result = useCase(testEntryId)

            // Then
            assertIs<SyncResult.Success>(result)
            coVerify(exactly = 1) { entryRepository.getEntryById(testEntryId) }
            coVerify(exactly = 1) { syncRepository.pushEntry(testEntry) }
            coVerify(exactly = 1) { syncRepository.updateEntrySyncState(testEntryId, SyncState.SYNCED) }
        }

    @Test
    fun `invoke returns TransientError when getEntryById fails retriably`() =
        runTest {
            // Given
            val getException = IOException("DB unavailable")
            coEvery { entryRepository.getEntryById(testEntryId) } returns ResultOperation.Error(getException, isRetriable = true)

            // When
            val result = useCase(testEntryId)

            // Then
            assertIs<SyncResult.Error>(result)
            assertIs<ErrorType.TransientError>(result.type)
            coVerify(exactly = 0) { syncRepository.pushEntry(any()) }
            coVerify(exactly = 0) { syncRepository.updateEntrySyncState(any(), any()) }
        }

    @Test
    fun `invoke returns PermanentError and updates state when getEntryById fails permanently`() =
        runTest {
            // Given
            val getException = Exception("Entry not found")
            coEvery { entryRepository.getEntryById(testEntryId) } returns ResultOperation.Error(getException, isRetriable = false)
            coEvery { syncRepository.updateEntrySyncState(testEntryId, SyncState.FAILED) } returns ResultOperation.Success(Unit)

            // When
            val result = useCase(testEntryId)

            // Then
            assertIs<SyncResult.Error>(result)
            assertIs<ErrorType.PermanentError>(result.type)
            coVerify(exactly = 1) { syncRepository.updateEntrySyncState(testEntryId, SyncState.FAILED) }
            coVerify(exactly = 0) { syncRepository.pushEntry(any()) }
        }

    @Test
    fun `invoke returns Error from update when getEntryById fails and update also fails`() =
        runTest {
            // Given
            val getException = Exception("Permanent get error")
            val updateException = IOException("Transient update error")
            coEvery { entryRepository.getEntryById(testEntryId) } returns ResultOperation.Error(getException, isRetriable = false)
            coEvery { syncRepository.updateEntrySyncState(testEntryId, SyncState.FAILED) } returns
                ResultOperation.Error(updateException, isRetriable = true)

            // When
            val result = useCase(testEntryId)

            // Then
            assertIs<SyncResult.Error>(result)
            assertIs<ErrorType.TransientError>(result.type)
            assertEquals(updateException, result.type.cause)
        }

    @Test
    fun `invoke returns TransientError when pushEntry fails retriably`() =
        runTest {
            // Given
            val pushException = IOException("Network error")
            coEvery { entryRepository.getEntryById(testEntryId) } returns ResultOperation.Success(testEntry)
            coEvery { syncRepository.pushEntry(testEntry) } returns ResultOperation.Error(pushException, isRetriable = true)

            // When
            val result = useCase(testEntryId)

            // Then
            assertIs<SyncResult.Error>(result)
            assertIs<ErrorType.TransientError>(result.type)
            coVerify(exactly = 0) { syncRepository.updateEntrySyncState(any(), any()) }
        }

    @Test
    fun `invoke returns PermanentError and updates state when push fails permanently`() =
        runTest {
            // Given
            val pushException = Exception("Permanent push error")
            coEvery { entryRepository.getEntryById(testEntryId) } returns ResultOperation.Success(testEntry)
            coEvery { syncRepository.pushEntry(testEntry) } returns ResultOperation.Error(pushException, isRetriable = false)
            coEvery { syncRepository.updateEntrySyncState(testEntryId, SyncState.FAILED) } returns ResultOperation.Success(Unit)

            // When
            val result = useCase(testEntryId)

            // Then
            assertIs<SyncResult.Error>(result)
            assertIs<ErrorType.PermanentError>(result.type)
            coVerify(exactly = 1) { syncRepository.updateEntrySyncState(testEntryId, SyncState.FAILED) }
        }

    @Test
    fun `invoke returns Error from update when push fails permanently and update fails`() =
        runTest {
            // Given
            val pushException = Exception("Permanent push error")
            val updateException = IOException("Transient update error")
            coEvery { entryRepository.getEntryById(testEntryId) } returns ResultOperation.Success(testEntry)
            coEvery { syncRepository.pushEntry(testEntry) } returns ResultOperation.Error(pushException, isRetriable = false)
            coEvery { syncRepository.updateEntrySyncState(testEntryId, SyncState.FAILED) } returns
                ResultOperation.Error(updateException, isRetriable = true)

            // When
            val result = useCase(testEntryId)

            // Then
            assertIs<SyncResult.Error>(result)
            assertIs<ErrorType.TransientError>(result.type)
            assertEquals(updateException, result.type.cause)
        }

    @Test
    fun `invoke returns Success when push succeeds but update to SYNCED fails`() =
        runTest {
            // Given
            val updateException = IOException("DB error")
            coEvery { entryRepository.getEntryById(testEntryId) } returns ResultOperation.Success(testEntry)
            coEvery { syncRepository.pushEntry(testEntry) } returns ResultOperation.Success(Unit)
            coEvery { syncRepository.updateEntrySyncState(testEntryId, SyncState.SYNCED) } returns
                ResultOperation.Error(updateException, isRetriable = true)

            // When
            val result = useCase(testEntryId)

            // Then
            assertIs<SyncResult.Success>(result)
            coVerify(exactly = 1) { syncRepository.updateEntrySyncState(testEntryId, SyncState.SYNCED) }
        }
}
