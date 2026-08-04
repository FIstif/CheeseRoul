// ui/navigation/NavGraph.kt
package com.pip.cheeseroul.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pip.cheeseroul.ui.screens.GameScreen
import com.pip.cheeseroul.ui.screens.HistoryScreen
import com.pip.cheeseroul.ui.screens.SetupScreen
import com.pip.cheeseroul.viewmodel.RouletteViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: RouletteViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Setup.route,
        // --- НОВОЕ: Плавные анимации перехода между всеми экранами ---
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400)) },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400)) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400)) }
    ) {
        composable(Screen.Setup.route) {
            SetupScreen(
                viewModel = viewModel,
                onStartGame = {
                    viewModel.startGameSession()
                    navController.navigate(Screen.Game.route)
                },
                onOpenHistory = {
                    navController.navigate(Screen.History.route)
                }
            )
        }

        composable(Screen.Game.route) {
            GameScreen(
                viewModel = viewModel,
                onBackToMenu = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}