package com.octopus.edu.core.data.entry

import android.os.Build
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.octopus.edu.core.data.entry.api.EntryApi
import com.octopus.edu.core.data.entry.api.EntryApiImpl
import com.octopus.edu.core.data.entry.api.EntryApiImpl.Companion.COLLECTION_ENTRIES
import com.octopus.edu.core.data.entry.utils.toDTO
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.model.mock
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertFailsWith

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class EntryApiTest {
    private val mockCollection = mockk<CollectionReference>()

    private val mockFirestore = mockk<FirebaseFirestore>()

    private lateinit var entryApi: EntryApi

    @Before
    fun setUp() {
        coEvery { mockFirestore.collection(COLLECTION_ENTRIES) } returns mockCollection
        entryApi = EntryApiImpl(mockFirestore)
    }

    @Test
    fun `saveEntry stores data in firestore`() =
        runTest {
            val entry = Task.mock("1")
            every { mockCollection.document(entry.toDTO().id).set(entry.toDTO()) } returns Tasks.forResult(null)
            entryApi.saveEntry(entry)

            verify(exactly = 1) {
                mockCollection
                    .document(entry.toDTO().id)
                    .set(entry.toDTO())
            }
        }

    @Test
    fun `saveEntry throws FirebaseFirestoreException when permission denied`() =
        runTest {
            val entry = Task.mock("2")
            every { mockCollection.document(any()).set(any()) } returns
                Tasks.forException(
                    FirebaseFirestoreException(
                        "Permission Denied",
                        FirebaseFirestoreException.Code.PERMISSION_DENIED,
                    ),
                )

            assertFailsWith<FirebaseFirestoreException> {
                entryApi.saveEntry(entry)
            }
        }

    @Test
    fun `saveEntry throws generic exception`() =
        runTest {
            val entry = Task.mock("2")
            every { mockCollection.document(any()).set(any()) } returns
                Tasks.forException(
                    IllegalStateException("Unexpected Error"),
                )

            assertFailsWith<IllegalStateException> {
                entryApi.saveEntry(entry)
            }
        }
}
