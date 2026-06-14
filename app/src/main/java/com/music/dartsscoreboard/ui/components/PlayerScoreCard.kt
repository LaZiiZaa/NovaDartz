package com.music.dartsscoreboard.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.dartsscoreboard.model.X01PlayerState
import com.music.dartsscoreboard.ui.theme.GlassCard
import com.music.dartsscoreboard.ui.theme.NovaCyan
import com.music.dartsscoreboard.ui.theme.NovaTextPrimary
import com.music.dartsscoreboard.ui.theme.NovaTextSecondary
import com.music.dartsscoreboard.ui.theme.StarGold

@Composable
fun PlayerScoreCard(
    playerState: X01PlayerState,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    checkoutHint: String? = null,
    teammateName: String? = null,
    activePlayerName: String? = null
) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        active = isActive,
        accent = StarGold,
        contentPadding = PaddingValues(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (teammateName != null) {
            Text(
                text = "${playerState.player.name} & $teammateName",
                style = MaterialTheme.typography.labelLarge,
                color = if (isActive) StarGold else NovaTextPrimary,
                maxLines = 1
            )
            if (activePlayerName != null && isActive) {
                Text(
                    text = "▶ $activePlayerName",
                    style = MaterialTheme.typography.labelSmall,
                    color = StarGold.copy(alpha = 0.85f)
                )
            }
        } else {
            Text(
                text = playerState.player.name,
                style = MaterialTheme.typography.labelLarge,
                color = if (isActive) StarGold else NovaTextPrimary,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "${playerState.scoreRemaining}",
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            color = NovaTextPrimary
        )

        if (checkoutHint != null) {
            Text(
                text = checkoutHint,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NovaCyan,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatPill("Moy.", "%.1f".format(playerState.average))
            StatPill("Best", "${playerState.bestThrow}")
            StatPill("Darts", "${playerState.dartsThrown}")
        }
    }
}

@Composable
private fun StatPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = NovaTextSecondary
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = NovaTextPrimary
        )
    }
}
