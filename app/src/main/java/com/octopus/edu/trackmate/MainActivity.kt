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
import com.octopus.edu.core.design.theme.TrackMateTheme
import com.octopus.edu.feature.signin.AuthViewModel
import com.octopus.edu.trackmate.ui.TrackMateApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    /**
     * Initializes the activity UI, installs a splash screen controlled by the authentication view model, and sets the Compose content with the app theme and main UI.
     *
     * The splash screen remains visible while the activity's `authViewModel` indicates it should be kept. Once the splash condition is cleared, the composed UI displays the TrackMateApp using the same view model.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied; otherwise null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            authViewModel.uiState.value.shouldKeepSplashScreen()
        }

        setContent {
            CompositionLocalProvider {
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