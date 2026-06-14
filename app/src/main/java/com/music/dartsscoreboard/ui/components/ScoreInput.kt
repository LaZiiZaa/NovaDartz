package com.music.dartsscoreboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.music.dartsscoreboard.ui.theme.GlassStroke
import com.music.dartsscoreboard.ui.theme.GradientButton
import com.music.dartsscoreboard.ui.theme.GhostButton
import com.music.dartsscoreboard.ui.theme.NovaCoral
import com.music.dartsscoreboard.ui.theme.NovaCyan
import com.music.dartsscoreboard.ui.theme.NovaTextPrimary
import com.music.dartsscoreboard.ui.theme.NovaTextSecondary
import com.music.dartsscoreboard.ui.theme.glassSurface

@Composable
fun ScoreInput(
    onScoreSubmit: (Int) -> Unit,
    onUndo: () -> Unit,
    maxScore: Int = 180,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .glassSurface(RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = inputText.ifEmpty { "0" },
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = if (inputText.isEmpty()) NovaTextSecondary else NovaTextPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Numpad
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("C", "0", "DEL")
        )

        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { label ->
                    KeypadKey(
                        label = label,
                        modifier = Modifier.weight(1f),
                        accent = when (label) {
                            "C" -> NovaCoral
                            "DEL" -> NovaTextSecondary
                            else -> null
                        },
                        onClick = {
                            when (label) {
                                "C" -> inputText = ""
                                "DEL" -> if (inputText.isNotEmpty()) inputText = inputText.dropLast(1)
                                else -> {
                                    val newText = inputText + label
                                    if ((newText.toIntOrNull() ?: 0) <= maxScore) inputText = newText
                                }
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Quick scores
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(26, 41, 45, 60, 100, 180).forEach { score ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NovaCyan.copy(alpha = 0.10f))
                        .border(1.dp, NovaCyan.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .clickable { inputText = score.toString() }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$score", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NovaCyan)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Submit & Undo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GhostButton(
                onClick = onUndo,
                modifier = Modifier.weight(1f).height(54.dp)
            ) {
                Text("Annuler", fontWeight = FontWeight.Bold)
            }
            GradientButton(
                onClick = {
                    onScoreSubmit(inputText.toIntOrNull() ?: 0)
                    inputText = ""
                },
                modifier = Modifier.weight(2f).height(54.dp)
            ) {
                Text("Valider", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun KeypadKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color? = null
) {
    val shape = RoundedCornerShape(14.dp)
    val fill = when (accent) {
        NovaCoral -> Brush.verticalGradient(listOf(NovaCoral.copy(alpha = 0.22f), NovaCoral.copy(alpha = 0.10f)))
        else -> Brush.verticalGradient(listOf(Color(0x1FFFFFFF), Color(0x0AFFFFFF)))
    }
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(shape)
            .background(fill, shape)
            .border(
                1.dp,
                if (accent == NovaCoral) NovaCoral.copy(alpha = 0.5f) else GlassStroke,
                shape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = if (label.length > 1) 16.sp else 22.sp,
            fontWeight = FontWeight.Bold,
            color = when (accent) {
                NovaCoral -> NovaCoral
                NovaTextSecondary -> NovaTextSecondary
                else -> NovaTextPrimary
            }
        )
    }
}
