package com.octopus.edu.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.octopus.edu.core.design.theme.TrackMateTheme
import com.octopus.edu.core.design.theme.components.EntryCard
import com.octopus.edu.core.design.theme.components.FullScreenCircularProgress
import com.octopus.edu.core.design.theme.components.TabContainer
import com.octopus.edu.core.design.theme.components.TrackMateTopBar
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.core.domain.model.Habit
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.model.mock
import com.octopus.edu.core.domain.model.mockList
import com.octopus.edu.feature.home.HomeUiContract.Tab.Habits
import com.octopus.edu.feature.home.HomeUiContract.Tab.Tasks
import com.octopus.edu.feature.home.HomeUiContract.UiEvent
import com.octopus.edu.feature.home.HomeUiContract.UiState
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    Scaffold(
        modifier =
            modifier.padding(
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
            ),
        topBar = { TrackMateTopBar(R.string.home_screen_title) },
        floatingActionButton = {
            if (!uiState.isLoading) {
                FloatingActionButton(
                    onClick = {},
                    containerColor = colorScheme.primaryContainer,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                    )
                }
            }
        },
    ) { padding ->
        HomeContent(
            modifier = Modifier.padding(padding),
            state = uiState,
            onEvent = viewModel::processEvent,
        )
    }
}

@Composable
private fun HomeContent(
    state: UiState,
    onEvent: (UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIndex = state.tabs.indexOfFirst { it == state.tabSelected }
    val pagerState = rememberPagerState(initialPage = selectedIndex) { state.tabs.size }

    Column(
        modifier = modifier,
    ) {
        WelcomeHeader()

        TabContainer(
            tabTitles = state.tabTitles,
            state = pagerState,
        ) { tabIndex ->
            LaunchedEffect(tabIndex) {
                onEvent(UiEvent.OnTabSelected(state.getTab(tabIndex)))
            }

            when {
                state.isLoading -> FullScreenCircularProgress()

                state.tabSelected is Habits && state.habits.isEmpty() -> {
                    EmptyEntries(state.tabSelected)
                }

                state.tabSelected is Tasks && state.tasks.isEmpty() -> {
                    EmptyEntries(state.tabSelected)
                }

                else -> {
                    EntriesList(state)
                }
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
        modifier = modifier.background(colorScheme.surface),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (state.tabSelected) {
            is Habits -> {
                items(state.habits) { habit ->
                    EntryItem(habit)
                }
            }

            is Tasks -> {
                items(state.tasks) { task ->
                    EntryItem(task)
                }
            }
        }

        this.item {
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun EntryItem(
    entry: Entry,
    modifier: Modifier = Modifier,
) {
    EntryCard {
        Column(
            modifier =
                modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    modifier = Modifier.offset(x = (-12).dp),
                    checked = entry.isDone,
                    onCheckedChange = { },
                )

                Text(
                    modifier = Modifier.offset(x = (-12).dp),
                    text = entry.title,
                    style = typography.headlineMedium,
                    color = colorScheme.onSurface,
                )
            }

            Text(
                text = entry.description,
                style = typography.bodyLarge,
                color = colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when (entry) {
                    is Habit -> {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_watch),
                            tint = colorScheme.onSurface,
                            contentDescription = null,
                        )

                        entry.getRecurrenceAsText()?.let { recurrence ->
                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = recurrence,
                                style = typography.labelLarge,
                                color = colorScheme.onSurface,
                            )
                        }

                        if (entry.isDone) {
                            entry.lastCompletedDate?.let { completedDate ->
                                Spacer(modifier = Modifier.width(16.dp))

                                Text(
                                    text = stringResource(R.string.completed_date, completedDate),
                                    style = typography.labelLarge,
                                    color = colorScheme.onSurface,
                                )
                            }
                        }
                    }

                    is Task -> {
                        Text(
                            text = stringResource(R.string.created_at, entry.createdAt),
                            style = typography.labelLarge,
                            color = colorScheme.onSurface,
                        )

                        entry.dueDate?.let { dueDate ->
                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = stringResource(R.string.due_date, dueDate),
                                style = typography.labelLarge,
                                color = colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyEntries(
    currentTab: HomeUiContract.Tab,
    modifier: Modifier = Modifier,
) {
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

        val entry =
            when (currentTab) {
                is Habits -> "Habit"
                is Tasks -> "Task"
            }

        Text(
            text = stringResource(R.string.add_new_entries_to_get_started, entry),
            style = typography.bodySmall,
        )
    }
}

@Composable
private fun WelcomeHeader(modifier: Modifier = Modifier) {
    Surface(
        modifier =
            modifier
                .fillMaxWidth(),
        color = colorScheme.primary,
        contentColor = colorScheme.onPrimary,
    ) {
        Text(
            modifier =
                Modifier.padding(
                    start = 16.dp,
                    bottom = 48.dp,
                ),
            text = stringResource(R.string.welcome),
            style = typography.displayLarge,
        )
    }
}

@PreviewLightDark
@Composable
private fun HomeHabitPreview() {
    TrackMateTheme {
        HomeContent(
            state = UiState(habits = Habit.mockList(7).toImmutableList()),
            onEvent = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun HomeTaskPreview() {
    TrackMateTheme {
        HomeContent(
            state = UiState(tasks = Task.mockList(7).toImmutableList()),
            onEvent = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun EmptyEntryPreview() {
    TrackMateTheme {
        EmptyEntries(currentTab = Habits())
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
