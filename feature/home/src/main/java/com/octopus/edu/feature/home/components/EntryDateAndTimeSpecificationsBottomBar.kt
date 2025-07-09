package com.octopus.edu.feature.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.octopus.edu.core.design.theme.TrackMateTheme
import com.octopus.edu.core.design.theme.components.TrackMateDatePicker
import com.octopus.edu.feature.home.HomeUiContract
import com.octopus.edu.feature.home.HomeUiContract.UiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EntryDateAndTimeSpecificationsBottomBar(
    state: HomeUiContract.EntryCreationState,
    onEvent: (UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LocalSoftwareKeyboardController.current?.hide()

    AnimatedVisibility(
        modifier = modifier,
        visible = state.isSetEntryDateModeEnabled,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(shapes.medium)
                    .background(colorScheme.surface),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            EntryTimePickerActionButtons(
                onConfirm = { onEvent(UiEvent.AddEntry.ConfirmDateAndTimeSettings) },
                onDismiss = { onEvent(UiEvent.AddEntry.CancelDateAndTimeSettings) },
            )

            Spacer(Modifier.height(8.dp))

            TrackMateDatePicker(
                modifier = Modifier.padding(horizontal = 16.dp),
                selectedDate = state.currentEntryDateOrToday,
                onDateSelected = { date -> onEvent(UiEvent.UpdateEntryDate(date)) },
            )

            Spacer(Modifier.height(8.dp))

            EntryOptions(
                modifier = Modifier.padding(horizontal = 16.dp),
                state = state,
                onEvent = onEvent,
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EntryTimePickerActionButtons(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { onDismiss() }) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = colorScheme.onSurface,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = { onConfirm() }) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Done",
                tint = colorScheme.onSurface,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun EntryTimeSpecificationsBottomBarPreview() {
    TrackMateTheme {
        EntryDateAndTimeSpecificationsBottomBar(
            state = HomeUiContract.EntryCreationState(isSetEntryDateModeEnabled = true),
            onEvent = {},
        )
    }
}
