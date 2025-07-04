package com.octopus.edu.feature.analytics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.octopus.edu.core.design.theme.TrackMateTheme
import com.octopus.edu.feature.analytics.AnalyticsUiContract.UiState

@Composable
fun AnalyticsScreen(modifier: Modifier = Modifier) {
    AnalyticsScreen(
        modifier = modifier,
        viewModel = hiltViewModel(),
    )
}

@Composable
internal fun AnalyticsScreen(
    modifier: Modifier = Modifier,
    viewModel: AnalyticsViewModel,
) {
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
    ) { padding ->
        AnalyticsContent(
            modifier = Modifier.padding(padding),
            state = uiState,
        )
    }
}

@Composable
private fun AnalyticsContent(
    state: UiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        Text(state.screenName)
    }
}

@PreviewLightDark
@Composable
private fun AnalyticsPreview() {
    TrackMateTheme {
        Scaffold { padding ->
            AnalyticsContent(
                modifier = Modifier.padding(padding),
                state = UiState(screenName = "Analytics Screen"),
            )
        }
    }
}
