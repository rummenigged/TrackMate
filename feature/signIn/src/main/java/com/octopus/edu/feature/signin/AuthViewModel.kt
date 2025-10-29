package com.octopus.edu.feature.signin

import androidx.lifecycle.viewModelScope
import com.octopus.edu.core.common.Logger
import com.octopus.edu.core.common.credentialService.ICredentialService
import com.octopus.edu.core.common.credentialService.SignInInitiationResult
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.repository.AuthRepository
import com.octopus.edu.core.ui.common.AuthErrorMapper.toUserMessage
import com.octopus.edu.core.ui.common.base.BaseViewModel
import com.octopus.edu.feature.signin.AuthUiContract.UiEffect
import com.octopus.edu.feature.signin.AuthUiContract.UiEffect.LaunchGoogleSignIn
import com.octopus.edu.feature.signin.AuthUiContract.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
    @Inject
    constructor(
        private val credentialService: ICredentialService,
        private val authRepository: AuthRepository
    ) : BaseViewModel<AuthUiContract.UiState, UiEffect, UiEvent>() {
        init {
            observeAuthState()
        }

        override fun getInitialState(): AuthUiContract.UiState = AuthUiContract.UiState.Unknown

        override fun processEvent(event: UiEvent) {
            when (event) {
                UiEvent.OnSignOut -> signOut()
                UiEvent.MarkEffectConsumed -> markEffectAsConsumed()
                UiEvent.OnLaunchGoogleSignIn -> setEffect(LaunchGoogleSignIn)
                is UiEvent.OnGoogleSignedIn -> onGoogleSignedIn(event.result)
            }
        }

        private fun observeAuthState() =
            viewModelScope.launch {
                authRepository.isUserLoggedIn.collectLatest { isLoggedIn ->
                    if (isLoggedIn) {
                        setState { AuthUiContract.UiState.Authenticated }
                    } else {
                        setState { AuthUiContract.UiState.Unauthenticated }
                    }
                }
            }

        private fun onGoogleSignedIn(result: SignInInitiationResult) =
            viewModelScope.launch {
                setState { AuthUiContract.UiState.Authenticating }
                when (result) {
                    is SignInInitiationResult.Authenticated -> {
                        signIn(result.idToken)
                    }
                    is SignInInitiationResult.Error -> {
                        Logger.e(
                            message = "initiateGoogleSignIn Error: ${result.message}",
                        )
                        setState { AuthUiContract.UiState.Unauthenticated }
                        UiEffect
                            .ShowError(
                                toUserMessage(result.message),
                            ).send()
                    }
                    SignInInitiationResult.NoOp -> {
                        setState { AuthUiContract.UiState.Unauthenticated }
                    }
                }
            }

        private suspend fun signIn(idToken: String) {
            when (val result = authRepository.signIn(idToken)) {
                is ResultOperation.Error -> {
                    setState { AuthUiContract.UiState.Unauthenticated }
                    UiEffect
                        .ShowError(
                            toUserMessage(result.throwable.message),
                        ).send()
                    Logger.e(
                        message = "App SignIn Error",
                        throwable = result.throwable,
                    )
                }
                is ResultOperation.Success -> {}
            }
        }

        private fun signOut() {
            viewModelScope.launch {
                when (val result = authRepository.signOut()) {
                    is ResultOperation.Error -> {
                        UiEffect
                            .ShowError(
                                toUserMessage(result.throwable.message),
                            ).send()
                        Logger.e(
                            message = "Couldn't sign out from firebase",
                            throwable = result.throwable,
                        )
                    }
                    is ResultOperation.Success -> {
                        credentialService.clearUserCredentials(
                            onError = { error ->
                                UiEffect
                                    .ShowError(
                                        toUserMessage(error.message),
                                    ).send()
                                Logger.e(
                                    message = "Couldn't clear user credentials",
                                    throwable = error,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
