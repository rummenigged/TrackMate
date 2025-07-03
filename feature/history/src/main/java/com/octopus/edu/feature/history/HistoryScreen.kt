package com.octopus.edu.feature.history

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
import com.octopus.edu.feature.history.HistoryUiContract.UiState

@Composable
fun HistoryScreen(modifier: Modifier = Modifier) {
    HistoryScreen(
        modifier = modifier,
        viewModel = hiltViewModel(),
    )
}

@Composable
internal fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel,
) {
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
    ) { padding ->
        HistoryContent(
            modifier = Modifier.padding(padding),
            state = uiState,
        )
    }
}

@Composable
private fun HistoryContent(
    state: UiState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(state.screenName)
    }
}

@PreviewLightDark
@Composable
private fun HomePreview() {
    TrackMateTheme {
        Scaffold { padding ->
            HistoryContent(
                modifier = Modifier.padding(padding),
                state = UiState(screenName = "History Screen"),
            )
        }
    }
}
