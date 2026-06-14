package com.music.dartsscoreboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.dartsscoreboard.data.PlayerGameStats
import com.music.dartsscoreboard.model.CheckoutTable
import com.music.dartsscoreboard.model.X01GameState
import com.music.dartsscoreboard.ui.components.DartByDartInput
import com.music.dartsscoreboard.ui.components.DartThrow
import com.music.dartsscoreboard.ui.components.PlayerScoreCard
import com.music.dartsscoreboard.ui.components.QuitGameDialog
import com.music.dartsscoreboard.ui.components.ScoreInput
import com.music.dartsscoreboard.ui.theme.NovaCyan
import com.music.dartsscoreboard.ui.theme.StarGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun X01GameScreen(
    gameState: X01GameState,
    endGameStats: List<PlayerGameStats>,
    onScoreSubmit: (Int) -> Unit,
    onDartsSubmit: (List<DartThrow>) -> Unit,
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

    val currentPlayer = gameState.currentPlayer
    val checkoutHint = CheckoutTable.getCheckout(currentPlayer.scoreRemaining)

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    "${gameState.gameType.displayName}${if (gameState.doubleOut) " (DO)" else ""}${if (gameState.isTeamMode) " \u2694\uFE0F 2v2" else ""}",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = { showQuitDialog = true }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                }
            },
            actions = {
                // Toggle input mode
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

        // Mode indicator
        Text(
            if (useDartByDart) "Mode : Fléchette par fléchette" else "Mode : Score total",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )

        // Player scores grid
        if (gameState.isTeamMode) {
            // Team mode: show 2 team cards
            val teams = gameState.players.groupBy { it.player.teamIndex }.toSortedMap()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                teams.forEach { (teamIdx, members) ->
                    val first = members.first()
                    val second = members.getOrNull(1)
                    val isTeamActive = members.any { it.player.index == gameState.currentPlayerIndex }
                    val activePlayer = members.find { it.player.index == gameState.currentPlayerIndex }
                    PlayerScoreCard(
                        playerState = first,
                        isActive = isTeamActive,
                        checkoutHint = CheckoutTable.getCheckout(first.scoreRemaining),
                        teammateName = second?.player?.name,
                        activePlayerName = activePlayer?.player?.name,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            val columns = if (gameState.players.size <= 4) 2 else 3
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(gameState.players) { playerState ->
                    PlayerScoreCard(
                        playerState = playerState,
                        isActive = playerState.player.index == gameState.currentPlayerIndex,
                        checkoutHint = CheckoutTable.getCheckout(playerState.scoreRemaining)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            "Tour de ${currentPlayer.player.name}",
            style = MaterialTheme.typography.titleLarge,
            color = StarGold,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )

        // Checkout suggestion for current player
        if (checkoutHint != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NovaCyan.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .border(1.dp, NovaCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Checkout : ",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    checkoutHint,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = StarGold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (useDartByDart) {
            DartByDartInput(
                onScoreSubmit = onScoreSubmit,
                onUndo = onUndo,
                onDartsSubmit = if (gameState.doubleOut) onDartsSubmit else null
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
