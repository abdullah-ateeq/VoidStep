package com.example.voidstep.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.voidstep.game.viewmodel.GameViewModel
import com.example.voidstep.ui.screens.AboutScreen
import com.example.voidstep.ui.screens.GameScreen
import com.example.voidstep.ui.screens.HomeScreen
import com.example.voidstep.ui.screens.ResultScreen
import com.example.voidstep.ui.screens.SettingsScreen
import com.example.voidstep.ui.screens.SplashScreen

@Composable
fun AppNavGraph(
    gameViewModel: GameViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val gameState by gameViewModel.gameState.collectAsState()
    val preferences by gameViewModel.preferences.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                preferences = preferences,
                onPlayClick = {
                    gameViewModel.startNewGame()
                    navController.navigate(Screen.Game.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onAboutClick = {
                    navController.navigate(Screen.About.route)
                }
            )
        }

        composable(Screen.Game.route) {
            GameScreen(
                gameState = gameState,
                preferences = preferences,
                onStepLeft = { gameViewModel.stepLeft() },
                onStepRight = { gameViewModel.stepRight() },
                onPauseClick = { gameViewModel.pauseGame() },
                onResumeClick = { gameViewModel.resumeGame() },
                onRestartClick = { gameViewModel.startNewGame() },
                onHomeClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onGameOverNavigate = {
                    navController.navigate(Screen.Result.route) {
                        popUpTo(Screen.Game.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Result.route) {
            ResultScreen(
                gameState = gameState,
                preferences = preferences,
                onPlayAgainClick = {
                    gameViewModel.startNewGame()
                    navController.navigate(Screen.Game.route) {
                        popUpTo(Screen.Result.route) { inclusive = true }
                    }
                },
                onHomeClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                preferences = preferences,
                onThemeModeChange = { mode -> gameViewModel.setThemeMode(mode) },
                onSoundToggle = { enabled -> gameViewModel.setSoundEnabled(enabled) },
                onHapticsToggle = { enabled -> gameViewModel.setHapticsEnabled(enabled) },
                onResetBestScore = { gameViewModel.resetBestScore() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                preferences = preferences,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
