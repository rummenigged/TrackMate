package com.octopus.edu.core.data.entry.entryRepository

import app.cash.turbine.test
import com.octopus.edu.core.common.toEpochMilli
import com.octopus.edu.core.data.database.entity.DeletedEntryEntity
import com.octopus.edu.core.data.database.entity.DoneEntryEntity
import com.octopus.edu.core.data.database.entity.EntryEntity
import com.octopus.edu.core.data.entry.EntrySyncRepositoryImpl
import com.octopus.edu.core.data.entry.api.EntryApi
import com.octopus.edu.core.data.entry.store.EntryStore
import com.octopus.edu.core.data.entry.utils.EntryNotFoundException
import com.octopus.edu.core.data.entry.utils.toDomain
import com.octopus.edu.core.data.entry.utils.toEntity
import com.octopus.edu.core.domain.model.DeletedEntry
import com.octopus.edu.core.domain.model.DoneEntry
import com.octopus.edu.core.domain.model.SyncState
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.model.common.ErrorType
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.model.mock
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
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.sql.SQLTimeoutException
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap
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
    private val dbSemaphore = Semaphore(Int.MAX_VALUE)
    private val entryLocks = ConcurrentHashMap<String, Mutex>()

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
                dbSemaphore = dbSemaphore,
                entryLocks = entryLocks,
                databaseErrorClassifier = databaseErrorClassifier,
                networkErrorClassifier = networkErrorClassifier,
                dispatcherProvider = TestDispatchers(),
            )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // region Flow tests
    @Test
    fun `pendingEntries flow emits correctly mapped domain entries`() =
        runTest {
            val entities = listOf(Task.mock("1").toEntity(), Task.mock("2").toEntity())
            every { entryStore.streamPendingEntries() } returns flowOf(entities)

            repository.pendingEntries.test {
                val items = awaitItem()
                assertEquals(2, items.size)
                assertEquals("1", items[0].id)
                awaitComplete()
            }
        }

    @Test
    fun `deletedEntryIds flow emits correctly mapped string ids`() =
        runTest {
            val deletedEntities = listOf(DeletedEntryEntity("del1", 1L), DeletedEntryEntity("del2", 2L))
            every { entryStore.streamPendingDeletedEntries() } returns flowOf(deletedEntities)

            repository.deletedEntryIds.test {
                val ids = awaitItem()
                assertEquals(listOf("del1", "del2"), ids)
                awaitComplete()
            }
        }

    @Test
    fun `pendingEntriesMarkedAsDone flow emits correctly mapped domain entries`() =
        runTest {
            val doneEntities =
                listOf(
                    DoneEntryEntity("done1", 1L, 2L, true, EntryEntity.SyncStateEntity.PENDING),
                    DoneEntryEntity("done2", 3L, 4L, true, EntryEntity.SyncStateEntity.PENDING),
                )
            every { entryStore.streamPendingDoneEntries() } returns flowOf(doneEntities)

            repository.pendingEntriesMarkedAsDone.test {
                val items = awaitItem()
                assertEquals(2, items.size)
                assertEquals("done1", items[0].id)
                awaitComplete()
            }
        }
    // endregion

    // region pushEntry tests
    @Test
    fun `pushEntry returns Success on API success`() =
        runTest {
            val entry = Task.mock("1")
            coEvery { entryApi.saveEntry(entry) } returns NetworkResponse.Success(Unit)
            val result = repository.pushEntry(entry)
            assertIs<ResultOperation.Success<Unit>>(result)
        }

    @Test
    fun `pushEntry returns Retriable Error on transient network error`() =
        runTest {
            val entry = Task.mock("1")
            val exception = IOException()
            coEvery { entryApi.saveEntry(entry) } returns NetworkResponse.Error(exception)
            every { networkErrorClassifier.classify(exception) } returns ErrorType.TransientError(exception)
            val result = repository.pushEntry(entry)
            assertIs<ResultOperation.Error>(result)
            assertTrue(result.isRetriable)
        }

    // endregion

    // region updateEntrySyncState tests
    @Test
    fun `updateEntrySyncState returns Success on store success`() =
        runTest {
            val syncState = SyncState.SYNCED
            coJustRun { entryStore.updateEntrySyncState(any(), syncState.toEntity()) }
            val result = repository.updateEntrySyncState(testEntryId, syncState)
            assertIs<ResultOperation.Success<Unit>>(result)
            coVerify { entryStore.updateEntrySyncState(testEntryId, syncState.toEntity()) }
        }

    @Test
    fun `updateEntrySyncState returns Permanent Error on non-retriable db error`() =
        runTest {
            val exception = RuntimeException()
            coEvery { entryStore.updateEntrySyncState(any(), any()) } throws exception
            every { databaseErrorClassifier.classify(exception) } returns ErrorType.PermanentError(exception)
            val result = repository.updateEntrySyncState(testEntryId, SyncState.SYNCED)
            assertIs<ResultOperation.Error>(result)
            assertFalse(result.isRetriable)
        }
    // endregion

    // region getPendingEntries tests
    @Test
    fun `getPendingEntries returns Success with entries`() =
        runTest {
            val entities = listOf(Task.mock("1").toEntity())
            coEvery { entryStore.getPendingEntries() } returns entities
            val result = repository.getPendingEntries()
            assertIs<ResultOperation.Success<List<com.octopus.edu.core.domain.model.Entry>>>(result)
            assertEquals(1, result.data.size)
            assertEquals("1", result.data[0].id)
        }

    @Test
    fun `getPendingEntries returns Success with empty list on error`() =
        runTest {
            coEvery { entryStore.getPendingEntries() } throws RuntimeException()
            val result = repository.getPendingEntries()
            assertIs<ResultOperation.Success<List<com.octopus.edu.core.domain.model.Entry>>>(result)
            assertTrue(result.data.isEmpty())
        }
    //endregion

    // region getDeletedEntry tests
    @Test
    fun `getDeletedEntry returns Success when store finds entry`() =
        runTest {
            val deletedEntity = DeletedEntryEntity("del1", 1L)
            coEvery { entryStore.getDeletedEntry("del1") } returns deletedEntity
            val result = repository.getDeletedEntry("del1")
            assertIs<ResultOperation.Success<DeletedEntry>>(result)
            assertEquals("del1", result.data.id)
        }

    @Test
    fun `getDeletedEntry returns Permanent Error when not found`() =
        runTest {
            coEvery { entryStore.getDeletedEntry(any()) } returns null
            every { databaseErrorClassifier.classify(any<EntryNotFoundException>()) } returns
                ErrorType.PermanentError(EntryNotFoundException(""))
            val result = repository.getDeletedEntry("del1")
            assertIs<ResultOperation.Error>(result)
            assertIs<EntryNotFoundException>(result.throwable)
            assertFalse(result.isRetriable)
        }
    // endregion

    // region pushDeletedEntry tests
    @Test
    fun `pushDeletedEntry returns Success on API success`() =
        runTest {
            val deletedEntry = DeletedEntry("del1", Instant.now())
            coEvery { entryApi.pushDeletedEntry(deletedEntry) } returns NetworkResponse.Success(Unit)
            val result = repository.pushDeletedEntry(deletedEntry)
            assertIs<ResultOperation.Success<Unit>>(result)
        }

    @Test
    fun `pushDeletedEntry returns Permanent Error on non-retriable network error`() =
        runTest {
            val deletedEntry = DeletedEntry("del1", Instant.now())
            val exception = Exception()
            coEvery { entryApi.pushDeletedEntry(deletedEntry) } returns NetworkResponse.Error(exception)
            every { networkErrorClassifier.classify(exception) } returns ErrorType.PermanentError(exception)
            val result = repository.pushDeletedEntry(deletedEntry)
            assertIs<ResultOperation.Error>(result)
            assertFalse(result.isRetriable)
        }
    // endregion

    // region updateDeletedEntrySyncState tests
    @Test
    fun `updateDeletedEntrySyncState returns Success on store success`() =
        runTest {
            coJustRun { entryStore.updateDeletedEntrySyncState(any(), SyncState.SYNCED.toEntity()) }
            val result = repository.updateDeletedEntrySyncState(testEntryId, SyncState.SYNCED)
            assertIs<ResultOperation.Success<Unit>>(result)
            coVerify { entryStore.updateDeletedEntrySyncState(testEntryId, SyncState.SYNCED.toEntity()) }
        }

    @Test
    fun `updateDeletedEntrySyncState returns Retriable Error on transient db error`() =
        runTest {
            val exception = SQLTimeoutException()
            coEvery { entryStore.updateDeletedEntrySyncState(any(), any()) } throws exception
            every { databaseErrorClassifier.classify(exception) } returns ErrorType.TransientError(exception)
            val result = repository.updateDeletedEntrySyncState(testEntryId, SyncState.SYNCED)
            assertIs<ResultOperation.Error>(result)
            assertTrue(result.isRetriable)
        }
    // endregion

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
            every { databaseErrorClassifier.classify(any<EntryNotFoundException>()) } returns
                ErrorType.PermanentError(EntryNotFoundException(""))

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
            val syncStateEntity = EntryEntity.SyncStateEntity.SYNCED
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
