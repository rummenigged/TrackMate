package com.octopus.edu.trackmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.octopus.edu.core.common.credentialService.ICredentialService
import com.octopus.edu.core.design.TrackMateApp
import com.octopus.edu.core.design.theme.TrackMateTheme
import com.octopus.edu.core.ui.common.compositionLocals.LocalCredentialManager
import com.octopus.edu.feature.signin.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    @Inject lateinit var credentialManager: ICredentialService

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            authViewModel.uiState.value.shouldKeepSplashScreen()
        }

        setContent {
            CompositionLocalProvider(
                LocalCredentialManager provides credentialManager,
            ) {
                TrackMateTheme(
                    darkTheme = isSystemInDarkTheme(),
                    dynamicColor = false,
                ) {
                    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

                    if (!uiState.shouldKeepSplashScreen()) {
                        TrackMateApp(viewModel = authViewModel)
                    }
                }
            }
        }
    }
}
