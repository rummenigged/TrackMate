package com.octopus.edu.core.ui.common.actions

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.octopus.edu.core.ui.common.R
import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Home : Route

    @Serializable
    data object Analytics : Route

    @Serializable
    data object History : Route
}

data class TrackMateTopLevelDestination(
    val route: Route,
    @param:DrawableRes val selectedIcon: Int,
    @param:DrawableRes val unselectedIcon: Int,
    @param:StringRes val iconTextId: Int,
)

class TrackMateNavigationActions(
    private val navController: NavHostController,
) {
    fun navigateTo(destination: TrackMateTopLevelDestination) {
        navController.navigate(destination.route) {
            popUpTo(id = navController.graph.findStartDestination().id) {
                saveState = true
            }

            launchSingleTop = true

            restoreState = true
        }
    }
}

val TOP_LEVEL_DESTINATIONS =
    listOf(
        TrackMateTopLevelDestination(
            route = Route.Home,
            selectedIcon = R.drawable.ic_home,
            unselectedIcon = R.drawable.ic_home,
            iconTextId = R.string.tab_home,
        ),
        TrackMateTopLevelDestination(
            route = Route.History,
            selectedIcon = R.drawable.ic_history,
            unselectedIcon = R.drawable.ic_history,
            iconTextId = R.string.tab_history,
        ),
        TrackMateTopLevelDestination(
            route = Route.Analytics,
            selectedIcon = R.drawable.ic_bar_chart,
            unselectedIcon = R.drawable.ic_bar_chart,
            iconTextId = R.string.tab_analytics,
        ),
    )
