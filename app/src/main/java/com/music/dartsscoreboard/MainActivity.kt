package com.music.dartsscoreboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.music.dartsscoreboard.model.GameType
import com.music.dartsscoreboard.ui.components.SpaceBackground
import com.music.dartsscoreboard.ui.components.UpdateDialog
import com.music.dartsscoreboard.ui.screens.*
import com.music.dartsscoreboard.ui.theme.NovaDartzTheme
import com.music.dartsscoreboard.viewmodel.GameViewModel
import com.music.dartsscoreboard.viewmodel.HistoryViewModel
import com.music.dartsscoreboard.viewmodel.UpdateViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            NovaDartzTheme {
                DartsApp()
            }
        }
    }
}

@Composable
fun DartsApp() {
    val navController = rememberNavController()
    val gameViewModel: GameViewModel = viewModel()
    val historyViewModel: HistoryViewModel = viewModel()
    val updateViewModel: UpdateViewModel = viewModel()
    val updateState by updateViewModel.state.collectAsStateWithLifecycle()

    // Reprise automatique d'une partie non terminée après fermeture de l'app.
    LaunchedEffect(Unit) {
        gameViewModel.activeRoute.value?.let { route ->
            navController.navigate(route)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SpaceBackground()

        NavHost(navController = navController, startDestination = "welcome") {

            composable("welcome") {
                WelcomeScreen(
                    onPlay = {
                        navController.navigate("home") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    }
                )
            }

            composable("home") {
                val savedPlayers by gameViewModel.savedPlayers.collectAsStateWithLifecycle()
                HomeScreen(
                    savedPlayers = savedPlayers,
                    onAddPlayer = { name -> gameViewModel.addSavedPlayer(name) },
                    onDeletePlayer = { player -> gameViewModel.deleteSavedPlayer(player) },
                    onRenamePlayer = { id, name -> gameViewModel.renameSavedPlayer(id, name) },
                    onStartGame = { gameType, playerNames, doubleOut, isTeamMode ->
                        when (gameType) {
                            GameType.CRICKET -> {
                                gameViewModel.startCricketGame(playerNames, isTeamMode)
                                navController.navigate("cricket")
                            }
                            GameType.AROUND_THE_CLOCK -> {
                                gameViewModel.startATCGame(playerNames)
                                navController.navigate("atc")
                            }
                            GameType.COUNT_UP -> {
                                gameViewModel.startCountUpGame(playerNames)
                                navController.navigate("countup")
                            }
                            GameType.KILLER -> {
                                gameViewModel.startKillerGame(playerNames)
                                navController.navigate("killer")
                            }
                            else -> {
                                gameViewModel.startX01Game(playerNames, gameType, doubleOut, isTeamMode)
                                navController.navigate("x01")
                            }
                        }
                    },
                    onViewHistory = { navController.navigate("history") },
                    onViewStats = { navController.navigate("stats") }
                )
            }

            composable("x01") {
                val state by gameViewModel.x01State.collectAsStateWithLifecycle()
                val endStats by gameViewModel.endGameStats.collectAsStateWithLifecycle()
                state?.let { gameState ->
                    X01GameScreen(
                        gameState = gameState,
                        endGameStats = endStats,
                        onScoreSubmit = { score -> gameViewModel.submitX01Score(score) },
                        onDartsSubmit = { darts -> gameViewModel.submitX01Darts(darts) },
                        onUndo = { gameViewModel.undoX01() },
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable("cricket") {
                val state by gameViewModel.cricketState.collectAsStateWithLifecycle()
                val endStats by gameViewModel.endGameStats.collectAsStateWithLifecycle()
                state?.let { gameState ->
                    CricketGameScreen(
                        gameState = gameState,
                        endGameStats = endStats,
                        onHit = { number, multiplier -> gameViewModel.submitCricketHit(number, multiplier) },
                        onMiss = { gameViewModel.submitCricketMiss() },
                        onEndTurn = { gameViewModel.endCricketTurn() },
                        onUndo = { gameViewModel.undoCricket() },
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable("atc") {
                val state by gameViewModel.atcState.collectAsStateWithLifecycle()
                val endStats by gameViewModel.endGameStats.collectAsStateWithLifecycle()
                state?.let { gameState ->
                    AroundTheClockScreen(
                        gameState = gameState,
                        endGameStats = endStats,
                        onThrow = { hit -> gameViewModel.submitATCThrow(hit) },
                        onUndo = { gameViewModel.undoATC() },
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable("countup") {
                val state by gameViewModel.countUpState.collectAsStateWithLifecycle()
                val endStats by gameViewModel.endGameStats.collectAsStateWithLifecycle()
                state?.let { gameState ->
                    CountUpScreen(
                        gameState = gameState,
                        endGameStats = endStats,
                        onScoreSubmit = { score -> gameViewModel.submitCountUpScore(score) },
                        onUndo = { gameViewModel.undoCountUp() },
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable("killer") {
                val state by gameViewModel.killerState.collectAsStateWithLifecycle()
                val endStats by gameViewModel.endGameStats.collectAsStateWithLifecycle()
                state?.let { gameState ->
                    KillerScreen(
                        gameState = gameState,
                        endGameStats = endStats,
                        onAssignNumber = { number -> gameViewModel.assignKillerNumber(number) },
                        onThrow = { hitNumber, isDouble -> gameViewModel.submitKillerThrow(hitNumber, isDouble) },
                        onEndTurn = { gameViewModel.endKillerTurn() },
                        onUndo = { gameViewModel.undoKiller() },
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable("history") {
                val games by historyViewModel.games.collectAsStateWithLifecycle()
                HistoryScreen(
                    games = games,
                    onDelete = { game -> historyViewModel.deleteGame(game) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("stats") {
                val games by historyViewModel.games.collectAsStateWithLifecycle()
                StatsScreen(
                    games = games,
                    onReset = { historyViewModel.resetAll() },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // Mise à jour : dialogue affiché par-dessus tout si une nouvelle version existe.
        if (updateState.visible) {
            UpdateDialog(
                state = updateState,
                onUpdate = { updateViewModel.startUpdate() },
                onDismiss = { updateViewModel.dismiss() }
            )
        }
    }
}
