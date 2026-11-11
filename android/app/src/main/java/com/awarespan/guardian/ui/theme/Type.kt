package com.awarespan.guardian.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─── AWARESPAN Typography Hierarchy (from design.md) ─────────────────────────
// Display & headings: Architectural light-weight sans-serif (Everett fallback)
// Body: Clean geometric grotesque (Switzer fallback)
// Labels & Metadata: Monospaced terminal readouts (Chivo Mono fallback)

val FontFamilyDisplay = FontFamily.SansSerif
val FontFamilyBody = FontFamily.SansSerif
val FontFamilyMono = FontFamily.Monospace

val AwarespanTypography = Typography(
    // Hero / Large display (adapted for mobile)
    displayLarge = TextStyle(
        fontFamily = FontFamilyDisplay,
        fontWeight = FontWeight.Light,
        fontSize = 36.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp,
        color = BoneWhite,
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamilyDisplay,
        fontWeight = FontWeight.Light,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.3).sp,
        color = BoneWhite,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamilyDisplay,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.2).sp,
        color = BoneWhite,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamilyDisplay,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = BoneWhite,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamilyDisplay,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = BoneWhite,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamilyDisplay,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = BoneWhite,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamilyBody,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = BoneWhite,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamilyMono,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = FogLight,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamilyBody,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = BoneWhite,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamilyBody,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = FogLight,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamilyBody,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = SteelMid,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamilyMono,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.8.sp,
        color = BoneWhite,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamilyMono,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.6.sp,
        color = FogLight,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamilyMono,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.5.sp,
        color = SteelMid,
    ),
)
