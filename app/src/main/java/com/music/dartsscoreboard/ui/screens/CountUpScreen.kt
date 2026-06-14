package com.music.dartsscoreboard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.dartsscoreboard.data.PlayerGameStats
import com.music.dartsscoreboard.model.CountUpGameState
import com.music.dartsscoreboard.ui.components.DartByDartInput
import com.music.dartsscoreboard.ui.components.QuitGameDialog
import com.music.dartsscoreboard.ui.components.ScoreInput
import com.music.dartsscoreboard.ui.theme.GlassCard
import com.music.dartsscoreboard.ui.theme.NovaTextPrimary
import com.music.dartsscoreboard.ui.theme.NovaTextSecondary
import com.music.dartsscoreboard.ui.theme.StarGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountUpScreen(
    gameState: CountUpGameState,
    endGameStats: List<PlayerGameStats>,
    onScoreSubmit: (Int) -> Unit,
    onUndo: () -> Unit,
    onBack: () -> Unit
) {
    if (gameState.isFinished) {
        GameEndScreen(stats = endGameStats, onBackHome = onBack)
        return
    }

    var useDartByDart by remember { mutableStateOf(false) }
    var showQuitDialog by remember { mutableStateOf(false) }

    if (showQuitDialog) {
        QuitGameDialog(
            onConfirm = onBack,
            onDismiss = { showQuitDialog = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text("Count Up - Manche ${gameState.currentRound}/${gameState.maxRounds}",
                    fontWeight = FontWeight.Bold)
            },
            navigationIcon = {
                IconButton(onClick = { showQuitDialog = true }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                }
            },
            actions = {
                FilledTonalIconButton(
                    onClick = { useDartByDart = !useDartByDart },
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = "Changer de mode")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        Text(
            if (useDartByDart) "Mode : Fléchette par fléchette" else "Mode : Score total",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )

        // Player score cards
        val columns = if (gameState.players.size <= 4) 2 else 3
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(gameState.players) { player ->
                val isActive = player.player.index == gameState.currentPlayerIndex
                GlassCard(
                    shape = RoundedCornerShape(16.dp),
                    active = isActive,
                    accent = StarGold,
                    contentPadding = PaddingValues(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        player.player.name,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isActive) StarGold else NovaTextPrimary,
                        maxLines = 1
                    )
                    Text(
                        "${player.totalScore}",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        color = NovaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CountUpStat("Moy.", "%.1f".format(player.average))
                        CountUpStat("Best", "${player.bestThrow}")
                        CountUpStat("Tour", "${player.rounds}/${gameState.maxRounds}")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            "Tour de ${gameState.currentPlayer.player.name}",
            style = MaterialTheme.typography.titleLarge,
            color = StarGold,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (useDartByDart) {
            DartByDartInput(
                onScoreSubmit = onScoreSubmit,
                onUndo = onUndo
            )
        } else {
            ScoreInput(
                onScoreSubmit = onScoreSubmit,
                onUndo = onUndo,
                maxScore = 180
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CountUpStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = NovaTextSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = NovaTextPrimary)
    }
}
