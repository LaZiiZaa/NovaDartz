package com.music.dartsscoreboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.dartsscoreboard.data.PlayerGameStats
import com.music.dartsscoreboard.ui.theme.GlassCard
import com.music.dartsscoreboard.ui.theme.GradientButton
import com.music.dartsscoreboard.ui.theme.HairlineDivider
import com.music.dartsscoreboard.ui.theme.NovaCyan
import com.music.dartsscoreboard.ui.theme.NovaGoldGradient
import com.music.dartsscoreboard.ui.theme.NovaTextPrimary
import com.music.dartsscoreboard.ui.theme.NovaTextSecondary
import com.music.dartsscoreboard.ui.theme.StarGold

@Composable
fun GameEndScreen(
    stats: List<PlayerGameStats>,
    onBackHome: () -> Unit
) {
    val winner = stats.firstOrNull { it.isWinner }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        // Trophy medallion with glow
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(StarGold.copy(alpha = 0.35f), Color.Transparent)
                        ),
                        CircleShape
                    )
            )
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(84.dp),
                tint = StarGold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            "VICTOIRE",
            style = TextStyle(
                brush = NovaGoldGradient,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
        )
        if (winner != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                winner.playerName,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NovaTextPrimary
            )
            Text(
                "remporte la partie",
                style = MaterialTheme.typography.bodyMedium,
                color = NovaTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            "Statistiques de la partie",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        stats.forEach { playerStat ->
            PlayerStatCard(playerStat)
            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        GradientButton(
            onClick = onBackHome,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Icon(Icons.Default.Home, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Nouvelle partie", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PlayerStatCard(playerStat: PlayerGameStats) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        active = playerStat.isWinner,
        accent = StarGold
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (playerStat.isWinner) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = StarGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                playerStat.playerName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (playerStat.isWinner) StarGold else NovaTextPrimary
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = HairlineDivider)
        Spacer(modifier = Modifier.height(10.dp))

        playerStat.stats.entries.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                row.forEach { (key, value) ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(key, style = MaterialTheme.typography.labelSmall, color = NovaTextSecondary)
                        Text(
                            value,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = NovaCyan
                        )
                    }
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
