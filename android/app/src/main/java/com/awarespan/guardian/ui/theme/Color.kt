package com.awarespan.guardian.ui.theme

import androidx.compose.ui.graphics.Color

// ─── AWARESPAN Core Design Tokens (from design.md) ───────────────────────────
// Dark infrastructure at night — pure black void, white light, ignited ember.

val EmberOrange = Color(0xFFFF4F2B)      // Primary actions, selected states, critical emphasis
val VoidBlack = Color(0xFF000000)        // Application canvas, primary background
val Carbon = Color(0xFF1A1A1A)           // Secondary surfaces, input backgrounds, cards
val GraphiteBorder = Color(0xFF3C3C3C)   // Hairline dividers, borders, section separators
val SteelMid = Color(0xFF737373)         // Secondary metadata, muted labels, timestamps
val FogLight = Color(0xFFBFBFBF)         // Secondary body text, card outlines
val BoneWhite = Color(0xFFF5F5F5)        // Primary text, headlines, icons

// ─── Operational Functional Status Colors ────────────────────────────────────
// Semantically consistent indicators for operational readiness and severity.

val StatusCritical = Color(0xFFFF4F2B)   // Critical threats, fatal faults, urgent alarms
val StatusHigh = Color(0xFFFF6B4A)       // High severity
val StatusWarning = Color(0xFFF5A623)    // Medium severity, warning, degraded state
val StatusSafe = Color(0xFF2ECC71)       // Safe, verified, online, synchronized
val StatusInfo = Color(0xFF3498DB)       // Informational, low threat
val StatusMuted = Color(0xFF737373)      // Unknown, neutral, pending

val NodeOnline = StatusSafe
val NodeDegraded = StatusWarning
val NodeOffline = StatusCritical

val ThreatLow = StatusInfo
val ThreatMedium = StatusWarning
val ThreatHigh = StatusHigh
val ThreatCritical = StatusCritical
