package com.awarespan.guardian.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// ─── Zero-Radius Architecture (from design.md) ───────────────────────────────
// All interactive elements, cards, inputs, and badges use 0px / 0dp border radius.
// Sharp, rectangular geometry is structural to the data-center / operations aesthetic.

val AwarespanShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp),
)

// Dark infrastructure canvas
private val AwarespanColorScheme = darkColorScheme(
    primary = EmberOrange,
    onPrimary = VoidBlack,
    primaryContainer = Carbon,
    onPrimaryContainer = BoneWhite,
    secondary = BoneWhite,
    onSecondary = VoidBlack,
    tertiary = FogLight,
    onTertiary = VoidBlack,
    background = VoidBlack,
    onBackground = BoneWhite,
    surface = Carbon,
    onSurface = BoneWhite,
    surfaceVariant = Carbon,
    onSurfaceVariant = FogLight,
    outline = GraphiteBorder,
    outlineVariant = GraphiteBorder,
    error = StatusCritical,
    onError = VoidBlack,
)

@Composable
fun AwarespanTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = AwarespanColorScheme,
        shapes = AwarespanShapes,
        typography = AwarespanTypography,
        content = content,
    )
}

/** Backwards-compatible alias for existing callers */
@Composable
fun SIPGuardianTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    AwarespanTheme(content = content)
}
