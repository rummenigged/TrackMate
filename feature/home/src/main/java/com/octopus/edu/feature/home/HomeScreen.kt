package com.octopus.edu.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.trace
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.octopus.edu.core.design.theme.TrackMateTheme
import com.octopus.edu.core.design.theme.components.FullScreenCircularProgress
import com.octopus.edu.core.design.theme.components.WeekCalendar
import com.octopus.edu.core.design.theme.components.snackBar.SnackBarType
import com.octopus.edu.core.design.theme.components.snackBar.TrackMateSnackBarHost
import com.octopus.edu.core.design.theme.components.snackBar.showSnackBar
import com.octopus.edu.core.design.theme.components.snackBar.toMillis
import com.octopus.edu.core.design.theme.utils.LaunchedEffectAndCollectLatest
import com.octopus.edu.core.design.theme.utils.animatedItemIndexed
import com.octopus.edu.core.design.theme.utils.updateAnimateItemsState
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.feature.home.HomeUiContract.UiEffect
import com.octopus.edu.feature.home.HomeUiContract.UiEntry
import com.octopus.edu.feature.home.HomeUiContract.UiEvent
import com.octopus.edu.feature.home.HomeUiContract.UiEvent.Entry.MarkAsDone
import com.octopus.edu.feature.home.HomeUiContract.UiEvent.Entry.UnmarkAsDone
import com.octopus.edu.feature.home.HomeUiContract.UiState
import com.octopus.edu.feature.home.components.EntryItem
import com.octopus.edu.feature.home.components.HomeAppBar
import com.octopus.edu.feature.home.utils.mockEntryList
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onFabClicked: () -> Unit
) {
    HomeScreenInternal(
        modifier = modifier,
        onFabClicked = onFabClicked,
    )
}

@Composable
internal fun HomeScreenInternal(
    onFabClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackBarHostState = remember { SnackbarHostState() }

    Surface {
        Box(modifier = modifier.fillMaxSize()) {
            HomeContent(
                uiState = uiState,
                onEvent = viewModel::processEvent,
            )

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomEnd),
            ) {
                TrackMateSnackBarHost(
                    snackBarHostState = snackBarHostState,
                )

                AddEntryFAB(
                    modifier = Modifier.align(Alignment.End),
                    onClick = onFabClicked,
                )
            }
        }
    }

    EffectHandler(
        effect = viewModel.effect,
        snackBarHostState = snackBarHostState,
        onEvent = viewModel::processEvent,
    )
}

@Composable
private fun EffectHandler(
    effect: Flow<UiEffect?>,
    snackBarHostState: SnackbarHostState,
    onEvent: (UiEvent) -> Unit
) {
    val context = LocalContext.current
    val accessibilityManager = LocalAccessibilityManager.current

    LaunchedEffectAndCollectLatest(
        flow = effect,
        onEffectConsumed = { onEvent(UiEvent.MarkEffectAsConsumed) },
    ) { event ->
        when (event) {
            UiEffect.ShowEntrySuccessfullyDeleted -> {}

            is UiEffect.ShowEntrySuccessfullyMarkedAsDone -> {
                snackBarHostState
                    .showSnackBar(
                        message =
                            context.getString(
                                R.string.entry_marked_as_done,
                            ),
                        actionLabel = context.getString(R.string.undo),
                        duration = SnackbarDuration.Short,
                    ).also { result ->
                        if (result == SnackbarResult.ActionPerformed) {
                            onEvent(UnmarkAsDone(event.entryId))
                        }
                    }
            }

            is UiEffect.MarkEntryAsDoneFailed -> {
                val actionLabel = if (event.isRetriable) context.getString(R.string.retry) else null
                snackBarHostState
                    .showSnackBar(
                        message =
                            context.getString(
                                R.string.cant_mark_entry_as_done,
                            ),
                        actionLabel = actionLabel,
                        duration = SnackbarDuration.Short,
                        type = SnackBarType.ERROR,
                    ).also { result ->
                        if (result == SnackbarResult.ActionPerformed) {
                            onEvent(
                                MarkAsDone(
                                    event.entryId,
                                    SnackbarDuration.Short.toMillis(
                                        hasAction = actionLabel != null,
                                        accessibilityManager = accessibilityManager,
                                    ),
                                ),
                            )
                        }
                    }
            }

            is UiEffect.ShowError -> {
                val actionLabel = if (event.isRetriable) context.getString(R.string.retry) else null
                snackBarHostState
                    .showSnackBar(
                        message =
                            context.getString(
                                R.string.cant_load_entries,
                            ),
                        actionLabel = actionLabel,
                        duration = SnackbarDuration.Short,
                        type = SnackBarType.ERROR,
                    ).also { result ->
                        if (result == SnackbarResult.ActionPerformed) {
                            onEvent(UiEvent.Entry.GetFromCurrentDate)
                        }
                    }
            }

            is UiEffect.UnmarkEntryAsDoneFailed -> {
                snackBarHostState
                    .showSnackBar(
                        message =
                            context.getString(
                                R.string.cant_mark_entry_as_undone,
                            ),
                        duration = SnackbarDuration.Short,
                        type = SnackBarType.ERROR,
                    )
            }
        }
    }
}

