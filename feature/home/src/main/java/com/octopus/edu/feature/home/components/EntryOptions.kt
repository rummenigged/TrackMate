package com.octopus.edu.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.octopus.edu.core.design.theme.TrackMateTheme
import com.octopus.edu.core.domain.model.Recurrence
import com.octopus.edu.feature.home.HomeUiContract
import com.octopus.edu.feature.home.HomeUiContract.UiEvent
import com.octopus.edu.feature.home.R
import com.octopus.edu.feature.home.getRecurrenceAsStringRes

@Composable
internal fun EntryOptions(
    state: HomeUiContract.EntryCreationState,
    onEvent: (UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .clip(shapes.medium)
                .background(colorScheme.surfaceContainerLow),
    ) {
        SettingsRow(
            modifier =
                Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clickable { onEvent(UiEvent.AddEntry.ShowTimePicker) },
            icon = painterResource(R.drawable.ic_watch),
            title = stringResource(R.string.time),
            trailingText = state.currentEntryTime.toString(),
            isFilled = state.currentEntryTime != null,
        )

        SettingsRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            icon = painterResource(R.drawable.ic_alarm),
            title = stringResource(R.string.reminder),
            trailingText = state.currentEntryReminder.orEmpty(),
        )

        SettingsRow(
            modifier =
                Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clickable { onEvent(UiEvent.AddEntry.ShowRecurrencePicker) },
            icon = painterResource(R.drawable.ic_autorenew_habit),
            title = stringResource(R.string.repeate),
            trailingText =
                stringResource(
                    getRecurrenceAsStringRes(state.currentEntryRecurrence ?: Recurrence.None),
                ),
            isFilled = state.currentEntryRecurrence != null,
        )
    }
}

@Composable
fun SettingsRow(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    trailingText: String,
    isFilled: Boolean = false
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = icon,
            contentDescription = null,
            tint =
                if (isFilled) {
                    colorScheme.primary
                } else {
                    colorScheme.onSurface
                },
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style =
                if (isFilled) {
                    typography.headlineSmall
                } else {
                    typography.bodyLarge
                },
            color =
                if (isFilled) {
                    colorScheme.primary
                } else {
                    colorScheme.onSurface
                },
        )

        Text(
            text = if (isFilled) trailingText else stringResource(R.string.none),
            style = typography.bodyLarge,
            color =
                if (isFilled) {
                    colorScheme.primary
                } else {
                    colorScheme.onSurface.copy(alpha = 0.5f)
                },
        )

        Spacer(modifier = Modifier.width(4.dp))

        Icon(
            imageVector =
                if (isFilled) {
                    Icons.Default.Close
                } else {
                    Icons.AutoMirrored.Default.KeyboardArrowRight
                },
            contentDescription = null,
            tint =
                if (isFilled) {
                    colorScheme.primary
                } else {
                    colorScheme.onSurface.copy(alpha = 0.5f)
                },
        )
    }
}

@PreviewLightDark
@Composable
private fun EntryOptionsPreview() {
    TrackMateTheme {
        EntryOptions(
            state = HomeUiContract.EntryCreationState(),
            onEvent = {},
        )
    }
}
