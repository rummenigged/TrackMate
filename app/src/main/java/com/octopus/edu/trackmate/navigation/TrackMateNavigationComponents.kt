package com.octopus.edu.trackmate.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldLayout
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.octopus.edu.core.ui.common.actions.TOP_LEVEL_DESTINATIONS
import com.octopus.edu.core.ui.common.actions.TrackMateTopLevelDestination

@Composable
internal fun TrackMateNavigationWrapper(
    currentDestination: NavDestination?,
    navigateToTopLevelDestination: (TrackMateTopLevelDestination) -> Unit,
    content: @Composable () -> Unit,
) {
    NavigationSuiteScaffoldLayout(
        layoutType = NavigationSuiteType.NavigationBar,
        navigationSuite = {
            TrackMateBottomNavigationBar(
                currentDestination = currentDestination,
                navigateToTopLevelDestination = navigateToTopLevelDestination,
            )
        },
    ) {
        content()
    }
}

@Composable
internal fun TrackMateBottomNavigationBar(
    currentDestination: NavDestination?,
    navigateToTopLevelDestination: (TrackMateTopLevelDestination) -> Unit,
) {
    NavigationBar(modifier = Modifier.fillMaxWidth()) {
        TOP_LEVEL_DESTINATIONS.forEach { destination ->
            NavigationBarItem(
                selected = currentDestination.hasRoute(destination),
                onClick = { navigateToTopLevelDestination(destination) },
                icon = {
                    Icon(
                        painter = painterResource(destination.selectedIcon),
                        contentDescription = stringResource(id = destination.iconTextId),
                    )
                },
            )
        }
    }
}

private fun NavDestination?.hasRoute(destination: TrackMateTopLevelDestination): Boolean =
    this?.hasRoute(destination.screen::class) ?: false
