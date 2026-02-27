package com.relateai.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.relateai.app.ui.navigation.RelateAINavGraph
import com.relateai.app.ui.navigation.Routes
import com.relateai.app.ui.screens.home.AnalyzerViewModel
import com.relateai.app.ui.theme.RelateAITheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RelateAITheme {
                val navController = rememberNavController()
                val viewModel: AnalyzerViewModel = hiltViewModel()

                // Determine start destination
                // If launched from a share/onboarding pref, adjust accordingly
                val startDestination = remember {
                    Routes.ONBOARDING // First launch → Onboarding
                    // TODO: Use DataStore to persist "has seen onboarding" flag
                }

                RelateAINavGraph(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.fillMaxSize()
                )

                // Handle incoming shared file from WhatsApp
                LaunchedEffect(Unit) {
                    handleIntent(intent, viewModel::processFileUri, navController)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Will be picked up on next recomposition / LaunchedEffect re-run
    }

    private fun handleIntent(
        intent: Intent?,
        processUri: (Uri) -> Unit,
        navController: androidx.navigation.NavController
    ) {
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    @Suppress("DEPRECATION")
                    val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                        ?: intent.data
                    if (uri != null) {
                        // Navigate to home first, then process
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                            launchSingleTop = true
                        }
                        processUri(uri)
                    }
                }
            }
            Intent.ACTION_VIEW -> {
                val uri = intent.data
                if (uri != null) {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                        launchSingleTop = true
                    }
                    processUri(uri)
                }
            }
        }
    }
}
