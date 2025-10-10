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
