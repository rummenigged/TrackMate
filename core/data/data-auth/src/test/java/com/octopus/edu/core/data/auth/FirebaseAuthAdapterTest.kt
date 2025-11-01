package com.octopus.edu.core.data.auth

import app.cash.turbine.test
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.octopus.edu.core.data.auth.authAdapter.FirebaseAuthAdapter
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class FirebaseAuthAdapterTest {
    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var mockFirebaseAuth: FirebaseAuth

    @MockK
    private lateinit var mockFirebaseUser: FirebaseUser

    private lateinit var firebaseAuthAdapter: FirebaseAuthAdapter

    private var authStateListenerSlot = slot<FirebaseAuth.AuthStateListener>()

    @Before
    fun setUp() {
        mockkStatic(GoogleAuthProvider::class)

        every { mockFirebaseAuth.addAuthStateListener(capture(authStateListenerSlot)) } just runs
        every { mockFirebaseAuth.removeAuthStateListener(any()) } just runs

        firebaseAuthAdapter = FirebaseAuthAdapter(mockFirebaseAuth)
    }

    @Test
    fun `isUserLoggedIn emits false initially when currentUser is null`() =
        runTest {
            every { mockFirebaseAuth.currentUser } returns null

            firebaseAuthAdapter.isUserLoggedIn().test {
                assertFalse(awaitItem())
                cancelAndConsumeRemainingEvents()
            }
            verify { mockFirebaseAuth.addAuthStateListener(any()) }
        }

    @Test
    fun `isUserLoggedIn emits true initially when currentUser is not null`() =
        runTest {
            every { mockFirebaseAuth.currentUser } returns mockFirebaseUser

            firebaseAuthAdapter.isUserLoggedIn().test {
                assertTrue(awaitItem())
                cancelAndConsumeRemainingEvents()
            }
            verify { mockFirebaseAuth.addAuthStateListener(any()) }
        }

    @Test
    fun `isUserLoggedIn emits updated state when AuthStateListener fires`() =
        runTest {
            every { mockFirebaseAuth.currentUser } returns null

            firebaseAuthAdapter.isUserLoggedIn().test {
                assertFalse(awaitItem())

                every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
                authStateListenerSlot.captured.onAuthStateChanged(mockFirebaseAuth)
                assertTrue(awaitItem())

                every { mockFirebaseAuth.currentUser } returns null
                authStateListenerSlot.captured.onAuthStateChanged(mockFirebaseAuth)
                assertFalse(awaitItem())

                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `isUserLoggedIn removes listener on flow collection cancellation`() =
        runTest {
            every { mockFirebaseAuth.currentUser } returns null

            firebaseAuthAdapter.isUserLoggedIn().test {
                assertFalse(awaitItem())
                // No more items, flow collection will be cancelled by 'test' scope ending
            }

            verify { mockFirebaseAuth.removeAuthStateListener(authStateListenerSlot.captured) }
        }

    @Test
    fun `signOut success completes normally`() =
        runTest {
            every { mockFirebaseAuth.signOut() } just runs

            try {
                firebaseAuthAdapter.signOut()
            } catch (e: Exception) {
                fail("signOut should not have thrown an exception on success: ${e.message}")
            }

            verify(exactly = 1) { mockFirebaseAuth.signOut() }
        }

    @Test
    fun `signOut failure throws exception`() =
        runTest {
            val expectedException = IOException("Firebase SignOut Network Error")
            every { mockFirebaseAuth.signOut() } throws expectedException

            val actualException =
                assertThrows(IOException::class.java) {
                    firebaseAuthAdapter.signOut()
                }
            Assert.assertEquals(expectedException.message, actualException.message)
        }
}
