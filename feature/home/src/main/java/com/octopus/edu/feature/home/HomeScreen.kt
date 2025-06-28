package com.octopus.edu.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.octopus.edu.core.design.theme.TrackMateTheme
import com.octopus.edu.feature.home.HomeUiContract.UiState

@Composable
internal fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
    ) { padding ->
        HomeContent(
            modifier = Modifier.padding(padding),
            state = uiState,
        )
    }
}

@Composable
private fun HomeContent(
    state: UiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(state.screenName)
    }
}

@PreviewLightDark
@Composable
private fun HomePreview() {
    TrackMateTheme {
        Scaffold { padding ->
            HomeContent(
                modifier = Modifier.padding(padding),
                state = UiState(screenName = "Home Screen"),
            )
        }
    }
}
