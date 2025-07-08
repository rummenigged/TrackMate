package com.octopus.edu.feature.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.octopus.edu.core.design.theme.TrackMateTheme
import com.octopus.edu.core.design.theme.components.TrackMateTimePicker
import com.octopus.edu.feature.home.HomeUiContract
import com.octopus.edu.feature.home.HomeUiContract.UiEvent

@Composable
internal fun EntryCreationBottomLayout(
    uiState: HomeUiContract.EntryCreationState,
    onEvent: (UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        if (uiState.isEntryCreationModeEnabled) {
            EntryCreationBottomBar(
                state = uiState,
                onEvent = onEvent,
            )
        }

        if (uiState.isSetEntryDateModeEnabled) {
            EntryDateAndTimeSpecificationsBottomBar(
                state = uiState,
                onEvent = onEvent,
            )
        }

        if (uiState.isSetEntryTimeModeEnabled) {
            TrackMateTimePicker(
                onDismiss = { onEvent(UiEvent.AddEntry.HideTimePicker) },
                onTimeSelected = { hour, minute ->
                    onEvent(UiEvent.UpdateEntryTime(hour, minute))
                },
            )
        }

        if (uiState.isSetEntryRecurrenceModeEnabled) {
            RecurrencePicker(
                currentEntryRecurrency = uiState.currentEntryRecurrence,
                onEvent = onEvent,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun EntryCreationBottomLayoutPreview() {
    TrackMateTheme {
        EntryCreationBottomLayout(
            uiState = HomeUiContract.EntryCreationState(),
            onEvent = {},
        )
    }
}
