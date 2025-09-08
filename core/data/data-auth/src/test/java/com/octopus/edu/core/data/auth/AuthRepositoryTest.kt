package com.octopus.edu.core.data.auth

import com.google.firebase.auth.AuthResult
import com.octopus.edu.core.common.DispatcherProvider
import com.octopus.edu.core.data.auth.authAdapter.FirebaseAuthAdapter
import com.octopus.edu.core.domain.model.common.ResultOperation
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AuthRepositoryTest {
    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var mockFirebaseAuthAdapter: FirebaseAuthAdapter

    @MockK
    private lateinit var mockDispatcherProvider: DispatcherProvider

    @MockK
    private lateinit var mockAuthResult: AuthResult

    private lateinit var authRepository: AuthRepositoryImpl

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        every { mockDispatcherProvider.io } returns testDispatcher

        authRepository = AuthRepositoryImpl(mockFirebaseAuthAdapter, mockDispatcherProvider)
    }

    @Test
    fun `isUserLoggedIn delegates to adapter and returns its flow`() =
        runTest {
            val expectedFlow = MutableStateFlow(true)
            every { mockFirebaseAuthAdapter.isUserLoggedIn() } returns expectedFlow

            val actualFlow = authRepository.isUserLoggedIn
            Assert.assertEquals(expectedFlow, actualFlow)
            Assert.assertTrue(actualFlow.first())
            verify(exactly = 1) {
                @Suppress("UnusedFlow")
                mockFirebaseAuthAdapter.isUserLoggedIn()
            }
        }

    @Test
    fun `signIn when adapter signInWithCredentials successfully returns AuthResult then repo returns Success Unit`() =
        runTest(testDispatcher) {
            val testToken = "test_id_token"

            coEvery { mockFirebaseAuthAdapter.signInWithCredentials(testToken) } returns mockAuthResult

            val result = authRepository.signIn(testToken)

            Assert.assertTrue("Result should be Success", result is ResultOperation.Success)

            Assert.assertEquals(ResultOperation.Success(Unit), result)

            coVerify(exactly = 1) { mockFirebaseAuthAdapter.signInWithCredentials(testToken) }

            verify { mockDispatcherProvider.io }
        }

    @Test
    fun `signIn when adapter throws exception within safeCall returns Error`() =
        runTest(testDispatcher) {
            val testToken = "test_id_token"
            val exceptionFromAdapter = RuntimeException("Adapter Threw Exception During Sign In")

            coEvery { mockFirebaseAuthAdapter.signInWithCredentials(testToken) } throws exceptionFromAdapter

            val result = authRepository.signIn(testToken)

            Assert.assertTrue(result is ResultOperation.Error)
            Assert.assertEquals(exceptionFromAdapter, (result as ResultOperation.Error).throwable)
            Assert.assertEquals(exceptionFromAdapter.message, result.throwable.message)
            coVerify(exactly = 1) { mockFirebaseAuthAdapter.signInWithCredentials(testToken) }
            verify { mockDispatcherProvider.io }
        }

    @Test
    fun `signOut when adapter succeeds within safeCall returns Success`() =
        runTest(testDispatcher) {
            coEvery { mockFirebaseAuthAdapter.signOut() } returns Unit

            val result = authRepository.signOut()

            Assert.assertTrue(result is ResultOperation.Success)
            coVerify(exactly = 1) { mockFirebaseAuthAdapter.signOut() }
            verify { mockDispatcherProvider.io }
        }

    @Test
    fun `signOut when adapter throws exception within safeCall returns Error`() =
        runTest(testDispatcher) {
            val exceptionFromAdapter = RuntimeException("Adapter Threw Exception During Sign Out")

            coEvery { mockFirebaseAuthAdapter.signOut() } throws exceptionFromAdapter

            val result = authRepository.signOut()

            Assert.assertTrue(result is ResultOperation.Error)
            Assert.assertEquals(exceptionFromAdapter, (result as ResultOperation.Error).throwable)
            Assert.assertEquals(exceptionFromAdapter.message, result.throwable.message)
            coVerify(exactly = 1) { mockFirebaseAuthAdapter.signOut() }
            verify { mockDispatcherProvider.io }
        }
}
