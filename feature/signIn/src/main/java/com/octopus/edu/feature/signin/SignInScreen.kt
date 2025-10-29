package com.octopus.edu.feature.signin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.octopus.edu.core.design.theme.TrackMateTheme
import com.octopus.edu.core.design.theme.components.PrimaryIconButton
import com.octopus.edu.core.design.theme.utils.LaunchedEffectAndCollectLatest
import com.octopus.edu.core.ui.common.compositionLocals.LocalCredentialManager
import com.octopus.edu.core.ui.common.rememberGoogleSignInLauncher
import com.octopus.edu.feature.signin.AuthUiContract.UiEffect
import com.octopus.edu.feature.signin.AuthUiContract.UiEvent
import com.octopus.edu.feature.signin.AuthUiContract.UiState
import kotlinx.coroutines.flow.Flow

@Composable
fun SignInScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackBarHostState) },
    ) { paddingValues ->
        SignInInContent(
            uiState = uiState,
            onEvent = viewModel::processEvent,
            modifier = Modifier.padding(paddingValues),
        )
    }

    EffectHandler(
        viewModel.effect,
        snackBarHostState,
        viewModel::processEvent,
    )
}

@Composable
private fun EffectHandler(
    uiEffect: Flow<UiEffect?>,
    snackBarHostState: SnackbarHostState,
    onEvent: (UiEvent) -> Unit
) {
    val credentialManager = LocalCredentialManager.current
    val googleSignInLauncher =
        rememberGoogleSignInLauncher(
            credentialService = credentialManager,
            onSignInResult = { result -> onEvent(UiEvent.OnGoogleSignedIn(result)) },
        )

    LaunchedEffectAndCollectLatest(
        uiEffect,
        onEffectConsumed = { onEvent(UiEvent.MarkEffectConsumed) },
    ) { effect ->
        when (effect) {
            is UiEffect.ShowError -> {
                effect.message?.let { message ->
                    snackBarHostState.showSnackbar(message)
                }
            }

            UiEffect.LaunchGoogleSignIn -> googleSignInLauncher.launch()
        }
    }
}

@Composable
private fun SignInInContent(
    uiState: UiState,
    onEvent: (UiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface {
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(all = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SignInAnimation()

            Text(
                text = stringResource(R.string.sign_in_text),
                style = typography.headlineLarge,
                color = colorScheme.primary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.weight(1f))

            SigInInButtonWithLoading(
                isLoading = uiState is UiState.Authenticating,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun SigInInButtonWithLoading(
    isLoading: Boolean,
    onEvent: (UiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isLoading,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        CircularProgressIndicator()
    }

    AnimatedVisibility(
        modifier = modifier,
        visible = !isLoading,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        PrimaryIconButton(
            text = stringResource(R.string.login_google),
            iconRes = com.octopus.edu.core.design.R.drawable.ic_google_new,
            onClick = { onEvent(UiEvent.OnLaunchGoogleSignIn) },
        )
    }
}

@Composable
private fun SignInAnimation() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.sign_in_animation),
    )

    val progress by animateLottieCompositionAsState(composition)

    LottieAnimation(
        composition = composition,
        contentScale = ContentScale.FillWidth,
        progress = { progress },
    )
}

@PreviewLightDark
@Composable
private fun SigInContentPreview() {
    TrackMateTheme {
        SignInInContent(
            uiState = UiState.Unauthenticated,
            onEvent = {},
        )
    }
}
