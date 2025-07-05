package com.octopus.edu.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.octopus.edu.core.design.theme.TrackMateTheme
import com.octopus.edu.core.design.theme.components.EntryCard
import com.octopus.edu.core.design.theme.components.FullScreenCircularProgress
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.model.mock
import com.octopus.edu.feature.home.HomeUiContract.UiEvent
import com.octopus.edu.feature.home.HomeUiContract.UiState
import kotlinx.collections.immutable.toImmutableList

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    HomeScreen(
        modifier = modifier,
        viewModel = hiltViewModel(),
    )
}

@Composable
internal fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
) {
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        HomeContent(
            modifier = modifier.windowInsetsPadding(WindowInsets.statusBars),
            state = uiState,
            onEvent = viewModel::processEvent,
        )

        if (!uiState.isLoading) {
            FloatingActionButton(
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                onClick = {},
                containerColor = colorScheme.primaryContainer,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    state: UiState,
    onEvent: (UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        when {
            state.isLoading -> FullScreenCircularProgress()

            state.entries.isEmpty() -> EmptyEntries()

            else -> {
                EntriesList(state = state)
            }
        }
    }
}

@Composable
private fun EntriesList(
    state: UiState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding =
            PaddingValues(
                top = 16.dp,
                start = 16.dp,
                end = 16.dp,
            ),
    ) {
        itemsIndexed(state.entries) { index, habit ->
            EntryItem(
                entry = habit,
                isFirstItem = index == 0,
                isLastItem = index == state.entries.lastIndex,
            )
        }

        item {
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
        }
    }
}

@Composable
private fun EntryItem(
    entry: Entry,
    modifier: Modifier = Modifier,
    isFirstItem: Boolean = false,
    isLastItem: Boolean = false,
) {
    ConstraintLayout(
        modifier =
            modifier
                .fillMaxWidth()
                .wrapContentHeight(),
    ) {
        val (time, icon, topLine, bottomLine, card) = createRefs()

        Text(
            text = entry.time,
            style = typography.labelMedium,
            modifier =
                Modifier.constrainAs(time) {
                    start.linkTo(parent.start)
                    top.linkTo(card.top)
                    bottom.linkTo(card.bottom)
                },
        )

        if (!isFirstItem) {
            Box(
                modifier =
                    Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(colorScheme.onSurface.copy(alpha = 0.2f))
                        .constrainAs(topLine) {
                            start.linkTo(icon.start)
                            top.linkTo(parent.top)
                            end.linkTo(icon.end)
                            bottom.linkTo(icon.top, margin = 2.dp)
                        },
            )
        }

        val iconsResource =
            when (entry) {
                is Habit -> R.drawable.ic_autorenew_habit
                is Task -> R.drawable.ic_task_circle
            }

        Icon(
            painter = painterResource(iconsResource),
            contentDescription = null,
            tint = colorScheme.primary,
            modifier =
                Modifier
                    .size(16.dp)
                    .constrainAs(icon) {
                        start.linkTo(time.end, margin = 8.dp)
                        top.linkTo(time.top)
                        bottom.linkTo(time.bottom)
                    },
        )

        if (!isLastItem) {
            Box(
                modifier =
                    Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(colorScheme.onSurface.copy(alpha = 0.2f))
                        .constrainAs(bottomLine) {
                            start.linkTo(icon.start)
                            top.linkTo(icon.bottom, margin = 2.dp)
                            end.linkTo(icon.end)
                            bottom.linkTo(parent.bottom)
                        },
            )
        }

        EntryCard(
            modifier =
                Modifier
                    .height(IntrinsicSize.Min)
                    .constrainAs(card) {
                        start.linkTo(icon.end, margin = 4.dp)
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                    },
        ) {
            Row(
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            ) {
                Text(
                    text = entry.title,
                    style = typography.bodyMedium,
                )

                if (entry is Habit && entry.getRecurrenceAsText() != null) {
                    Spacer(modifier = Modifier.weight(1F))

                    Text(
                        text = entry.getRecurrenceAsText().orEmpty(),
                        style = typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyEntries(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .background(color = colorScheme.surfaceContainer, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier.padding(24.dp),
                painter = painterResource(id = R.drawable.ic_empty_entries),
                tint = colorScheme.onSurface,
                contentDescription = null,
            )
        }

        Text(
            text = stringResource(R.string.all_caught_up),
            style = typography.titleMedium,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.add_new_entries_to_get_started),
            style = typography.bodySmall,
        )
    }
}

@PreviewLightDark
@Composable
private fun HomePreview() {
    TrackMateTheme {
        HomeContent(
            state = UiState(entries = mockEntryList(8).toImmutableList()),
            onEvent = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun EmptyEntryPreview() {
    TrackMateTheme {
        EmptyEntries()
    }
}

@PreviewLightDark
@Composable
private fun TaskItemPreview() {
    TrackMateTheme {
        EntryItem(Task.mock("1"))
    }
}

@PreviewLightDark
@Composable
private fun TaskHabitPreview() {
    TrackMateTheme {
        EntryItem(Habit.mock("1"))
    }
}
