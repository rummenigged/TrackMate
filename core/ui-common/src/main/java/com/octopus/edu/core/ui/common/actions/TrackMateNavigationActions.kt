package com.octopus.edu.core.ui.common.actions

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.octopus.edu.core.ui.common.R
import kotlinx.serialization.Serializable

sealed interface Screen {
    val name: String
        get() = this::class.qualifiedName.orEmpty()

    @Serializable
    data object SignIn : Screen

    @Serializable
    data object MainContent : Screen

    @Serializable
    data object Home : Screen

    @Serializable
    data object Analytics : Screen

    @Serializable
    data object History : Screen
}

data class TrackMateTopLevelDestination(
    val screen: Screen,
    @param:DrawableRes val selectedIcon: Int,
    @param:DrawableRes val unselectedIcon: Int,
    @param:StringRes val iconTextId: Int,
)

class TrackMateNavigationActions(
    private val navController: NavHostController,
) {
    fun navigateTo(destination: TrackMateTopLevelDestination) {
        navController.navigate(destination.screen) {
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
            screen = Screen.Home,
            selectedIcon = R.drawable.ic_home,
            unselectedIcon = R.drawable.ic_home,
            iconTextId = R.string.tab_home,
        ),
        TrackMateTopLevelDestination(
            screen = Screen.History,
            selectedIcon = R.drawable.ic_history,
            unselectedIcon = R.drawable.ic_history,
            iconTextId = R.string.tab_history,
        ),
        TrackMateTopLevelDestination(
            screen = Screen.Analytics,
            selectedIcon = R.drawable.ic_bar_chart,
            unselectedIcon = R.drawable.ic_bar_chart,
            iconTextId = R.string.tab_analytics,
        ),
    )
