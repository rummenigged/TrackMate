package com.octopus.edu.trackmate.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet // Changed import
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.octopus.edu.core.ui.common.actions.Route
import com.octopus.edu.core.ui.common.actions.TrackMateNavigationActions
import com.octopus.edu.feature.analytics.AnalyticsScreen
import com.octopus.edu.feature.history.HistoryScreen
import com.octopus.edu.feature.home.HomeScreen
import com.octopus.edu.feature.home.components.EntryCreationBottomLayout
import com.octopus.edu.trackmate.navigation.TrackMateNavigationWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TrackMateApp() {
    val navController = rememberNavController()
    val navActions =
        remember(navController) {
            TrackMateNavigationActions(navController)
        }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var showEntryCreationSheet by remember { mutableStateOf(false) }
    val sheetState: SheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )

    val openEntryCreationSheet = remember { { showEntryCreationSheet = true } }

    val closeEntryCreationSheet = remember { { showEntryCreationSheet = false } }

    Surface {
        TrackMateNavigationWrapper(
            currentDestination = currentDestination,
            navigateToTopLevelDestination = navActions::navigateTo,
        ) {
            NavHost(
                navController = navController,
                startDestination = Route.Home,
            ) {
                composable<Route.Home> {
                    HomeScreen(
                        onFabClicked = openEntryCreationSheet,
                    )
                }

                composable<Route.History> {
                    HistoryScreen()
                }

                composable<Route.Analytics> {
                    AnalyticsScreen()
                }
            }
        }

        if (showEntryCreationSheet) {
            ModalBottomSheet(
                containerColor = colorScheme.surface,
                onDismissRequest = closeEntryCreationSheet,
                sheetState = sheetState,
            ) {
                EntryCreationBottomLayout(onFinished = closeEntryCreationSheet)
            }
        }
    }
}
