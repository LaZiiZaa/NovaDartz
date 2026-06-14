package com.music.dartsscoreboard.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.music.dartsscoreboard.data.GameHistoryEntity
import com.music.dartsscoreboard.data.PlayerGameStats
import com.music.dartsscoreboard.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    games: List<GameHistoryEntity>,
    onDelete: (GameHistoryEntity) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Historique", fontWeight = FontWeight.ExtraBold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        if (games.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(64.dp), tint = NovaTextMuted)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Aucune partie enregistrée", style = MaterialTheme.typography.titleMedium, color = NovaTextSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(games, key = { it.id }) { game ->
                    GameHistoryCard(game = game, onDelete = { onDelete(game) })
                }
            }
        }
    }
}

@Composable
private fun GameHistoryCard(
    game: GameHistoryEntity,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE) }
    val playerNames: List<String> = remember(game.playerNames) {
        try {
            Gson().fromJson(game.playerNames, object : TypeToken<List<String>>() {}.type)
        } catch (_: Exception) {
            listOf("?")
        }
    }
    val playerStats: List<PlayerGameStats> = remember(game.playerStats) {
        try {
            if (game.playerStats.isNotBlank()) {
                Gson().fromJson(game.playerStats, object : TypeToken<List<PlayerGameStats>>() {}.type)
            } else emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = game.gameType, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = NovaTextPrimary)
                    if (game.startScore > 0) {
                        Text(text = " (${game.startScore})", color = NovaTextSecondary, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = StarGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = game.winnerName, color = StarGold, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = playerNames.joinToString(", "), style = MaterialTheme.typography.bodySmall, color = NovaTextSecondary)
                Text(
                    text = "${dateFormat.format(Date(game.date))} · ${game.durationMinutes} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = NovaTextMuted
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = NovaCoral.copy(alpha = 0.8f))
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Détails",
                    tint = NovaTextSecondary
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            if (playerStats.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = HairlineDivider)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Statistiques de la partie", style = MaterialTheme.typography.labelLarge, color = NovaCyan)
                    Spacer(modifier = Modifier.height(8.dp))
                    playerStats.forEach { ps ->
                        Text(
                            "${ps.playerName}${if (ps.isWinner) " 🏆" else ""}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (ps.isWinner) StarGold else NovaTextPrimary
                        )
                        ps.stats.forEach { (key, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(key, style = MaterialTheme.typography.bodySmall, color = NovaTextSecondary)
                                Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = NovaTextPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            } else {
                Text(
                    "Pas de stats détaillées pour cette partie",
                    style = MaterialTheme.typography.bodySmall,
                    color = NovaTextMuted,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
