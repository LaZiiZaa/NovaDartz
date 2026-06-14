package com.music.dartsscoreboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.dartsscoreboard.data.PlayerGameStats
import com.music.dartsscoreboard.model.ATCGameState
import com.music.dartsscoreboard.ui.components.DartByDartInput
import com.music.dartsscoreboard.ui.components.QuitGameDialog
import com.music.dartsscoreboard.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AroundTheClockScreen(
    gameState: ATCGameState,
    endGameStats: List<PlayerGameStats>,
    onThrow: (hitTarget: Boolean) -> Unit,
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
        QuitGameDialog(onConfirm = onBack, onDismiss = { showQuitDialog = false })
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Around the Clock", fontWeight = FontWeight.ExtraBold) },
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
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        Text(
            if (useDartByDart) "Mode : Fléchette par fléchette" else "Mode : Touché / Raté",
            style = MaterialTheme.typography.labelMedium,
            color = NovaTextSecondary,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Tour de ${gameState.currentPlayer.player.name}",
                style = MaterialTheme.typography.titleLarge,
                color = StarGold,
                textAlign = TextAlign.Center
            )
            if (!useDartByDart) {
                Text(
                    "Fléchette ${gameState.currentDartsInTurn + 1} / 3",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NovaTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Target display with glow
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(
                            Brush.radialGradient(listOf(NovaCoral.copy(alpha = 0.35f), Color.Transparent)),
                            CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(listOf(Color(0xFFFF8A95), Color(0xFFE0455C))),
                            CircleShape
                        )
                        .border(2.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CIBLE", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
                        Text(
                            gameState.currentPlayer.nextTargetDisplay,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (useDartByDart) {
                DartByDartInput(
                    onScoreSubmit = { },
                    onUndo = onUndo,
                    onDartsSubmit = { darts ->
                        var target = gameState.currentPlayer.currentTarget
                        darts.forEach { dart ->
                            if (dart.number == target && dart.multiplier > 0) {
                                onThrow(true)
                                target = when {
                                    target < 20 -> target + 1
                                    target == 20 -> 25
                                    else -> 25
                                }
                            } else {
                                onThrow(false)
                            }
                        }
                    }
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GhostButton(
                        onClick = { onThrow(false) },
                        accent = NovaCoral,
                        modifier = Modifier.weight(1f).height(66.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Raté", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    GradientButton(
                        onClick = { onThrow(true) },
                        modifier = Modifier.weight(1f).height(66.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Touché", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                GhostButton(
                    onClick = onUndo,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Annuler", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader("Progression", modifier = Modifier.fillMaxWidth(), accent = NovaCyan)
            Spacer(modifier = Modifier.height(10.dp))

            gameState.players.forEach { player ->
                val isActive = player.player.index == gameState.currentPlayerIndex
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    active = isActive,
                    accent = StarGold,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                player.player.name,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) StarGold else NovaTextPrimary
                            )
                            Text(
                                "Cible : ${player.nextTargetDisplay}  ·  Fléchettes : ${player.dartsThrown}",
                                style = MaterialTheme.typography.bodySmall,
                                color = NovaTextSecondary
                            )
                        }
                        Text("${player.targetsHit}/21", fontWeight = FontWeight.Black, color = NovaCyan)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { player.targetsHit / 21f },
                        modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                        color = NovaCyan,
                        trackColor = Color.White.copy(alpha = 0.08f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
