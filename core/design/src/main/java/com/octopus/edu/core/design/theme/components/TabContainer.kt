package com.octopus.edu.core.design.theme.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch

@Composable
fun TabContainer(
    tabTitles: List<String>,
    modifier: Modifier = Modifier,
    state: PagerState = rememberPagerState { tabTitles.size },
    tabContent: @Composable (
        title: String,
        isSelected: Boolean,
    ) -> Unit = { title, _ -> Text(title) },
    tabRow: @Composable (
        selectedTabIndex: Int,
        tabs: @Composable () -> Unit,
    ) -> Unit = { index, tabs ->
        TabRow(
            selectedTabIndex = index,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[state.currentPage]),
                )
            },
        ) {
            tabs()
        }
    },
    pageContent: @Composable (pageIndex: Int) -> Unit,
) {
    val scope = rememberCoroutineScope()

    Column(modifier.fillMaxWidth()) {
        tabRow(state.currentPage) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = state.currentPage == index,
                    onClick = {
                        scope.launch {
                            state.animateScrollToPage(index)
                        }
                    },
                    text = { tabContent(title, state.currentPage == index) },
                )
            }
        }

        HorizontalPager(
            state = state,
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top,
        ) { page ->
            AnimatedContent(
                targetState = page,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                        slideOutHorizontally { -it } + fadeOut()
                },
            ) {
                pageContent(it)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TabContainerPreview() {
    val tabNames = listOf("Home", "Explore", "Settings")
    TabContainer(
        tabTitles = tabNames,
        pageContent = { index ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Page: ${tabNames[index]}")
            }
        },
        tabContent = { title, selected ->
            Text(
                text = title,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            )
        },
    )
}
