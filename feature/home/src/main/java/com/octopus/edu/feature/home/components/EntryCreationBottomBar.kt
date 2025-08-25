package com.octopus.edu.feature.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.octopus.edu.core.design.theme.TrackMateTheme
import com.octopus.edu.core.domain.model.Recurrence
import com.octopus.edu.feature.home.R
import com.octopus.edu.feature.home.createEntry.CreateEntryUiScreen.UiEvent
import com.octopus.edu.feature.home.createEntry.CreateEntryUiScreen.UiState
import com.octopus.edu.feature.home.models.EntryCreationData
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime

@Composable
internal fun EntryCreationBottomBar(
    state: UiState,
    onEvent: (UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth(),
    ) {
        BottomInputBar(
            state = state,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun BottomInputBar(
    state: UiState,
    onEvent: (UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val textInputColors =
        TextFieldDefaults.colors(
            focusedContainerColor = colorScheme.surface,
            unfocusedContainerColor = colorScheme.surface,
            focusedIndicatorColor = colorScheme.surface,
            unfocusedIndicatorColor = colorScheme.surface,
        )

    LaunchedEffect(Unit) {
        delay(150)
        focusRequester.requestFocus()
    }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(8.dp),
    ) {
        TextField(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            value = state.data.title.orEmpty(),
            textStyle = typography.bodyLarge,
            colors = textInputColors,
            shape = shapes.medium,
            maxLines = 4,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            placeholder = {
                Text(
                    text = stringResource(R.string.task_creation_title_placeholder),
                    style = typography.bodyMedium,
                    color = colorScheme.onSurface.copy(alpha = 0.5f),
                )
            },
            onValueChange = { onEvent(UiEvent.UpdateEntryTitle(it)) },
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.data.description.orEmpty(),
            textStyle = typography.bodySmall,
            colors = textInputColors,
            shape = shapes.medium,
            maxLines = 4,
            placeholder = {
                if (!state.data.title.isNullOrEmpty()) {
                    Text(
                        text = stringResource(R.string.task_creation_description_placeholder),
                        style = typography.bodyMedium,
                        color = colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
            },
            onValueChange = {
                onEvent(UiEvent.UpdateEntryDescription(it))
            },
        )

        Spacer(modifier = Modifier.height(8.dp))

        EntryCreationActions(
            state = state,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun EntryCreationActions(
    state: UiState,
    onEvent: (UiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier =
            modifier
                .padding(4.dp)
                .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier =
                Modifier
                    .padding(4.dp)
                    .clickable { onEvent(UiEvent.ShowSettingsPicker) },
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val dateColor =
                if (state.isOverdue) {
                    colorScheme.error
                } else {
                    colorScheme.primary
                }

            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = Icons.Outlined.DateRange,
                contentDescription = stringResource(R.string.entry_date_and_time),
                tint = dateColor,
            )

            val dateText =
                with(state.entryDateState.resolveStringResArgs()) {
                    stringResource(resId, *formatArgs.toTypedArray())
                }

            Text(
                text = dateText,
                style = typography.labelLarge,
                color = dateColor,
            )

            with(state.data) {
                time?.let { time ->
                    Text(
                        text = ", $time",
                        style = typography.labelLarge,
                        color = dateColor,
                    )
                }

                reminder?.let { reminder ->
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(R.drawable.ic_alarm),
                        contentDescription = stringResource(R.string.reminder),
                        tint = dateColor,
                    )
                }
                recurrence?.let { recurrence ->
                    if (recurrence != Recurrence.None) {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            painter = painterResource(R.drawable.ic_autorenew_habit),
                            contentDescription = stringResource(R.string.recurrence),
                            tint = dateColor,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            modifier =
                Modifier
                    .clip(shapes.medium)
                    .height(28.dp),
            onClick = { onEvent(UiEvent.Save) },
            colors =
                IconButtonDefaults.iconButtonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary,
                    disabledContainerColor = colorScheme.surfaceContainerHighest,
                    disabledContentColor = colorScheme.onPrimary.copy(alpha = 0.5f),
                ),
            enabled = !state.data.title.isNullOrEmpty(),
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
            )
        }
    }
}

@PreviewLightDark()
@Composable
private fun BottomPreview() {
    TrackMateTheme {
        EntryCreationBottomBar(
            state =
                UiState(
                    data =
                        EntryCreationData(
                            date = LocalDate.of(2025, 6, 6),
                            time = LocalTime.of(11, 25),
                            recurrence = Recurrence.Daily,
                        ),
                ),
            onEvent = {},
        )
    }
}
