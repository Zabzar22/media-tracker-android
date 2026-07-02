package edu.metrostate.ics342.mediatracker.theme

import androidx.compose.ui.graphics.Color

// Palette from the Media Tracker wireframes — Material 3, "Indigo + Pink + Amber".

// Primary — indigo
val Primary            = Color(0xFF6366F1)
val OnPrimary          = Color(0xFFFFFFFF)
val PrimaryContainer   = Color(0xFFE0E0FF)
val OnPrimaryContainer = Color(0xFF3730A3)

// Secondary — pink
val Secondary            = Color(0xFFDB2777)
val OnSecondary          = Color(0xFFFFFFFF)
val SecondaryContainer   = Color(0xFFFCE7F3)
val OnSecondaryContainer = Color(0xFF9D174D)

// Tertiary — amber
val Tertiary            = Color(0xFFD97706)
val OnTertiary          = Color(0xFFFFFFFF)
val TertiaryContainer   = Color(0xFFFEF3C7)
val OnTertiaryContainer = Color(0xFF78350F)

// Neutral surfaces
val Background      = Color(0xFFF1F5F9)
val OnBackground    = Color(0xFF0F172A)
val Surface         = Color(0xFFFFFFFF)
val OnSurface       = Color(0xFF0F172A)
val SurfaceVariant  = Color(0xFFF8FAFC)
val OnSurfaceVariant = Color(0xFF64748B)
val Outline         = Color(0xFFCBD5E1)
val OutlineVariant  = Color(0xFFE8EDF2)

// Saturated accent colors from the wireframe design system (want-to / in-progress / finished).
val WantTo     = Color(0xFF7C3AED)
val InProgress = Color(0xFF2563EB)
val Finished   = Color(0xFF059669)

// Avatar palette — the darker/saturated brand colors. Each user gets a stable color
// from this set (all take white text).
val AvatarColors = listOf(Primary, Secondary, Tertiary, WantTo, InProgress, Finished)

/** Deterministic avatar color for a user key, so the same person is always the same color. */
fun avatarColor(key: String): Color =
    AvatarColors[(key.hashCode() and Int.MAX_VALUE) % AvatarColors.size]

// Dark theme
val DarkBackground       = Color(0xFF0F0F1A)
val DarkSurface          = Color(0xFF1A1A2E)
val DarkOnSurface        = Color(0xFFE8E8F0)
val DarkPrimary          = Color(0xFF818CF8)
val DarkOnPrimary        = Color(0xFF1E1B4B)
val DarkPrimaryContainer = Color(0xFF2D2B6E)
val DarkOnPrimaryContainer = Color(0xFFE0E7FF)
