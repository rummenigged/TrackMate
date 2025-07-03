package com.octopus.edu.trackmate.ui.theme

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.octopus.edu.core.ui.common.actions.Route
import com.octopus.edu.core.ui.common.actions.TrackMateNavigationActions
import com.octopus.edu.feature.analytics.AnalyticsScreen
import com.octopus.edu.feature.history.HistoryScreen
import com.octopus.edu.feature.home.HomeScreen
import com.octopus.edu.trackmate.navigation.TrackMateNavigationWrapper

@Composable
internal fun TrackMateApp() {
    val navController = rememberNavController()
    val navActions =
        remember(navController) {
            TrackMateNavigationActions(navController)
        }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

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
                    HomeScreen()
                }

                composable<Route.History> {
                    HistoryScreen()
                }

                composable<Route.Analytics> {
                    AnalyticsScreen()
                }
            }
        }
    }
}
