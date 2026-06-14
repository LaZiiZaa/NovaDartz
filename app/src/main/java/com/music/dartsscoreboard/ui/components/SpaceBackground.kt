package com.music.dartsscoreboard.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.music.dartsscoreboard.ui.theme.NovaCyan
import com.music.dartsscoreboard.ui.theme.NovaViolet
import com.music.dartsscoreboard.ui.theme.StarGold
import kotlin.math.sin
import kotlin.random.Random

private data class Star(
    val x: Float,
    val y: Float,
    val size: Float,
    val phaseOffset: Float,
    val speed: Float,
    val baseAlpha: Float,
    val color: Color
)

private data class Nebula(
    val cx: Float,
    val cy: Float,
    val radius: Float,
    val color: Color,
    val phase: Float,
    val baseAlpha: Float
)

/**
 * Premium deep-space nebula backdrop.
 * Deep navy vertical gradient + a few softly breathing nebula glows (violet / cyan / gold)
 * + delicate twinkling stars + a subtle vignette. No cartoon planets — quiet and elegant.
 */
@Composable
fun SpaceBackground() {
    val stars = remember {
        val colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFCCD6FF),
            Color(0xFFBFA9FF),
            StarGold.copy(alpha = 0.9f)
        )
        List(90) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 1.8f + 0.4f,
                phaseOffset = Random.nextFloat() * 6.28f,
                speed = Random.nextFloat() * 1.2f + 0.4f,
                baseAlpha = Random.nextFloat() * 0.4f + 0.25f,
                color = colors.random()
            )
        }
    }

    val nebulae = remember {
        listOf(
            Nebula(0.18f, 0.16f, 0.85f, NovaViolet, 0.0f, 0.22f),
            Nebula(0.86f, 0.30f, 0.70f, NovaCyan, 2.1f, 0.16f),
            Nebula(0.72f, 0.92f, 0.95f, NovaViolet, 4.0f, 0.18f),
            Nebula(0.10f, 0.80f, 0.65f, StarGold, 1.0f, 0.07f)
        )
    }

    val transition = rememberInfiniteTransition(label = "space")
    val twinkle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(5200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "twinkle"
    )
    val breathe by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "breathe"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // --- Base deep-space gradient ---
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0A0E22),
                    Color(0xFF070A18),
                    Color(0xFF04060F)
                )
            )
        )

        // --- Soft breathing nebula glows ---
        nebulae.forEach { neb ->
            val pulse = (sin(breathe + neb.phase) + 1f) / 2f
            val alpha = neb.baseAlpha * (0.6f + 0.4f * pulse)
            val radius = neb.radius * w * (0.95f + 0.05f * pulse)
            val center = Offset(neb.cx * w, neb.cy * h)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        neb.color.copy(alpha = alpha),
                        neb.color.copy(alpha = alpha * 0.35f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }

        // --- Twinkling stars ---
        stars.forEach { star ->
            val flicker = (sin(twinkle * star.speed + star.phaseOffset) + 1f) / 2f
            val alpha = (star.baseAlpha + flicker * 0.55f).coerceIn(0.1f, 1f)
            val center = Offset(star.x * w, star.y * h)
            // faint halo for the larger stars
            if (star.size > 1.3f) {
                drawCircle(
                    color = star.color.copy(alpha = alpha * 0.18f),
                    radius = star.size * density * 3.2f,
                    center = center
                )
            }
            drawCircle(
                color = star.color.copy(alpha = alpha),
                radius = star.size * density,
                center = center
            )
        }

        // --- Vignette for depth & focus ---
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color(0x66000000)),
                center = Offset(w / 2f, h * 0.42f),
                radius = maxOf(w, h) * 0.85f
            ),
            size = Size(w, h)
        )
    }
}
