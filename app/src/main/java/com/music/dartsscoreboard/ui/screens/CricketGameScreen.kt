package com.music.dartsscoreboard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.dartsscoreboard.data.PlayerGameStats
import com.music.dartsscoreboard.model.CricketGameState
import com.music.dartsscoreboard.ui.components.CricketScoreBoard
import com.music.dartsscoreboard.ui.components.QuitGameDialog
import com.music.dartsscoreboard.ui.theme.StarGold
import com.music.dartsscoreboard.ui.theme.NovaBlue
import com.music.dartsscoreboard.ui.theme.NovaCoral
import com.music.dartsscoreboard.ui.theme.GhostButton
import com.music.dartsscoreboard.ui.theme.GradientButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CricketGameScreen(
    gameState: CricketGameState,
    endGameStats: List<PlayerGameStats>,
    onHit: (number: Int, multiplier: Int) -> Unit,
    onMiss: () -> Unit,
    onEndTurn: () -> Unit,
    onUndo: () -> Unit,
    onBack: () -> Unit
) {
    if (gameState.isFinished) {
        GameEndScreen(stats = endGameStats, onBackHome = onBack)
        return
    }

    var showQuitDialog by remember { mutableStateOf(false) }

    if (showQuitDialog) {
        QuitGameDialog(
            onConfirm = onBack,
            onDismiss = { showQuitDialog = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Cricket${if (gameState.isTeamMode) " \u2694\uFE0F 2v2" else ""}", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = { showQuitDialog = true }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
                // Titre + compteur de flechettes (max 3 par tour)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Tour de ${gameState.currentPlayer.player.name}",
                        style = MaterialTheme.typography.titleMedium,
                        color = StarGold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${gameState.dartsThisTurn}/3 fléchettes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (gameState.dartsThisTurn >= 3) NovaBlue
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                CricketScoreBoard(
                    players = gameState.players,
                    currentPlayerIndex = gameState.currentPlayerIndex,
                    onNumberClick = onHit,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    isTeamMode = gameState.isTeamMode,
                    inputEnabled = gameState.dartsThisTurn < 3
                )

                // Fléchette ratée : compte une fléchette sans marque
                GhostButton(
                    onClick = onMiss,
                    enabled = gameState.dartsThisTurn < 3,
                    accent = NovaCoral,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                        .height(46.dp)
                ) {
                    Text("Raté (0)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GhostButton(
                        onClick = onUndo,
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Annuler", fontWeight = FontWeight.Bold)
                    }
                    GradientButton(
                        onClick = onEndTurn,
                        modifier = Modifier.weight(2f).height(50.dp)
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Fin du tour", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
