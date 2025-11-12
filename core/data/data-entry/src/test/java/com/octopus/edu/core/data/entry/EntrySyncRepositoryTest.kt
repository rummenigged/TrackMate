package com.octopus.edu.core.data.entry

import com.octopus.edu.core.common.toEpochMilli
import com.octopus.edu.core.data.database.entity.DoneEntryEntity
import com.octopus.edu.core.data.entry.api.EntryApi
import com.octopus.edu.core.data.entry.store.EntryStore
import com.octopus.edu.core.data.entry.utils.EntryNotFoundException
import com.octopus.edu.core.data.entry.utils.toDomain
import com.octopus.edu.core.data.entry.utils.toEntity
import com.octopus.edu.core.domain.model.DoneEntry
import com.octopus.edu.core.domain.model.SyncState
import com.octopus.edu.core.domain.model.common.ErrorType
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.repository.EntrySyncRepository
import com.octopus.edu.core.domain.utils.ErrorClassifier
import com.octopus.edu.core.network.utils.NetworkResponse
import com.octopus.edu.core.testing.TestDispatchers
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.sql.SQLTimeoutException
import java.time.Instant
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class EntrySyncRepositoryTest {
    private lateinit var entryStore: EntryStore
    private lateinit var entryApi: EntryApi
    private lateinit var databaseErrorClassifier: ErrorClassifier
    private lateinit var networkErrorClassifier: ErrorClassifier
    private lateinit var repository: EntrySyncRepository

    private val testEntryId = "test-id"
    private val testDate = LocalDate.of(2024, 7, 26)
    private val testDateEpochMilli = testDate.toEpochMilli()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic("com.octopus.edu.core.data.entry.utils.EntityMappingExtensionsKt")

        entryStore = mockk(relaxed = true)
        entryApi = mockk(relaxed = true)
        databaseErrorClassifier = mockk()
        networkErrorClassifier = mockk()
        repository =
            EntrySyncRepositoryImpl(
                entryStore = entryStore,
                entryApi = entryApi,
                databaseErrorClassifier = databaseErrorClassifier,
                networkErrorClassifier = networkErrorClassifier,
                dispatcherProvider = TestDispatchers(),
            )
    }

    @After
    fun tearDown() {
        io.mockk.unmockkAll()
    }

    // region getDoneEntry tests
    @Test
    fun `getDoneEntry returns Success when store finds entry`() =
        runTest {
            // Given
            val doneEntryEntity = mockk<DoneEntryEntity>()
            val expectedDoneEntry = DoneEntry("id", testDate, Instant.now())
            coEvery { entryStore.getDoneEntry(testEntryId, testDateEpochMilli) } returns doneEntryEntity
            every { doneEntryEntity.toDomain() } returns expectedDoneEntry

            // When
            val result = repository.getDoneEntry(testEntryId, testDate)

            // Then
            assertIs<ResultOperation.Success<DoneEntry>>(result)
            assertEquals(expectedDoneEntry, result.data)
        }

    @Test
    fun `getDoneEntry returns Error when store returns null`() =
        runTest {
            // Given
            coEvery { entryStore.getDoneEntry(testEntryId, testDateEpochMilli) } returns null
            every {
                databaseErrorClassifier.classify(any<EntryNotFoundException>())
            } returns ErrorType.PermanentError(EntryNotFoundException(""))

            // When
            val result = repository.getDoneEntry(testEntryId, testDate)

            // Then
            assertIs<ResultOperation.Error>(result)
            assertIs<EntryNotFoundException>(result.throwable)
        }

    @Test
    fun `getDoneEntry returns Retriable Error on transient db exception`() =
        runTest {
            // Given
            val dbException = SQLTimeoutException("DB timeout")
            coEvery { entryStore.getDoneEntry(any(), any()) } throws dbException
            every { databaseErrorClassifier.classify(dbException) } returns ErrorType.TransientError(dbException)

            // When
            val result = repository.getDoneEntry(testEntryId, testDate)

            // Then
            assertIs<ResultOperation.Error>(result)
            assertTrue(result.isRetriable)
            assertEquals(dbException, result.throwable)
        }

    @Test
    fun `getDoneEntry returns Permanent Error on non-retriable db exception`() =
        runTest {
            // Given
            val dbException = RuntimeException("DB error")
            coEvery { entryStore.getDoneEntry(any(), any()) } throws dbException
            every { databaseErrorClassifier.classify(dbException) } returns ErrorType.PermanentError(dbException)

            // When
            val result = repository.getDoneEntry(testEntryId, testDate)

            // Then
            assertIs<ResultOperation.Error>(result)
            assertFalse(result.isRetriable)
            assertEquals(dbException, result.throwable)
        }
    // endregion

    // region pushDoneEntry tests
    @Test
    fun `pushDoneEntry returns Success when api succeeds`() =
        runTest {
            // Given
            val doneEntry = DoneEntry(testEntryId, testDate, Instant.now())
            coEvery { entryApi.pushDoneEntry(doneEntry) } returns NetworkResponse.Success(Unit)

            // When
            val result = repository.pushDoneEntry(doneEntry)

            // Then
            assertIs<ResultOperation.Success<Unit>>(result)
        }

    @Test
    fun `pushDoneEntry returns Retriable Error when api returns network error`() =
        runTest {
            // Given
            val doneEntry = DoneEntry(testEntryId, testDate, Instant.now())
            val networkException = IOException("Network error")
            coEvery { entryApi.pushDoneEntry(doneEntry) } returns NetworkResponse.Error(networkException)
            every { networkErrorClassifier.classify(networkException) } returns ErrorType.TransientError(networkException)

            // When
            val result = repository.pushDoneEntry(doneEntry)

            // Then
            assertIs<ResultOperation.Error>(result)
            assertTrue(result.isRetriable)
            assertEquals(networkException, result.throwable)
        }

    @Test
    fun `pushDoneEntry returns Permanent Error on non-retriable api error`() =
        runTest {
            // Given
            val doneEntry = DoneEntry(testEntryId, testDate, Instant.now())
            val apiException = Exception("Permanent API error")
            coEvery { entryApi.pushDoneEntry(doneEntry) } returns NetworkResponse.Error(apiException)
            every { networkErrorClassifier.classify(apiException) } returns ErrorType.PermanentError(apiException)

            // When
            val result = repository.pushDoneEntry(doneEntry)

            // Then
            assertIs<ResultOperation.Error>(result)
            assertFalse(result.isRetriable)
            assertEquals(apiException, result.throwable)
        }
    // endregion

    // region updateDoneEntrySyncState tests
    @Test
    fun `updateDoneEntrySyncState returns Success on successful update`() =
        runTest {
            // Given
            val syncState = SyncState.SYNCED
            val syncStateEntity = com.octopus.edu.core.data.database.entity.EntryEntity.SyncStateEntity.SYNCED
            coJustRun { entryStore.updateDoneEntrySyncState(testEntryId, testDateEpochMilli, syncStateEntity) }
            every { syncState.toEntity() } returns syncStateEntity

            // When
            val result = repository.updateDoneEntrySyncState(testEntryId, testDate, syncState)

            // Then
            assertIs<ResultOperation.Success<Unit>>(result)
            coVerify(exactly = 1) {
                entryStore.updateDoneEntrySyncState(testEntryId, testDateEpochMilli, syncStateEntity)
            }
        }

    @Test
    fun `updateDoneEntrySyncState returns Retriable Error on transient db exception`() =
        runTest {
            // Given
            val dbException = SQLTimeoutException("DB timeout")
            val syncState = SyncState.FAILED
            coEvery { entryStore.updateDoneEntrySyncState(any(), any(), any()) } throws dbException
            every { databaseErrorClassifier.classify(dbException) } returns ErrorType.TransientError(dbException)

            // When
            val result = repository.updateDoneEntrySyncState(testEntryId, testDate, syncState)

            // Then
            assertIs<ResultOperation.Error>(result)
            assertTrue(result.isRetriable)
            assertEquals(dbException, result.throwable)
        }

    @Test
    fun `updateDoneEntrySyncState returns Permanent Error on non-retriable db exception`() =
        runTest {
            // Given
            val dbException = RuntimeException("Permanent DB error")
            val syncState = SyncState.FAILED
            coEvery { entryStore.updateDoneEntrySyncState(any(), any(), any()) } throws dbException
            every { databaseErrorClassifier.classify(dbException) } returns ErrorType.PermanentError(dbException)

            // When
            val result = repository.updateDoneEntrySyncState(testEntryId, testDate, syncState)

            // Then
            assertIs<ResultOperation.Error>(result)
            assertFalse(result.isRetriable)
            assertEquals(dbException, result.throwable)
        }
    // endregion
}
