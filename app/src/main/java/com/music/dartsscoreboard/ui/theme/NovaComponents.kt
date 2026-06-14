package com.music.dartsscoreboard.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Frosted-glass surface: translucent gradient fill + light hairline border.
 * Reads as premium "glass" over the animated nebula background.
 */
fun Modifier.glassSurface(
    shape: Shape,
    fill: Brush = Brush.verticalGradient(listOf(GlassFillTop, GlassFillBottom)),
    borderTop: Color = GlassBorderTop,
    borderBottom: Color = GlassBorderBottom,
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(shape)
    .background(fill, shape)
    .border(borderWidth, Brush.verticalGradient(listOf(borderTop, borderBottom)), shape)

/**
 * A glassmorphism card. When [active] it gains an accent-tinted fill + border glow.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    active: Boolean = false,
    accent: Color = NovaCyan,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    val fill = if (active) {
        Brush.verticalGradient(listOf(accent.copy(alpha = 0.20f), Color(0x0AFFFFFF)))
    } else {
        Brush.verticalGradient(listOf(GlassFillTop, GlassFillBottom))
    }
    Box(
        modifier.glassSurface(
            shape = shape,
            fill = fill,
            borderTop = if (active) accent.copy(alpha = 0.85f) else GlassBorderTop,
            borderBottom = if (active) accent.copy(alpha = 0.20f) else GlassBorderBottom,
            borderWidth = if (active) 1.5.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            horizontalAlignment = horizontalAlignment,
            content = content
        )
    }
}

/**
 * Primary call-to-action button with a signature gradient fill.
 */
@Composable
fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradient: Brush = NovaPrimaryGradient,
    shape: Shape = RoundedCornerShape(16.dp),
    contentColor: Color = Color.White,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
    content: @Composable RowScope.() -> Unit
) {
    val resolvedContentColor = if (enabled) contentColor else NovaTextMuted
    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (enabled) Modifier.background(gradient, shape)
                else Modifier.background(Color.White.copy(alpha = 0.06f), shape)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides resolvedContentColor) {
            Row(
                modifier = Modifier.padding(contentPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                content = content
            )
        }
    }
}

/**
 * Ghost / secondary button: hairline glass outline, no fill.
 */
@Composable
fun GhostButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: Color = NovaTextPrimary,
    shape: Shape = RoundedCornerShape(16.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit
) {
    val tint = if (enabled) accent else NovaTextMuted
    Box(
        modifier = modifier
            .clip(shape)
            .background(Color.White.copy(alpha = 0.04f), shape)
            .border(1.dp, GlassStroke, shape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides tint) {
            Row(
                modifier = Modifier.padding(contentPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                content = content
            )
        }
    }
}

/**
 * Small section title with an accent tick on the left — used to head sections.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    accent: Color = NovaCyan
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accent)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

/** Hairline accent outline used for chips / outlined elements. */
fun glassBorder(width: Dp = 1.dp) = BorderStroke(width, GlassStroke)
