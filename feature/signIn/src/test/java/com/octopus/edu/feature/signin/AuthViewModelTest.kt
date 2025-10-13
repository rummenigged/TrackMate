package com.octopus.edu.feature.signin

import android.content.Context
import app.cash.turbine.test
import com.octopus.edu.core.common.Logger
import com.octopus.edu.core.domain.credentialManager.ICredentialService
import com.octopus.edu.core.domain.credentialManager.SignInInitiationResult
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.repository.AuthRepository
import com.octopus.edu.core.ui.common.AuthErrorMapper
import com.octopus.edu.feature.signin.AuthUiContract.UiEvent
import com.octopus.edu.feature.signin.AuthUiContract.UiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.bytebuddy.matcher.ElementMatchers.any
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AuthViewModelTest {
    @get:Rule
    val mockkRule = MockKRule(this)

    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)

    @MockK
    private lateinit var mockCredentialService: ICredentialService

    @MockK
    private lateinit var mockAuthRepository: AuthRepository

    @MockK
    private lateinit var context: Context

    private lateinit var viewModel: AuthViewModel
    private lateinit var isUserLoggedInFlow: MutableStateFlow<Boolean>

    private var clearCredentialsOnErrorSlot = slot<(Throwable) -> Unit>()

    @Before
    fun setUp() {
        mockkObject(AuthErrorMapper)
        every { AuthErrorMapper.toUserMessage(any()) } answers { firstArg<String?>() ?: "Default error" }

        Dispatchers.setMain(testDispatcher)

        mockkObject(Logger)
        every { Logger.e(any(), any(), any()) } just runs

        mockkObject(AuthErrorMapper)
        every { AuthErrorMapper.toUserMessage(any()) } answers { firstArg<String?>() ?: "Default error" }

        isUserLoggedInFlow = MutableStateFlow(false)
        every { mockAuthRepository.isUserLoggedIn } returns isUserLoggedInFlow

        coEvery { mockCredentialService.clearUserCredentials(onError = capture(clearCredentialsOnErrorSlot)) } coAnswers {
            // In a success scenario, the onError lambda is not invoked by the service.
            // If the test needs to simulate an error from clearUserCredentials, this mock will be overridden.
        }

        viewModel = AuthViewModel(mockCredentialService, mockAuthRepository)
        testScheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkObject(Logger, AuthErrorMapper)
    }

    @Test
    fun `initial state is Unknown then Unauthenticated via checkAuthState`() =
        runTest(testDispatcher.scheduler) {
            assertEquals(UiState.Unauthenticated, viewModel.uiState.value)
        }

    @Test
    fun `checkAuthState updates uiState to Authenticated when isUserLoggedIn emits true`() =
        runTest(testDispatcher.scheduler) {
            isUserLoggedInFlow.value = true
            testScheduler.advanceUntilIdle()
            assertEquals(UiState.Authenticated, viewModel.uiState.value)
        }

    @Test
    fun `checkAuthState updates uiState to Unauthenticated when isUserLoggedIn emits false`() =
        runTest(testDispatcher.scheduler) {
            isUserLoggedInFlow.value = true
            testScheduler.advanceUntilIdle()
            assertEquals(UiState.Authenticated, viewModel.uiState.value)

            isUserLoggedInFlow.value = false
            testScheduler.advanceUntilIdle()
            assertEquals(UiState.Unauthenticated, viewModel.uiState.value)
        }

    @Test
    fun `OnGoogleSignIn when initiateGoogleSignIn is Authenticated and signIn success`() =
        runTest(testDispatcher.scheduler) {
            val testToken = "google_auth_token"
            val initiationResult = SignInInitiationResult.Authenticated(testToken)
            coEvery { mockCredentialService.initiateGoogleSignIn(any()) } returns initiationResult
            coEvery { mockAuthRepository.signIn(testToken) } returns ResultOperation.Success(Unit)

            viewModel.processEvent(UiEvent.OnGoogleSignIn(context))
            testScheduler.advanceUntilIdle()

            assertEquals(UiState.Authenticating, viewModel.uiState.value)

            isUserLoggedInFlow.value = true
            testScheduler.advanceUntilIdle()
            assertEquals(UiState.Authenticated, viewModel.uiState.value)

            coVerify(exactly = 1) { mockCredentialService.initiateGoogleSignIn(context) }
            coVerify(exactly = 1) { mockAuthRepository.signIn(testToken) }
        }

    @Test
    fun `OnGoogleSignIn when initiateGoogleSignIn is Authenticated and signIn fails`() =
        runTest(testDispatcher.scheduler) {
            val testToken = "google_auth_token"
            val initiationResult = SignInInitiationResult.Authenticated(testToken)
            val signInException = RuntimeException("Firebase Sign-In Failed")
            val mappedErrorMessage = "Firebase Sign-In Failed User Message"

            coEvery { mockCredentialService.initiateGoogleSignIn(any()) } returns initiationResult
            coEvery { mockAuthRepository.signIn(testToken) } returns ResultOperation.Error(signInException)
            every { AuthErrorMapper.toUserMessage(signInException.message) } returns mappedErrorMessage

            viewModel.effect.test {
                viewModel.processEvent(UiEvent.OnGoogleSignIn(context))
                testScheduler.advanceUntilIdle()

                assertEquals(UiState.Unauthenticated, viewModel.uiState.value)
                cancelAndConsumeRemainingEvents()
            }
            coVerify(exactly = 1) { mockAuthRepository.signIn(testToken) }
        }

    @Test
    fun `OnGoogleSignIn when initiateGoogleSignIn is Error`() =
        runTest(testDispatcher.scheduler) {
            val errorMessage = "Credential service init error"
            val mappedErrorMessage = "Credential service init error User Message"
            val initiationResult = SignInInitiationResult.Error(errorMessage)

            coEvery { mockCredentialService.initiateGoogleSignIn(any()) } returns initiationResult
            every { AuthErrorMapper.toUserMessage(errorMessage) } returns mappedErrorMessage

            viewModel.effect.test {
                viewModel.processEvent(UiEvent.OnGoogleSignIn(context))
                testScheduler.advanceUntilIdle()

                println("effect is ${awaitItem()}")
                println("ui state is ${viewModel.uiState.value}")
                assertEquals(UiState.Unauthenticated, viewModel.uiState.value)
                verify { Logger.e(message = "initiateGoogleSignIn Error: $errorMessage") }
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `OnGoogleSignIn when initiateGoogleSignIn is NoOp`() =
        runTest(testDispatcher.scheduler) {
            val initiationResult = SignInInitiationResult.NoOp
            coEvery { mockCredentialService.initiateGoogleSignIn(any()) } returns initiationResult

            viewModel.processEvent(UiEvent.OnGoogleSignIn(context))
            testScheduler.advanceUntilIdle()

            assertEquals(UiState.Unauthenticated, viewModel.uiState.value)
        }

    @Test
    fun `OnSignOut when authRepository signOut success and clearUserCredentials success`() =
        runTest(testDispatcher.scheduler) {
            coEvery { mockAuthRepository.signOut() } returns ResultOperation.Success(Unit)
            coEvery { mockCredentialService.clearUserCredentials(onError = any()) } coAnswers {}

            viewModel.processEvent(UiEvent.OnSignOut)
            testScheduler.advanceUntilIdle()

            isUserLoggedInFlow.value = false
            testScheduler.advanceUntilIdle()
            assertEquals(UiState.Unauthenticated, viewModel.uiState.value)

            coVerify(exactly = 1) { mockAuthRepository.signOut() }
            coVerify(exactly = 1) { mockCredentialService.clearUserCredentials(onError = any()) }
        }

    @Test
    fun `OnSignOut when authRepository signOut fails`() =
        runTest(testDispatcher.scheduler) {
            val signOutException = RuntimeException("Firebase SignOut Failed")
            val mappedErrorMessage = "Firebase SignOut Failed User Message"
            coEvery { mockAuthRepository.signOut() } returns ResultOperation.Error(signOutException)
            every { AuthErrorMapper.toUserMessage(signOutException.message) } returns mappedErrorMessage

            viewModel.effect.test {
                viewModel.processEvent(UiEvent.OnSignOut)
                testScheduler.advanceUntilIdle()

                isUserLoggedInFlow.value = true
                testScheduler.advanceUntilIdle()
                assertEquals(UiState.Authenticated, viewModel.uiState.value)

                verify { Logger.e(message = "Couldn't sign out from firebase", throwable = signOutException) }
                coVerify(exactly = 0) { mockCredentialService.clearUserCredentials(onError = any()) }
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `OnSignOut when clearUserCredentials fails via onError callback`() =
        runTest(testDispatcher.scheduler) {
            val clearCredentialsException = RuntimeException("Clear Creds Failed")
            val mappedErrorMessage = "Clear Creds Failed User Message"

            coEvery { mockAuthRepository.signOut() } returns ResultOperation.Success(Unit)
            coEvery { mockCredentialService.clearUserCredentials(onError = capture(clearCredentialsOnErrorSlot)) } coAnswers {
                clearCredentialsOnErrorSlot.captured.invoke(clearCredentialsException)
            }
            every { AuthErrorMapper.toUserMessage(clearCredentialsException.message) } returns mappedErrorMessage

            viewModel.effect.test {
                viewModel.processEvent(UiEvent.OnSignOut)
                testScheduler.advanceUntilIdle()

                isUserLoggedInFlow.value = false
                testScheduler.advanceUntilIdle()
                assertEquals(UiState.Unauthenticated, viewModel.uiState.value)

                verify { Logger.e(message = "Couldn't clear user credentials", throwable = clearCredentialsException) }
                cancelAndConsumeRemainingEvents()
            }
        }
}
