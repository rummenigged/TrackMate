package com.octopus.edu.trackmate.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.octopus.edu.core.ui.common.actions.Screen.Analytics
import com.octopus.edu.core.ui.common.actions.Screen.History
import com.octopus.edu.core.ui.common.actions.Screen.Home
import com.octopus.edu.core.ui.common.actions.Screen.MainContent
import com.octopus.edu.core.ui.common.actions.Screen.SignIn
import com.octopus.edu.core.ui.common.actions.TrackMateNavigationActions
import com.octopus.edu.feature.analytics.AnalyticsScreen
import com.octopus.edu.feature.history.HistoryScreen
import com.octopus.edu.feature.home.HomeScreen
import com.octopus.edu.feature.home.components.AddEntryBottomLayout
import com.octopus.edu.feature.home.createEntry.AddEntryViewModel
import com.octopus.edu.feature.signin.AuthUiContract.UiState
import com.octopus.edu.feature.signin.AuthViewModel
import com.octopus.edu.feature.signin.SignInScreen
import com.octopus.edu.trackmate.navigation.TrackMateNavigationWrapper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TrackMateApp(viewModel: AuthViewModel = hiltViewModel()) {
    val navController = rememberNavController()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val initialDestination = if (uiState is UiState.Authenticated) MainContent else SignIn

    LaunchedEffect(uiState, navController) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (uiState is UiState.Authenticated) {
            if (currentRoute != MainContent.name) {
                navController.navigate(MainContent) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
            }
        } else if (currentRoute != SignIn.name) {
            navController.navigate(SignIn) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Surface {
        NavHost(
            navController = navController,
            startDestination = initialDestination,
        ) {
            composable<SignIn> {
                SignInScreen(viewModel = viewModel)
            }

            composable<MainContent> {
                MainAppContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainAppContent(addEntryViewModel: AddEntryViewModel = hiltViewModel()) {
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val mainContentNavController = rememberNavController()
    val navActions =
        remember(mainContentNavController) {
            TrackMateNavigationActions(mainContentNavController)
        }
    val navBackStackEntry by mainContentNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var showEntryCreationSheet by remember { mutableStateOf(false) }
    val sheetState: SheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )

    val openEntryCreationSheet = remember { { showEntryCreationSheet = true } }

    val closeEntryCreationSheet =
        remember {
            {
                addEntryViewModel.clearAddEntrySpecificationsMode()
                showEntryCreationSheet = false
            }
        }

    TrackMateNavigationWrapper(
        currentDestination = currentDestination,
        navigateToTopLevelDestination = navActions::navigateTo,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = mainContentNavController,
                startDestination = Home,
            ) {
                composable<Home> {
                    HomeScreen(
                        modifier = Modifier.testTag("HomeScreen"),
                        onFabClicked = openEntryCreationSheet,
                    )
                }

                composable<History> {
                    HistoryScreen()
                }

                composable<Analytics> {
                    AnalyticsScreen()
                }
            }

            SnackbarHost(
                modifier = Modifier.align(Alignment.BottomCenter),
                hostState = snackBarHostState,
            )
        }
    }

    if (showEntryCreationSheet) {
        ModalBottomSheet(
            containerColor = colorScheme.surface,
            onDismissRequest = closeEntryCreationSheet,
            sheetState = sheetState,
        ) {
            val context = LocalContext.current
            AddEntryBottomLayout(
                viewModel = addEntryViewModel,
                onSuccess = { messageRes, args ->
                    closeEntryCreationSheet()
                    scope.launch {
                        messageRes?.let {
                            snackBarHostState.showSnackbar(context.getString(it, *args.toTypedArray()))
                        }
                    }
                },
                onError = { message ->
                    scope.launch {
                        snackBarHostState.showSnackbar(message)
                    }
                },
            )
        }
    }
}
