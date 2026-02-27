package com.relateai.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.relateai.app.ui.screens.dashboard.DashboardScreen
import com.relateai.app.ui.screens.home.AnalyzerViewModel
import com.relateai.app.ui.screens.home.HomeScreen
import com.relateai.app.ui.screens.home.UiState
import com.relateai.app.ui.screens.loading.LoadingScreen
import com.relateai.app.ui.screens.onboarding.OnboardingScreen

@Composable
fun RelateAINavGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    val viewModel: AnalyzerViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // React to state changes by navigating to the right screen
    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Analyzing -> navController.navigate(Routes.LOADING) {
                popUpTo(Routes.HOME) { inclusive = false }
                launchSingleTop = true
            }
            is UiState.Success -> navController.navigate(Routes.DASHBOARD) {
                popUpTo(Routes.HOME) { inclusive = false }
                launchSingleTop = true
            }
            is UiState.Idle, is UiState.Error, is UiState.ReadyToSend, is UiState.Parsing -> {
                val currentRoute = navController.currentDestination?.route
                if (currentRoute == Routes.LOADING || currentRoute == Routes.DASHBOARD) {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            slideInVertically(
                animationSpec = tween(400),
                initialOffsetY = { it / 10 }
            ) + fadeIn(tween(400))
        },
        exitTransition = {
            slideOutVertically(
                animationSpec = tween(300),
                targetOffsetY = { -it / 10 }
            ) + fadeOut(tween(300))
        },
        popEnterTransition = {
            slideInVertically(
                animationSpec = tween(400),
                initialOffsetY = { -it / 10 }
            ) + fadeIn(tween(400))
        },
        popExitTransition = {
            slideOutVertically(
                animationSpec = tween(300),
                targetOffsetY = { it / 10 }
            ) + fadeOut(tween(300))
        }
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onFinish = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                }
            })
        }

        composable(Routes.HOME) {
            HomeScreen(
                uiState = uiState,
                onAnalyzeClick = { viewModel.startAnalysis() },
                onResetClick = { viewModel.reset() },
                onPickFile = { uri -> viewModel.processFileUri(uri) }
            )
        }

        composable(Routes.LOADING) {
            LoadingScreen()
        }

        composable(Routes.DASHBOARD) {
            val successState = uiState as? UiState.Success
            if (successState != null) {
                DashboardScreen(
                    result = successState.result,
                    onNewAnalysis = { viewModel.reset() }
                )
            }
        }
    }
}
