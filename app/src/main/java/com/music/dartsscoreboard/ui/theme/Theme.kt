package com.music.dartsscoreboard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/* -------------------------------------------------------------------------- */
/*  NovaDartz — "Nova Premium" design tokens                                  */
/*  Deep-space navy, subtle nebula accents, glassmorphism. Refined & modern.  */
/* -------------------------------------------------------------------------- */

// --- Core surfaces (deep space navy) ---
val SpaceBlack = Color(0xFF05070F)      // app background base
val SpaceDeep = Color(0xFF080B1A)       // gradient anchor
val SpaceSurface = Color(0xFF0C1022)    // elevated surface
val SpaceCard = Color(0xFF161C3A)       // solid card / contrast fill

// --- Accents ---
val StarGold = Color(0xFFF4C56A)        // primary highlight: winner, active, gold
val NovaGold = StarGold
val NovaCyan = Color(0xFF45D4EC)        // interactive / CTA accent
val NovaBlue = NovaCyan                 // kept for backward compat (now premium cyan)
val NovaViolet = Color(0xFF8E7BFF)      // nebula violet
val NovaPurple = NovaViolet             // kept for backward compat
val NovaCoral = Color(0xFFFF6B7A)       // destructive / miss
val NovaRed = NovaCoral                 // kept for backward compat
val NovaGreen = Color(0xFF4ADE80)       // success

// --- Text ---
val NovaTextPrimary = Color(0xFFEDF0FB)
val NovaTextSecondary = Color(0xFF99A0C2)
val NovaTextMuted = Color(0xFF5B638A)
val NovaWhite = NovaTextPrimary         // kept for backward compat

// --- Glass tokens ---
val GlassFillTop = Color(0x1FFFFFFF)
val GlassFillBottom = Color(0x0AFFFFFF)
val GlassBorderTop = Color(0x3DFFFFFF)
val GlassBorderBottom = Color(0x12FFFFFF)
val GlassStroke = Color(0x24FFFFFF)
val HairlineDivider = Color(0x14FFFFFF)

// --- Signature gradients ---
val NovaPrimaryGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF45D4EC), Color(0xFF8E7BFF))
)
val NovaGoldGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFF8D27E), Color(0xFFE49B3A))
)
val NovaDangerGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFF7E8A), Color(0xFFE0455C))
)

private val NovaDartzColorScheme = darkColorScheme(
    primary = NovaCyan,
    onPrimary = Color(0xFF04121A),
    primaryContainer = NovaCyan.copy(alpha = 0.18f),
    onPrimaryContainer = NovaCyan,
    secondary = NovaViolet,
    onSecondary = Color.White,
    secondaryContainer = NovaViolet.copy(alpha = 0.18f),
    onSecondaryContainer = NovaViolet,
    tertiary = StarGold,
    onTertiary = Color(0xFF1A1304),
    background = SpaceBlack,
    onBackground = NovaTextPrimary,
    surface = SpaceSurface,
    onSurface = NovaTextPrimary,
    surfaceVariant = SpaceCard,
    onSurfaceVariant = NovaTextSecondary,
    outline = GlassStroke,
    outlineVariant = HairlineDivider,
    error = NovaCoral,
    onError = Color(0xFF240207)
)

private val Sans = FontFamily.SansSerif

private val NovaDartzTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Black,
        fontSize = 34.sp,
        letterSpacing = (-0.5).sp,
        color = NovaTextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 25.sp,
        letterSpacing = (-0.25).sp,
        color = NovaTextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 0.sp,
        color = NovaTextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        letterSpacing = 0.1.sp,
        color = NovaTextPrimary
    ),
    titleSmall = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.1.sp,
        color = NovaTextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = Sans,
        fontSize = 16.sp,
        letterSpacing = 0.15.sp,
        color = NovaTextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = Sans,
        fontSize = 14.sp,
        letterSpacing = 0.15.sp,
        color = NovaTextPrimary
    ),
    bodySmall = TextStyle(
        fontFamily = Sans,
        fontSize = 12.sp,
        letterSpacing = 0.2.sp,
        color = NovaTextSecondary
    ),
    labelLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        letterSpacing = 0.3.sp,
        color = NovaTextPrimary
    ),
    labelMedium = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        letterSpacing = 0.6.sp,
        color = NovaTextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.8.sp,
        color = NovaTextSecondary
    )
)

@Composable
fun NovaDartzTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NovaDartzColorScheme,
        typography = NovaDartzTypography,
        content = content
    )
}
