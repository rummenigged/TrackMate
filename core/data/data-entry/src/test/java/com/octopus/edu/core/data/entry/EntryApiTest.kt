package com.octopus.edu.core.data.entry

import android.os.Build
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObjects
import com.octopus.edu.core.common.Logger
import com.octopus.edu.core.data.entry.api.EntryApi
import com.octopus.edu.core.data.entry.api.EntryApiImpl
import com.octopus.edu.core.data.entry.api.EntryApiImpl.Companion.COLLECTION_DELETED_ENTRIES
import com.octopus.edu.core.data.entry.api.EntryApiImpl.Companion.COLLECTION_DONE_ENTRIES
import com.octopus.edu.core.data.entry.api.EntryApiImpl.Companion.COLLECTION_ENTRIES
import com.octopus.edu.core.data.entry.api.EntryApiImpl.Companion.COLLECTION_USERS
import com.octopus.edu.core.data.entry.api.dto.DeletedEntryDto
import com.octopus.edu.core.data.entry.api.dto.DoneEntryDto
import com.octopus.edu.core.data.entry.api.dto.EntryDto
import com.octopus.edu.core.data.entry.utils.toDTO
import com.octopus.edu.core.data.entry.utils.toDto
import com.octopus.edu.core.domain.model.DeletedEntry
import com.octopus.edu.core.domain.model.DoneEntry
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.model.mock
import com.octopus.edu.core.network.utils.NetworkResponse
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertIs

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class EntryApiTest {
    private val mockFirestore: FirebaseFirestore = mockk()
    private val mockUserPreferencesProvider: UserPreferencesProvider = mockk()

    private val mockEntriesCollection = mockk<CollectionReference>(relaxed = true)
    private val mockDeletedEntriesCollection = mockk<CollectionReference>(relaxed = true)
    private val mockDoneEntriesCollection = mockk<CollectionReference>(relaxed = true)
    private val mockUserDocument = mockk<DocumentReference>(relaxed = true)

    private lateinit var entryApi: EntryApi
    private val testUserId = "test-user-id"

    @Before
    fun setUp() {
        mockkObject(Logger)
        every { Logger.e(any(), null, any()) } just runs

        every { mockUserPreferencesProvider.userId } returns testUserId
        every { mockFirestore.collection(COLLECTION_USERS).document(testUserId) } returns mockUserDocument

        every { mockUserDocument.collection(COLLECTION_ENTRIES) } returns mockEntriesCollection
        every { mockUserDocument.collection(COLLECTION_DELETED_ENTRIES) } returns mockDeletedEntriesCollection
        every { mockUserDocument.collection(COLLECTION_DONE_ENTRIES) } returns mockDoneEntriesCollection

        entryApi = EntryApiImpl(mockFirestore, mockUserPreferencesProvider)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `saveEntry stores data in correct user collection`() =
        runTest {
            val entry = Task.mock("1")
            val entryDto = entry.toDTO()
            every {
                mockEntriesCollection.document(entryDto.id).set(entryDto)
            } returns Tasks.forResult(null)
            entryApi.saveEntry(entry)

            verify(exactly = 1) {
                mockEntriesCollection
                    .document(entryDto.id)
                    .set(entryDto)
            }
        }

    @Test
    fun `saveEntry throws FirebaseFirestoreException when permission denied`() =
        runTest {
            val entry = Task.mock("2")
            val expectedException =
                FirebaseFirestoreException(
                    "Permission Denied",
                    FirebaseFirestoreException.Code.PERMISSION_DENIED,
                )
            every { mockEntriesCollection.document(any()).set(any()) } returns
                Tasks.forException(expectedException)

            val response = entryApi.saveEntry(entry)

            assertIs<NetworkResponse.Error>(response)
            assertEquals(expectedException, response.exception)
            verify(exactly = 1) { Logger.e("Permission Denied", null, expectedException) }
        }

    @Test
    fun `saveEntry throws generic exception`() =
        runTest {
            val entry = Task.mock("2")
            val expectedException = IllegalStateException("Something went wrong")
            every { mockEntriesCollection.document(any()).set(any()) } returns
                Tasks.forException(expectedException)

            val response = entryApi.saveEntry(entry)

            assertIs<NetworkResponse.Error>(response)
            assertEquals(expectedException, response.exception)
            verify(exactly = 1) { Logger.e("Something went wrong", null, expectedException) }
        }

    @Test
    fun `fetchEntries returns Success on successful fetch from correct user`() =
        runTest {
            // Given
            val mockSnapshot = mockk<QuerySnapshot>()
            val expectedEntries = listOf(EntryDto(id = "1"), EntryDto(id = "2"))
            every { mockSnapshot.toObjects<EntryDto>() } returns expectedEntries
            every { mockEntriesCollection.get() } returns Tasks.forResult(mockSnapshot)

            // When
            val response = entryApi.fetchEntries()

            // Then
            assertIs<NetworkResponse.Success<List<EntryDto>>>(response)
            assertEquals(expectedEntries, response.data)
            verify { mockEntriesCollection.get() }
        }

    @Test
    fun `fetchEntries returns Error on fetch failure`() =
        runTest {
            // Given
            val exception = FirebaseFirestoreException("Unavailable", FirebaseFirestoreException.Code.UNAVAILABLE)
            every { mockEntriesCollection.get() } returns Tasks.forException(exception)

            // When
            val response = entryApi.fetchEntries()

            // Then
            assertIs<NetworkResponse.Error>(response)
            assertEquals(exception, response.exception)
            verify(exactly = 1) { Logger.e("Unavailable", null, exception) }
        }

    @Test
    fun `pushDeletedEntry successfully pushes entry to correct user collection`() =
        runTest {
            // Given
            val deletedEntry = DeletedEntry("deleted-id-1", Instant.now())
            val deletedEntryDto = deletedEntry.toDto()
            val idSlot = slot<String>()
            val dtoSlot = slot<DeletedEntryDto>()
            every {
                mockDeletedEntriesCollection.document(capture(idSlot)).set(capture(dtoSlot))
            } returns Tasks.forResult(null)

            // When
            entryApi.pushDeletedEntry(deletedEntry)

            // Then
            verify(exactly = 1) {
                mockDeletedEntriesCollection.document(any()).set(any())
            }

            assertEquals(deletedEntry.id, idSlot.captured)
            assertEquals(deletedEntryDto, dtoSlot.captured)
        }

    @Test
    fun `pushDeletedEntry throws exception on firestore failure`() =
        runTest {
            // Given
            val deletedEntry = DeletedEntry("deleted-id-2", Instant.now())
            val expectedException = FirebaseFirestoreException("Permission Denied", FirebaseFirestoreException.Code.PERMISSION_DENIED)
            every {
                mockDeletedEntriesCollection.document(any()).set(any())
            } returns Tasks.forException(expectedException)

            // When
            val response = entryApi.pushDeletedEntry(deletedEntry)

            // Then
            assertIs<NetworkResponse.Error>(response)
            assertEquals(expectedException, response.exception)
            verify(exactly = 1) { Logger.e("Permission Denied", null, expectedException) }
        }

    @Test
    fun `fetchDeletedEntry returns Success on successful fetch from correct user`() =
        runTest {
            // Given
            val now = Timestamp(Instant.now())
            val mockSnapshot = mockk<QuerySnapshot>()
            val expectedDeletedEntries =
                listOf(
                    DeletedEntryDto(id = "1", deletedAt = now),
                    DeletedEntryDto(id = "2", deletedAt = now),
                )
            every { mockSnapshot.toObjects<DeletedEntryDto>() } returns expectedDeletedEntries
            every { mockDeletedEntriesCollection.get() } returns Tasks.forResult(mockSnapshot)

            // When
            val response = entryApi.fetchDeletedEntries()

            // Then
            assertIs<NetworkResponse.Success<List<DeletedEntryDto>>>(response)
            assertEquals(expectedDeletedEntries, response.data)
            verify { mockDeletedEntriesCollection.get() }
        }

    @Test
    fun `fetchDeletedEntry returns Error on fetch failure`() =
        runTest {
            // Given
            val exception = FirebaseFirestoreException("Unavailable", FirebaseFirestoreException.Code.UNAVAILABLE)
            every { mockDeletedEntriesCollection.get() } returns Tasks.forException(exception)

            // When
            val response = entryApi.fetchDeletedEntries()

            // Then
            assertIs<NetworkResponse.Error>(response)
            assertEquals(exception, response.exception)
            verify(exactly = 1) { Logger.e("Unavailable", null, exception) }
        }

    @Test
    fun `pushDoneEntry successfully pushes entry`() =
        runTest {
            // Given
            val doneEntry = DoneEntry("done-id-1", LocalDate.now(), Instant.now())
            val doneEntryDto = doneEntry.toDto()
            val idSlot = slot<String>()
            val dtoSlot = slot<DoneEntryDto>()

            every { mockDoneEntriesCollection.document(capture(idSlot)).set(capture(dtoSlot)) } returns Tasks.forResult(null)

            // When
            val response = entryApi.pushDoneEntry(doneEntry)

            // Then
            assertIs<NetworkResponse.Success<Unit>>(response)
            verify(exactly = 1) { mockDoneEntriesCollection.document(any()).set(any()) }
            assertEquals(doneEntry.id, idSlot.captured)
            assertEquals(doneEntryDto.date, dtoSlot.captured.date)
            assertEquals(doneEntryDto.doneAt, dtoSlot.captured.doneAt)
        }

    @Test
    fun `pushDoneEntry returns Error on failure and logs`() =
        runTest {
            // Given
            val doneEntry = DoneEntry("done-id-2", LocalDate.now(), Instant.now())
            val expectedException = FirebaseFirestoreException("Permission Denied", FirebaseFirestoreException.Code.PERMISSION_DENIED)
            every { mockDoneEntriesCollection.document(any()).set(any()) } returns Tasks.forException(expectedException)

            // When
            val response = entryApi.pushDoneEntry(doneEntry)

            // Then
            assertIs<NetworkResponse.Error>(response)
            assertEquals(expectedException, response.exception)
            verify(exactly = 1) { Logger.e("Permission Denied", null, expectedException) }
        }

    @Test
    fun `fetchDoneEntries returns Success on successful fetch from correct user`() =
        runTest {
            // Given
            val now = Timestamp(Instant.now())
            val mockSnapshot = mockk<QuerySnapshot>()
            val expectedDoneEntries =
                listOf(
                    DoneEntryDto(id = "1", date = now, doneAt = now),
                    DoneEntryDto(id = "2", date = now, doneAt = now),
                )
            every { mockSnapshot.toObjects<DoneEntryDto>() } returns expectedDoneEntries
            every { mockDoneEntriesCollection.get() } returns Tasks.forResult(mockSnapshot)

            // When
            val response = entryApi.fetchDoneEntries()

            // Then
            assertIs<NetworkResponse.Success<List<DoneEntryDto>>>(response)
            assertEquals(expectedDoneEntries, response.data)
            verify { mockDoneEntriesCollection.get() }
        }

    @Test
    fun `fetchDoneEntries returns Error on fetch failure`() =
        runTest {
            // Given
            val exception = FirebaseFirestoreException("Unavailable", FirebaseFirestoreException.Code.UNAVAILABLE)
            every { mockDoneEntriesCollection.get() } returns Tasks.forException(exception)

            // When
            val response = entryApi.fetchDoneEntries()

            // Then
            assertIs<NetworkResponse.Error>(response)
            assertEquals(exception, response.exception)
            verify(exactly = 1) { Logger.e("Unavailable", null, exception) }
        }
}