@Composable
private fun AddEntryFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        modifier = modifier.padding(16.dp).testTag("add_entry_fab"),
        onClick = onClick,
        containerColor = colorScheme.primaryContainer,
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    uiState: UiState,
    onEvent: (UiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState =
        rememberPagerState(
            initialPage = Int.MAX_VALUE / 2,
            pageCount = { Int.MAX_VALUE },
        )

    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        modifier = modifier,
        isRefreshing = uiState.isRefreshing,
        state = pullToRefreshState,
        onRefresh = { onEvent(UiEvent.Refresh) },
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            HomeAppBar(
                title =
                    stringResource(
                        R.string.current_month_and_day,
                        uiState.currentMonth,
                        uiState.currentYear,
                    ),
            )

            WeekCalendar(
                selectedDate = uiState.currentDate,
                pagerState = pagerState,
                onDateSelected = { date ->
                    onEvent(UiEvent.SetCurrentDateAs(date))
                },
            )

            when {
                uiState.isLoading -> FullScreenCircularProgress()
                uiState.entries.isEmpty() ->
                    EmptyEntries(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                    )
                else ->
                    EntriesList(
                        entries = uiState.entries,
                        onEvent = onEvent,
                    )
            }
        }
    }
}

@Composable
private fun EntriesList(
    entries: ImmutableList<Entry>,
    onEvent: (UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) = trace("EntriesList") {
    val accessibilityManager = LocalAccessibilityManager.current
    val animatedList by updateAnimateItemsState(entries.map { UiEntry(it) })
    LazyColumn(
        modifier = modifier.fillMaxSize().testTag("entry_list"),
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp),
    ) {
        animatedItemIndexed(
            items = animatedList,
            key = { item -> item.entry.id },
        ) { index, item ->
            EntryItem(
                modifier = Modifier.testTag("entry_item"),
                entry = item.entry,
                isFirstItem = index == 0,
                isLastItem = index == entries.lastIndex,
                onItemSwipedFromStartToEnd = { entry ->
                    onEvent(
                        MarkAsDone(
                            entry.id,
                            SnackbarDuration.Short.toMillis(
                                hasAction = false,
                                accessibilityManager = accessibilityManager,
                            ),
                        ),
                    )
                },
                onItemSwipedFromEndToStart = { entry -> onEvent(UiEvent.Entry.Delete(entry.id)) },
            )
        }

        item {
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
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
            modifier = Modifier.background(color = colorScheme.surfaceContainer, shape = CircleShape),
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
            style = typography.bodyMedium,
        )
    }
}

@PreviewLightDark
@Composable
private fun HomePreview() {
    TrackMateTheme {
        HomeContent(
            uiState = UiState(entries = mockEntryList(8).toImmutableList()),
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
