package com.music.dartsscoreboard.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.dartsscoreboard.R
import com.music.dartsscoreboard.ui.theme.GlassStroke
import com.music.dartsscoreboard.ui.theme.GradientButton
import com.music.dartsscoreboard.ui.theme.NovaCyan
import com.music.dartsscoreboard.ui.theme.NovaPrimaryGradient
import com.music.dartsscoreboard.ui.theme.NovaTextMuted
import com.music.dartsscoreboard.ui.theme.NovaTextSecondary
import com.music.dartsscoreboard.ui.theme.NovaViolet

@Composable
fun WelcomeScreen(
    onPlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo medallion with soft glow + glass ring
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                NovaViolet.copy(alpha = 0.30f),
                                NovaCyan.copy(alpha = 0.10f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(168.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0x1FFFFFFF), Color(0x08FFFFFF))
                        )
                    )
                    .border(1.dp, GlassStroke, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.novadartz_logo),
                    contentDescription = "NovaDartz",
                    modifier = Modifier.size(118.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            "BIENVENUE",
            style = MaterialTheme.typography.labelMedium,
            color = NovaTextMuted,
            letterSpacing = 4.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            "NovaDartz",
            style = TextStyle(
                brush = NovaPrimaryGradient,
                fontSize = 46.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            "L'application privée de scoreboard\npour vos parties de fléchettes.",
            style = MaterialTheme.typography.bodyLarge,
            color = NovaTextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        GradientButton(
            onClick = onPlay,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            gradient = NovaPrimaryGradient
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Jouer", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            "Développé par Julien V.",
            style = MaterialTheme.typography.labelSmall,
            color = NovaTextMuted,
            textAlign = TextAlign.Center
        )
    }
}
