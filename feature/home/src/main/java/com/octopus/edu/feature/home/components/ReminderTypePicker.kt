package com.octopus.edu.feature.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.trace
import com.octopus.edu.core.design.theme.TrackMateTheme
import com.octopus.edu.core.design.theme.components.TrackMateDialog
import com.octopus.edu.core.domain.model.Reminder
import com.octopus.edu.core.domain.scheduler.ReminderType
import com.octopus.edu.feature.home.R
import com.octopus.edu.feature.home.createEntry.AddEntryUiContractor.UiState
import com.octopus.edu.feature.home.createEntry.AddEntryUiContractor.getReminderTypeAsStringRes
import kotlinx.collections.immutable.ImmutableList

/**
 * Displays a dialog that lets the user pick a ReminderType.
 *
 * The dialog initializes its selection from [currentReminderType] or defaults to `ReminderType.NOTIFICATION`.
 * When confirmed, it calls [onConfirm] with the selected reminder; when dismissed, it calls [onDismiss].
 *
 * @param reminders The list of reminder options to display.
 * @param onDismiss Callback invoked when the dialog is dismissed without confirmation.
 * @param onConfirm Callback invoked with the currently selected `ReminderType` when the user confirms.
 * @param currentReminderType Optional initial selection for the dialog; if `null`, selection defaults to `ReminderType.NOTIFICATION`.
 */
@Composable
internal fun ReminderTypePicker(
    reminders: ImmutableList<ReminderType>,
    onDismiss: () -> Unit,
    onConfirm: (ReminderType) -> Unit,
    currentReminderType: ReminderType?,
    modifier: Modifier = Modifier,
) = trace("ReminderTypePicker") {
    var currentReminder by remember(currentReminderType) {
        mutableStateOf(currentReminderType ?: ReminderType.NOTIFICATION)
    }

    TrackMateDialog(
        modifier = modifier,
        title = R.string.reminder_type,
        confirmText = R.string.ok,
        dismissText = R.string.cancel,
        onDismiss = { onDismiss() },
        onConfirm = { onConfirm(currentReminder) },
    ) {
        ReminderTyeOptions(
            reminders = reminders,
            selectedReminderType = currentReminder,
            onItemSelected = { reminder -> currentReminder = reminder },
        )
    }
}

@Composable
private fun ReminderTyeOptions(
    reminders: ImmutableList<ReminderType>,
    selectedReminderType: ReminderType,
    onItemSelected: (ReminderType) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(reminders) { reminder ->
            ReminderTypeItem(
                reminder,
                isSelected = reminder == selectedReminderType,
                onItemClicked = onItemSelected,
            )
        }
    }
}

@Composable
private fun ReminderTypeItem(
    reminder: ReminderType,
    isSelected: Boolean,
    onItemClicked: (ReminderType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable {
                    onItemClicked(reminder)
                },
    ) {
        Text(
            modifier = Modifier.padding(vertical = 12.dp),
            text = stringResource(getReminderTypeAsStringRes(reminder)),
            style = typography.titleSmall,
        )

        if (isSelected) {
            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = colorScheme.primary,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ReminderTypeDialogPreview() {
    TrackMateTheme {
        ReminderPicker(
            reminders = UiState.reminderByDayOptions,
            currentReminder = Reminder.None,
            onConfirm = {},
            onDismiss = {},
        )
    }
}