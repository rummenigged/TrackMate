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
import com.octopus.edu.core.design.theme.TrackMateTheme
import com.octopus.edu.core.design.theme.components.TrackMateDialog
import com.octopus.edu.core.domain.model.Reminder
import com.octopus.edu.core.domain.scheduler.ReminderType
import com.octopus.edu.feature.home.R
import com.octopus.edu.feature.home.models.EntryCreationState
import com.octopus.edu.feature.home.models.getReminderTypeAsStringRes
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun ReminderTypePicker(
    reminders: ImmutableList<ReminderType>,
    onDismiss: () -> Unit,
    onConfirm: (ReminderType) -> Unit,
    currentReminderType: ReminderType?,
    modifier: Modifier = Modifier,
) {
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
            selectedReminder = currentReminder,
            onItemSelected = { reminder -> currentReminder = reminder },
        )
    }
}

@Composable
private fun ReminderTyeOptions(
    reminders: ImmutableList<ReminderType>,
    selectedReminder: ReminderType,
    onItemSelected: (ReminderType) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(reminders) { reminder ->
            ReminderItem(
                reminder,
                isSelected = reminder == selectedReminder,
                onItemClicked = onItemSelected,
            )
        }
    }
}

@Composable
private fun ReminderItem(
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
            style = typography.headlineSmall,
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
private fun ReminderDialogPreview() {
    TrackMateTheme {
        ReminderPicker(
            reminders = EntryCreationState.reminderByDayOptions,
            currentReminder = Reminder.None,
            onConfirm = {},
            onDismiss = {},
        )
    }
}
