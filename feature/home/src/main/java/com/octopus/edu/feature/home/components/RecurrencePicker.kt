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
import com.octopus.edu.core.domain.model.Recurrence
import com.octopus.edu.feature.home.R
import com.octopus.edu.feature.home.models.EntryCreationState
import com.octopus.edu.feature.home.models.getRecurrenceAsStringRes
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun RecurrencePicker(
    currentRecurrence: Recurrence?,
    onDismiss: () -> Unit,
    onConfirm: (Recurrence) -> Unit,
    modifier: Modifier = Modifier,
) {
    var internalRecurrence by remember(currentRecurrence) {
        mutableStateOf(currentRecurrence ?: Recurrence.None)
    }
    TrackMateDialog(
        modifier = modifier,
        title = R.string.recurrence,
        confirmText = R.string.ok,
        dismissText = R.string.cancel,
        onDismiss = { onDismiss() },
        onConfirm = { onConfirm(internalRecurrence) },
    ) {
        ReminderOptions(
            recurrences = EntryCreationState.recurrenceOptions,
            selectedRecurrence = internalRecurrence,
            onOptionSelected = { recurrence -> internalRecurrence = recurrence },
        )
    }
}

@Composable
private fun ReminderOptions(
    recurrences: ImmutableList<Recurrence>,
    selectedRecurrence: Recurrence,
    onOptionSelected: (Recurrence) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(recurrences) { recurrence ->
            ReminderItem(
                recurrence,
                isSelected = recurrence == selectedRecurrence,
                onItemClicked = { recurrence -> onOptionSelected(recurrence) },
            )
        }
    }
}

@Composable
private fun ReminderItem(
    recurrence: Recurrence,
    isSelected: Boolean,
    onItemClicked: (Recurrence) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable {
                    onItemClicked(recurrence)
                },
    ) {
        Text(
            modifier = Modifier.padding(vertical = 12.dp),
            text = stringResource(getRecurrenceAsStringRes(recurrence)),
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
private fun RecurrenceDialogPreview() {
    TrackMateTheme {
        RecurrencePicker(
            currentRecurrence = Recurrence.None,
            onDismiss = {},
            onConfirm = {},
        )
    }
}
