package com.octopus.edu.trackmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import com.octopus.edu.core.design.theme.TrackMateTheme
import com.octopus.edu.trackmate.ui.TrackMateApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            CompositionLocalProvider {
                TrackMateTheme(
                    darkTheme = isSystemInDarkTheme(),
                    dynamicColor = false,
                ) {
                    TrackMateApp()
                }
            }
        }
    }
}
