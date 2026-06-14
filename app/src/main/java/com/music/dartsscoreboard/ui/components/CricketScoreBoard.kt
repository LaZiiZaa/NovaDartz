package com.music.dartsscoreboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.dartsscoreboard.model.CricketPlayerState
import com.music.dartsscoreboard.model.cricketNumbers
import com.music.dartsscoreboard.ui.theme.GlassStroke
import com.music.dartsscoreboard.ui.theme.NovaCyan
import com.music.dartsscoreboard.ui.theme.NovaTextMuted
import com.music.dartsscoreboard.ui.theme.NovaTextPrimary
import com.music.dartsscoreboard.ui.theme.NovaTextSecondary
import com.music.dartsscoreboard.ui.theme.NovaViolet
import com.music.dartsscoreboard.ui.theme.StarGold

// Largeur reservee au groupe de boutons x1/x2/x3 sur chaque ligne.
private val InputColumnWidth = 132.dp
private val NumberLabelWidth = 40.dp

@Composable
fun CricketScoreBoard(
    players: List<CricketPlayerState>,
    currentPlayerIndex: Int,
    onNumberClick: (number: Int, multiplier: Int) -> Unit,
    modifier: Modifier = Modifier,
    isTeamMode: Boolean = false,
    inputEnabled: Boolean = true
) {
    data class TeamColumn(
        val names: String,
        val player: CricketPlayerState,
        val isActive: Boolean,
        val activePlayerName: String?
    )

    val columns: List<TeamColumn> = if (isTeamMode) {
        val teams = players.groupBy { it.player.teamIndex }
        teams.map { (_, members) ->
            val isActive = members.any { it.player.index == currentPlayerIndex }
            val activePlayer = members.find { it.player.index == currentPlayerIndex }
            TeamColumn(
                names = members.joinToString(" & ") { it.player.name },
                player = members.first(),
                isActive = isActive,
                activePlayerName = activePlayer?.player?.name
            )
        }
    } else {
        players.map { p ->
            TeamColumn(
                names = p.player.name,
                player = p,
                isActive = p.player.index == currentPlayerIndex,
                activePlayerName = null
            )
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Header: player/team names + scores
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Spacer(modifier = Modifier.width(NumberLabelWidth))
            columns.forEach { col ->
                val shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                val fill = if (col.isActive) {
                    Brush.verticalGradient(listOf(StarGold.copy(alpha = 0.22f), Color(0x0AFFFFFF)))
                } else {
                    Brush.verticalGradient(listOf(Color(0x14FFFFFF), Color(0x06FFFFFF)))
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                        .clip(shape)
                        .background(fill, shape)
                        .border(
                            1.dp,
                            if (col.isActive) StarGold.copy(alpha = 0.7f) else GlassStroke,
                            shape
                        )
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = col.names,
                        fontSize = if (isTeamMode) 12.sp else 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (col.isActive) StarGold else NovaTextPrimary,
                        maxLines = 1
                    )
                    if (isTeamMode && col.activePlayerName != null && col.isActive) {
                        Text("▶ ${col.activePlayerName}", fontSize = 10.sp, color = StarGold.copy(alpha = 0.85f), maxLines = 1)
                    }
                    Text("${col.player.points}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = NovaTextPrimary)
                }
            }
            Spacer(modifier = Modifier.width(InputColumnWidth))
        }

        // Score grid
        cricketNumbers.forEach { number ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (number == 25) "Bull" else "$number",
                    modifier = Modifier.width(NumberLabelWidth),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = NovaTextSecondary
                )

                columns.forEach { col ->
                    val marks = col.player.marks[number] ?: 0
                    Text(
                        text = when {
                            marks >= 3 -> "⊗"
                            marks == 2 -> "✕"
                            marks == 1 -> "/"
                            else -> "–"
                        },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            marks >= 3 -> NovaCyan
                            marks > 0 -> NovaTextPrimary
                            else -> NovaTextMuted
                        },
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }

                MultiplierButtons(number = number, enabled = inputEnabled, onNumberClick = onNumberClick)
            }
        }
    }
}

@Composable
private fun MultiplierButtons(
    number: Int,
    enabled: Boolean,
    onNumberClick: (Int, Int) -> Unit
) {
    val multipliers = if (number == 25) listOf(1, 2) else listOf(1, 2, 3)
    Row(
        modifier = Modifier.width(InputColumnWidth),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        multipliers.forEach { mult ->
            val accent = when (mult) {
                1 -> NovaTextSecondary
                2 -> NovaCyan
                else -> NovaViolet
            }
            val shape = RoundedCornerShape(10.dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(shape)
                    .background(
                        Brush.verticalGradient(
                            listOf(accent.copy(alpha = if (enabled) 0.22f else 0.06f), accent.copy(alpha = if (enabled) 0.08f else 0.03f))
                        ),
                        shape
                    )
                    .border(1.dp, accent.copy(alpha = if (enabled) 0.55f else 0.2f), shape)
                    .clickable(enabled = enabled) { onNumberClick(number, mult) },
                contentAlignment = Alignment.Center
            ) {
                Text("x$mult", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (enabled) NovaTextPrimary else NovaTextMuted)
            }
        }
        if (number == 25) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
