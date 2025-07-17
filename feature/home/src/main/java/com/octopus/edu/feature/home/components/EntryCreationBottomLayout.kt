package com.octopus.edu.feature.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.octopus.edu.core.design.theme.TrackMateTheme
import com.octopus.edu.core.design.theme.components.TrackMateTimePicker
import com.octopus.edu.feature.home.HomeUiContract.UiEvent
import com.octopus.edu.feature.home.models.EntryCreationState

@Composable
internal fun EntryCreationBottomLayout(
    uiState: EntryCreationState,
    onEvent: (UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
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
                onConfirm = { hour, minute ->
                    onEvent(UiEvent.UpdateEntryTime(hour, minute))
                },
            )
        }

        if (uiState.isSetEntryRecurrenceModeEnabled) {
            RecurrencePicker(
                currentRecurrence = uiState.dataDraftSnapshot.recurrence,
                onConfirm = { recurrence -> onEvent(UiEvent.UpdateEntryRecurrence(recurrence)) },
                onDismiss = { onEvent(UiEvent.AddEntry.HideRecurrencePicker) },
            )
        }

        if (uiState.isSetEntryReminderModeEnabled) {
            val reminders =
                if (uiState.dataDraftSnapshot.time != null ||
                    uiState.data.time != null
                ) {
                    EntryCreationState.reminderByTimeOptions
                } else {
                    EntryCreationState.reminderByDayOptions
                }

            ReminderPicker(
                reminders = reminders,
                currentReminder = uiState.dataDraftSnapshot.reminder,
                onDismiss = { onEvent(UiEvent.AddEntry.HideReminderPicker) },
                onConfirm = { reminder -> onEvent(UiEvent.UpdateEntryReminder(reminder)) },
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun EntryCreationBottomLayoutPreview() {
    TrackMateTheme {
        EntryCreationBottomLayout(
            uiState = EntryCreationState(),
            onEvent = {},
        )
    }
}
