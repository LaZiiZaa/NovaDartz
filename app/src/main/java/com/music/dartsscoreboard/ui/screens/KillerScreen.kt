package com.music.dartsscoreboard.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
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
import com.music.dartsscoreboard.model.KillerGameState
import com.music.dartsscoreboard.model.KillerPhase
import com.music.dartsscoreboard.ui.components.DartByDartInput
import com.music.dartsscoreboard.ui.theme.GhostButton
import com.music.dartsscoreboard.ui.theme.GlassCard
import com.music.dartsscoreboard.ui.theme.GradientButton
import com.music.dartsscoreboard.ui.theme.NovaCoral
import com.music.dartsscoreboard.ui.theme.NovaDangerGradient
import com.music.dartsscoreboard.ui.theme.NovaRed
import com.music.dartsscoreboard.ui.theme.NovaTextMuted
import com.music.dartsscoreboard.ui.theme.NovaTextPrimary
import com.music.dartsscoreboard.ui.theme.NovaTextSecondary
import com.music.dartsscoreboard.ui.theme.SpaceCard
import com.music.dartsscoreboard.ui.theme.StarGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KillerScreen(
    gameState: KillerGameState,
    endGameStats: List<PlayerGameStats>,
    onAssignNumber: (Int) -> Unit,
    onThrow: (hitNumber: Int, isDouble: Boolean) -> Unit,
    onEndTurn: () -> Unit,
    onUndo: () -> Unit,
    onBack: () -> Unit
) {
    if (gameState.isFinished) {
        GameEndScreen(stats = endGameStats, onBackHome = onBack)
        return
    }

    var useDartByDart by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Killer", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                }
            },
            actions = {
                if (gameState.phase == KillerPhase.PLAYING) {
                    FilledTonalIconButton(
                        onClick = { useDartByDart = !useDartByDart },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Changer de mode")
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        if (gameState.phase == KillerPhase.PLAYING) {
            Text(
                if (useDartByDart) "Mode : Fléchette par fléchette" else "Mode : Sélection manuelle",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                textAlign = TextAlign.Center
            )
        }

        when (gameState.phase) {
            KillerPhase.ASSIGN_NUMBERS -> AssignNumbersPhase(gameState, onAssignNumber)
            KillerPhase.PLAYING -> {
                if (useDartByDart) {
                    PlayingPhaseDartByDart(gameState, onThrow, onEndTurn, onUndo)
                } else {
                    PlayingPhase(gameState, onThrow, onEndTurn, onUndo)
                }
            }
        }
    }
}

@Composable
private fun AssignNumbersPhase(
    gameState: KillerGameState,
    onAssignNumber: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Attribution des numéros",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = StarGold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "${gameState.currentPlayer.player.name}, choisissez votre numéro",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Already assigned numbers
        val takenNumbers = gameState.players.filter { it.assignedNumber != 0 }.map { it.assignedNumber }.toSet()

        // Number grid 1-20
        (1..20).chunked(5).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { number ->
                    val taken = number in takenNumbers
                    Button(
                        onClick = { if (!taken) onAssignNumber(number) },
                        enabled = !taken,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (taken) Color.Gray.copy(alpha = 0.3f)
                            else SpaceCard,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            "$number",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (taken) Color.Gray else Color.White
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Show assigned numbers
        if (takenNumbers.isNotEmpty()) {
            Text("Numéros attribués :", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            gameState.players.filter { it.assignedNumber != 0 }.forEach { player ->
                Text(
                    "${player.player.name} → ${player.assignedNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StarGold
                )
            }
        }
    }
}

@Composable
private fun KillerPlayerCards(gameState: KillerGameState) {
    gameState.players.forEach { player ->
        val isActive = player.player.index == gameState.currentPlayerIndex
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            active = isActive || player.isEliminated,
            accent = if (player.isEliminated) NovaCoral else StarGold,
            contentPadding = PaddingValues(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            player.player.name,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                player.isEliminated -> NovaTextMuted
                                isActive -> StarGold
                                else -> NovaTextPrimary
                            }
                        )
                        if (player.isKiller) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("KILLER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NovaCoral)
                        }
                        if (player.isEliminated) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ÉLIMINÉ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NovaTextMuted)
                        }
                    }
                    Text(
                        "N°${player.assignedNumber}  ·  Kills : ${player.killCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = NovaTextSecondary
                    )
                }
                Row {
                    repeat(player.maxLives) { i ->
                        Icon(
                            if (i < player.lives) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (i < player.lives) NovaCoral else NovaTextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun PlayingPhase(
    gameState: KillerGameState,
    onThrow: (hitNumber: Int, isDouble: Boolean) -> Unit,
    onEndTurn: () -> Unit,
    onUndo: () -> Unit
) {
    var selectedNumber by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Current player info
        Text(
            "Tour de ${gameState.currentPlayer.player.name}",
            style = MaterialTheme.typography.titleLarge,
            color = StarGold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            if (gameState.currentPlayer.isKiller) "KILLER - Visez les doubles adverses !"
            else "Touchez votre double (${gameState.currentPlayer.assignedNumber}) pour gagner des vies",
            style = MaterialTheme.typography.bodyMedium,
            color = if (gameState.currentPlayer.isKiller) NovaRed else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Player status cards
        KillerPlayerCards(gameState)

        Spacer(modifier = Modifier.height(16.dp))

        // Number selection
        Text(
            "Numéro touché :",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))

        (1..20).chunked(5).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { number ->
                    val isSelected = selectedNumber == number
                    OutlinedButton(
                        onClick = { selectedNumber = number },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) StarGold.copy(alpha = 0.3f) else Color.Transparent
                        ),
                        border = BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) StarGold else Color.Gray
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("$number", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Throw buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GhostButton(
                onClick = {
                    if (selectedNumber > 0) {
                        onThrow(selectedNumber, false)
                        selectedNumber = 0
                    }
                },
                enabled = selectedNumber > 0,
                modifier = Modifier.weight(1f).height(54.dp)
            ) {
                Text("Simple", fontWeight = FontWeight.Bold)
            }
            GradientButton(
                onClick = {
                    if (selectedNumber > 0) {
                        onThrow(selectedNumber, true)
                        selectedNumber = 0
                    }
                },
                enabled = selectedNumber > 0,
                gradient = NovaDangerGradient,
                modifier = Modifier.weight(1f).height(54.dp)
            ) {
                Text("DOUBLE", fontWeight = FontWeight.ExtraBold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
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
                Text("Fin du tour", fontWeight = FontWeight.ExtraBold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PlayingPhaseDartByDart(
    gameState: KillerGameState,
    onThrow: (hitNumber: Int, isDouble: Boolean) -> Unit,
    onEndTurn: () -> Unit,
    onUndo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Current player info
        Text(
            "Tour de ${gameState.currentPlayer.player.name}",
            style = MaterialTheme.typography.titleLarge,
            color = StarGold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            if (gameState.currentPlayer.isKiller) "KILLER - Visez les doubles adverses !"
            else "Touchez votre double (${gameState.currentPlayer.assignedNumber}) pour gagner des vies",
            style = MaterialTheme.typography.bodyMedium,
            color = if (gameState.currentPlayer.isKiller) NovaRed else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Player status cards
        KillerPlayerCards(gameState)

        Spacer(modifier = Modifier.height(16.dp))

        DartByDartInput(
            onScoreSubmit = { },
            onUndo = onUndo,
            onDartsSubmit = { darts ->
                darts.forEach { dart ->
                    if (dart.number in 1..20 && dart.multiplier > 0) {
                        onThrow(dart.number, dart.multiplier >= 2)
                    }
                }
                onEndTurn()
            }
        )
    }
}
