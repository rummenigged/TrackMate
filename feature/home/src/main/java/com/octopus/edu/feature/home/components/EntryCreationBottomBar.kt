package com.octopus.edu.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.octopus.edu.core.design.theme.TrackMateTheme
import com.octopus.edu.core.domain.model.Recurrence
import com.octopus.edu.core.ui.common.extensions.noClickableOverlay
import com.octopus.edu.feature.home.HomeUiContract
import com.octopus.edu.feature.home.HomeUiContract.EntryCreationState.EntryDateState.DateBeforeToday
import com.octopus.edu.feature.home.HomeUiContract.EntryCreationState.EntryDateState.DateLaterToday
import com.octopus.edu.feature.home.HomeUiContract.UiEvent
import com.octopus.edu.feature.home.R
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime

@Composable
internal fun EntryCreationBottomBar(
    state: HomeUiContract.EntryCreationState,
    onEvent: (UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val imePadding =
        if (imeVisible) {
            WindowInsets.ime.exclude(WindowInsets.navigationBars).asPaddingValues()
        } else {
            PaddingValues(bottom = 0.dp)
        }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(colorScheme.scrim.copy(alpha = 0.3f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { onEvent(UiEvent.AddEntry.Cancel) },
                ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(imePadding)
                    .noClickableOverlay(),
        ) {
            BottomInputBar(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun BottomInputBar(
    state: HomeUiContract.EntryCreationState,
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
                .clip(shapes.medium)
                .background(colorScheme.surface)
                .padding(8.dp)
                .draggable(
                    orientation = Orientation.Vertical,
                    state =
                        rememberDraggableState { delta ->
                            if (delta > 12f) onEvent(UiEvent.AddEntry.Cancel)
                        },
                ),
    ) {
        TextField(
            value = state.data.currentEntryTitle.orEmpty(),
            colors = textInputColors,
            onValueChange = { onEvent(UiEvent.UpdateEntryTitle(it)) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            shape = shapes.medium,
            placeholder = {
                Text(
                    text = stringResource(R.string.task_creation_title_placeholder),
                    style = typography.bodyMedium,
                    color = colorScheme.onSurface.copy(alpha = 0.5f),
                )
            },
            maxLines = 4,
        )

        TextField(
            colors = textInputColors,
            value = state.data.currentEntryDescription.orEmpty(),
            onValueChange = {
                onEvent(UiEvent.UpdateEntryDescription(it))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = shapes.medium,
            placeholder = {
                if (!state.data.currentEntryTitle.isNullOrEmpty()) {
                    Text(
                        text = "Description",
                        style = typography.bodyMedium,
                        color = colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
            },
            maxLines = 4,
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
    state: HomeUiContract.EntryCreationState,
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
                    .clip(shapes.small)
                    .padding(4.dp)
                    .clickable { onEvent(UiEvent.AddEntry.ShowSettingsPicker) },
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val dateColor =
                if (state.isDateBeforeToday || state.isTimeBeforeNow) {
                    colorScheme.error
                } else {
                    colorScheme.primary
                }

            Icon(
                imageVector = Icons.Outlined.DateRange,
                contentDescription = stringResource(R.string.entry_date_and_time),
                tint = dateColor,
            )

            val dateState =
                when (val date = state.entryDateState) {
                    is DateBeforeToday ->
                        stringResource(
                            id = date.value,
                            date.month,
                            date.day,
                        )

                    is DateLaterToday -> {
                        stringResource(
                            id = date.value,
                            date.month,
                            date.day,
                        )
                    }

                    else -> stringResource(date.value)
                }

            Text(
                text = dateState,
                style = typography.bodyLarge,
                color = dateColor,
            )

            when (val date = state.entryDateState) {
                is DateBeforeToday ->
                    Text(
                        text = ", ${stringResource(R.string.days_before, date.daysOverdue)}",
                        style = typography.bodyLarge,
                        color = colorScheme.error,
                    )

                is DateLaterToday ->
                    Text(
                        text = ", ${stringResource(R.string.days_later, date.daysLeft)}",
                        style = typography.bodyLarge,
                        color = colorScheme.primary,
                    )

                else -> {}
            }

            state.data.currentEntryTime?.let { time ->
                Text(
                    text = ", $time",
                    style = typography.bodyLarge,
                    color = dateColor,
                )
            }

            state.data.currentEntryRecurrence?.let { recurrence ->
                if (recurrence != Recurrence.None)
                    {
                        Icon(
                            painter = painterResource(R.drawable.ic_autorenew_habit),
                            contentDescription = stringResource(R.string.recurrence),
                            tint = dateColor,
                        )
                    }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            modifier =
                Modifier
                    .clip(shapes.medium)
                    .height(28.dp),
            onClick = { onEvent(UiEvent.Entry.Save) },
            colors =
                IconButtonDefaults.iconButtonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary,
                    disabledContainerColor = colorScheme.surfaceContainerHighest,
                    disabledContentColor = colorScheme.onPrimary.copy(alpha = 0.5f),
                ),
            enabled = !state.data.currentEntryTitle.isNullOrEmpty(),
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
                HomeUiContract.EntryCreationState(
                    data =
                        HomeUiContract.EntryCreationState.EntryCreationData(
                            currentEntryDate = LocalDate.of(2025, 6, 6),
                            currentEntryTime = LocalTime.of(11, 25),
                            currentEntryRecurrence = Recurrence.Daily,
                        ),
                ),
            onEvent = {},
        )
    }
}
