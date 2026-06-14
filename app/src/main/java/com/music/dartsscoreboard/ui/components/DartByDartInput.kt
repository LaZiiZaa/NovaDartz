package com.music.dartsscoreboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.dartsscoreboard.ui.theme.GlassStroke
import com.music.dartsscoreboard.ui.theme.GhostButton
import com.music.dartsscoreboard.ui.theme.GradientButton
import com.music.dartsscoreboard.ui.theme.NovaCoral
import com.music.dartsscoreboard.ui.theme.NovaCyan
import com.music.dartsscoreboard.ui.theme.NovaTextPrimary
import com.music.dartsscoreboard.ui.theme.NovaTextSecondary
import com.music.dartsscoreboard.ui.theme.NovaViolet
import com.music.dartsscoreboard.ui.theme.StarGold
import com.music.dartsscoreboard.ui.theme.glassSurface

data class DartThrow(val number: Int, val multiplier: Int) {
    val score: Int get() = number * multiplier
    val label: String
        get() = when {
            number == 0 -> "Miss"
            number == 25 && multiplier == 2 -> "D-Bull"
            number == 25 -> "Bull"
            multiplier == 3 -> "T$number"
            multiplier == 2 -> "D$number"
            else -> "$number"
        }
}

@Composable
fun DartByDartInput(
    onScoreSubmit: (Int) -> Unit,
    onUndo: () -> Unit,
    modifier: Modifier = Modifier,
    onDartsSubmit: ((List<DartThrow>) -> Unit)? = null
) {
    var darts by remember { mutableStateOf(listOf<DartThrow>()) }
    var selectedNumber by remember { mutableIntStateOf(-1) }

    val totalScore = darts.sumOf { it.score }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Current turn summary
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .glassSurface(RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (0 until 3).forEach { i ->
                        val active = i == darts.size
                        val filled = i < darts.size
                        Box(
                            modifier = Modifier
                                .size(width = 84.dp, height = 50.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        filled -> NovaCyan.copy(alpha = 0.20f)
                                        active -> StarGold.copy(alpha = 0.14f)
                                        else -> Color.White.copy(alpha = 0.04f)
                                    },
                                    RoundedCornerShape(10.dp)
                                )
                                .border(
                                    1.dp,
                                    when {
                                        active -> StarGold
                                        filled -> NovaCyan.copy(alpha = 0.5f)
                                        else -> GlassStroke
                                    },
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (filled) {
                                Text(darts[i].label, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NovaTextPrimary)
                            } else {
                                Text("Dart ${i + 1}", fontSize = 12.sp, color = NovaTextSecondary)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Total : $totalScore", fontSize = 28.sp, fontWeight = FontWeight.Black, color = NovaTextPrimary)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (darts.size < 3) {
            if (selectedNumber == -1) {
                NumberSelectionGrid(onNumberSelected = { selectedNumber = it })
            } else {
                MultiplierSelection(
                    number = selectedNumber,
                    onMultiplierSelected = { multiplier ->
                        darts = darts + DartThrow(selectedNumber, multiplier)
                        selectedNumber = -1
                    },
                    onCancel = { selectedNumber = -1 }
                )
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            Text("3 fléchettes lancées !", style = MaterialTheme.typography.titleMedium, color = StarGold, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GhostButton(
                onClick = onUndo,
                modifier = Modifier.weight(1f).height(48.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Annuler", fontSize = 12.sp)
            }
            GhostButton(
                onClick = {
                    if (darts.isNotEmpty()) darts = darts.dropLast(1)
                    selectedNumber = -1
                },
                enabled = darts.isNotEmpty(),
                modifier = Modifier.weight(1f).height(48.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Retirer", fontSize = 12.sp)
            }
            GradientButton(
                onClick = {
                    if (onDartsSubmit != null) onDartsSubmit(darts.toList()) else onScoreSubmit(totalScore)
                    darts = emptyList()
                    selectedNumber = -1
                },
                enabled = darts.isNotEmpty(),
                modifier = Modifier.weight(1.5f).height(48.dp)
            ) {
                Text("Valider", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun NumberSelectionGrid(onNumberSelected: (Int) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        DartKey(
            label = "MISS (0)",
            onClick = { onNumberSelected(0) },
            modifier = Modifier.fillMaxWidth().height(40.dp),
            accent = NovaCoral
        )
        Spacer(modifier = Modifier.height(6.dp))

        (1..20).chunked(5).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { number ->
                    DartKey(
                        label = "$number",
                        onClick = { onNumberSelected(number) },
                        modifier = Modifier.weight(1f).height(48.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        DartKey(
            label = "BULL (25)",
            onClick = { onNumberSelected(25) },
            modifier = Modifier.fillMaxWidth().height(44.dp),
            accent = NovaCoral
        )
    }
}

@Composable
private fun DartKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color? = null
) {
    val shape = RoundedCornerShape(12.dp)
    val fill = if (accent != null) {
        Brush.verticalGradient(listOf(accent.copy(alpha = 0.24f), accent.copy(alpha = 0.10f)))
    } else {
        Brush.verticalGradient(listOf(Color(0x1FFFFFFF), Color(0x0AFFFFFF)))
    }
    Box(
        modifier = modifier
            .clip(shape)
            .background(fill, shape)
            .border(1.dp, accent?.copy(alpha = 0.5f) ?: GlassStroke, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontSize = if (label.length > 2) 15.sp else 18.sp,
            fontWeight = FontWeight.Bold,
            color = accent ?: NovaTextPrimary
        )
    }
}

@Composable
private fun MultiplierSelection(
    number: Int,
    onMultiplierSelected: (Int) -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            if (number == 25) "Bull" else "Numéro $number",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = StarGold
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (number == 0) {
            LaunchedEffect(Unit) { onMultiplierSelected(0) }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MultiplierCard("Simple", number * 1, NovaTextSecondary, Modifier.weight(1f)) { onMultiplierSelected(1) }
                MultiplierCard("Double", number * 2, NovaCyan, Modifier.weight(1f)) { onMultiplierSelected(2) }
                if (number != 25) {
                    MultiplierCard("Triple", number * 3, NovaViolet, Modifier.weight(1f)) { onMultiplierSelected(3) }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Changer de numéro",
                style = MaterialTheme.typography.labelMedium,
                color = NovaTextSecondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onCancel)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun MultiplierCard(
    label: String,
    value: Int,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape: Shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .height(66.dp)
            .clip(shape)
            .background(
                Brush.verticalGradient(listOf(accent.copy(alpha = 0.20f), accent.copy(alpha = 0.08f))),
                shape
            )
            .border(1.dp, accent.copy(alpha = 0.5f), shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NovaTextSecondary)
            Text("$value", fontSize = 20.sp, fontWeight = FontWeight.Black, color = accent)
        }
    }
}
