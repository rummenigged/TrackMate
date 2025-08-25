package com.octopus.edu.feature.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.octopus.edu.core.design.theme.TrackMateTheme
import com.octopus.edu.core.design.theme.components.TrackMateTimePicker
import com.octopus.edu.core.design.theme.utils.LaunchedEffectAndCollectLatest
import com.octopus.edu.core.domain.scheduler.ReminderType
import com.octopus.edu.feature.home.createEntry.CreateEntryUiScreen.UiEffect
import com.octopus.edu.feature.home.createEntry.CreateEntryUiScreen.UiEvent
import com.octopus.edu.feature.home.createEntry.CreateEntryUiScreen.UiState
import com.octopus.edu.feature.home.createEntry.CreateEntryViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow

@Composable
fun EntryCreationBottomLayout(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    EntryCreationBottomLayoutInternal(modifier = modifier, onFinished = onFinished)
}

@Composable
internal fun EntryCreationBottomLayoutInternal(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateEntryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
        EntryCreationBottomBar(
            state = uiState,
            onEvent = viewModel::processEvent,
        )

        if (uiState.isSetEntryDateModeEnabled) {
            EntryDateAndTimeSpecificationsBottomBar(
                state = uiState,
                onEvent = viewModel::processEvent,
            )
        }

        if (uiState.isSetEntryTimeModeEnabled) {
            TrackMateTimePicker(
                onDismiss = { viewModel.processEvent(UiEvent.HideTimePicker) },
                onConfirm = { hour, minute ->
                    viewModel.processEvent(UiEvent.UpdateEntryTime(hour, minute))
                },
            )
        }

        if (uiState.isSetEntryRecurrenceModeEnabled) {
            RecurrencePicker(
                currentRecurrence = uiState.dataDraftSnapshot.recurrence,
                onConfirm = { recurrence -> viewModel.processEvent(UiEvent.UpdateEntryRecurrence(recurrence)) },
                onDismiss = { viewModel.processEvent(UiEvent.HideRecurrencePicker) },
            )
        }

        if (uiState.isSetEntryReminderModeEnabled) {
            val reminders =
                if (uiState.dataDraftSnapshot.time != null ||
                    uiState.data.time != null
                ) {
                    UiState.reminderByTimeOptions
                } else {
                    UiState.reminderByDayOptions
                }

            ReminderPicker(
                reminders = reminders,
                currentReminder = uiState.dataDraftSnapshot.reminder,
                onDismiss = { viewModel.processEvent(UiEvent.HideReminderPicker) },
                onConfirm = { reminder -> viewModel.processEvent(UiEvent.UpdateEntryReminder(reminder)) },
            )
        }

        if (uiState.isSetEntryReminderTypeModeEnabled) {
            ReminderTypePicker(
                ReminderType.entries.toImmutableList(),
                currentReminderType = uiState.dataDraftSnapshot.reminderType,
                onDismiss = { viewModel.processEvent(UiEvent.HideReminderTypePicker) },
                onConfirm = { reminderType -> viewModel.processEvent(UiEvent.UpdateEntryReminderType(reminderType)) },
            )
        }
    }

    EffectHandler(
        viewModel.effect,
        onEvent = viewModel::processEvent,
        onFinished = onFinished,
    )
}

@Composable
private fun EffectHandler(
    effectFlow: Flow<UiEffect?>,
    onEvent: (UiEvent) -> Unit,
    onFinished: () -> Unit
) {
    LaunchedEffectAndCollectLatest(
        effectFlow,
        onEffectConsumed = { onEvent(UiEvent.MarkEffectAsConsumed) },
    ) { effect ->
        when (effect) {
            UiEffect.ShowEntrySuccessfullyCreated -> {
                onFinished()
            }

            is UiEffect.ShowError -> {
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun EntryCreationBottomLayoutPreview() {
    TrackMateTheme {
        EntryCreationBottomLayout(onFinished = {})
    }
}
