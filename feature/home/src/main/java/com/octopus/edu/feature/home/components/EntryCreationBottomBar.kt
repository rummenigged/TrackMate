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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.octopus.edu.core.design.theme.TrackMateTheme
import com.octopus.edu.feature.home.HomeUiContract
import com.octopus.edu.feature.home.HomeUiContract.UiEvent
import com.octopus.edu.feature.home.R
import kotlinx.coroutines.delay

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
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {},
                    ),
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
            value = state.currentEntryTitle.orEmpty(),
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
            value = state.currentEntryDescription.orEmpty(),
            onValueChange = {
                onEvent(UiEvent.UpdateEntryDescription(it))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = shapes.medium,
            placeholder = {
                if (!state.currentEntryTitle.isNullOrEmpty()) {
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
        ) {
            IconButton(onClick = {
                onEvent(UiEvent.AddEntry.ShowSettingsPicker)
            }) {
                Icon(
                    imageVector = Icons.Outlined.DateRange,
                    contentDescription = stringResource(R.string.today),
                    modifier = Modifier.size(16.dp),
                    tint = colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (!state.currentEntryTitle.isNullOrEmpty()) {
                IconButton(
                    modifier =
                        Modifier
                            .clip(shapes.medium)
                            .background(colorScheme.primary),
                    onClick = { onEvent(UiEvent.Entry.Save) },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

@PreviewLightDark()
@Composable
private fun BottomPreview() {
    TrackMateTheme {
        EntryCreationBottomBar(
            state = HomeUiContract.EntryCreationState(),
            onEvent = {},
        )
    }
}
