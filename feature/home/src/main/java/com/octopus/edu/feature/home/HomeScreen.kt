package com.octopus.edu.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.octopus.edu.core.design.theme.utils.animatedItemIndexed
import com.octopus.edu.core.design.theme.utils.updateAnimateItemsState
import com.octopus.edu.core.domain.model.Entry
import com.octopus.edu.feature.home.HomeUiContract.UiEntry
import com.octopus.edu.feature.home.HomeUiContract.UiEvent
import com.octopus.edu.feature.home.HomeUiContract.UiState
import com.octopus.edu.feature.home.components.EntryItem
import com.octopus.edu.feature.home.components.HomeAppBar
import com.octopus.edu.feature.home.utils.mockEntryList
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

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

    Surface {
        Box(modifier = modifier.fillMaxSize()) {
            HomeContent(
                uiState = uiState,
                onEvent = viewModel::processEvent,
            )

            AddEntryFAB(
                modifier = Modifier.align(Alignment.BottomEnd),
                onClick = onFabClicked,
            )
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
    Column(modifier = modifier.statusBarsPadding()) {
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

            uiState.entries.isEmpty() -> EmptyEntries()

            else ->
                EntriesList(
                    entries = uiState.entries,
                    onEvent = onEvent,
                )
        }
    }
}

@Composable
private fun EntriesList(
    entries: ImmutableList<Entry>,
    onEvent: (UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) = trace("EntriesList") {
    val animatedList by updateAnimateItemsState(entries.map { UiEntry(it) })
    LazyColumn(
        modifier = modifier.fillMaxSize().testTag("entry_list"),
        contentPadding =
            PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp),
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
