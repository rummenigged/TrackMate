package com.octopus.edu.feature.signin

import com.octopus.edu.core.common.credentialService.SignInInitiationResult
import com.octopus.edu.core.ui.common.base.ViewEffect
import com.octopus.edu.core.ui.common.base.ViewEvent
import com.octopus.edu.core.ui.common.base.ViewState

class AuthUiContract {
    sealed interface UiState : ViewState {
        data object Authenticated : UiState

        data object Unauthenticated : UiState

        data object Authenticating : UiState

        data object Unknown : UiState

        fun shouldKeepSplashScreen() = this is Unknown
    }

    sealed interface UiEvent : ViewEvent {
        data object MarkEffectConsumed : UiEvent

        data object OnLaunchGoogleSignIn : UiEvent

        data class OnGoogleSignedIn(
            val result: SignInInitiationResult
        ) : UiEvent

        data object OnSignOut : UiEvent
    }

    sealed interface UiEffect : ViewEffect {
        data object LaunchGoogleSignIn : UiEffect

        data class ShowError(
            val message: String? = null
        ) : UiEffect
    }
}
