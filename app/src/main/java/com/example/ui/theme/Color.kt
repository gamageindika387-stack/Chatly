package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary Brand Palette: Deep Blue + Violet
val PulseVioletPrimary = Color(0xFF6366F1)
val PulseVioletDark = Color(0xFF4F46E5)
val PulseVioletLight = Color(0xFF818CF8)
val PulseDeepNavy = Color(0xFF0F172A)
val PulseMidnight = Color(0xFF080D1A)
val PulseSlate = Color(0xFF1E293B)

// Accent Colors
val PulseCyanAccent = Color(0xFF06B6D4)
val PulseSkyBlue = Color(0xFF38BDF8)
val PulseEmeraldGreen = Color(0xFF10B981)
val PulseRosePink = Color(0xFFF43F5E)
val PulseAmberOrange = Color(0xFFF59E0B)

// Light Theme Tokens
val LightPrimary = Color(0xFF5346E0)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFE0E0FF)
val LightOnPrimaryContainer = Color(0xFF0C006B)

val LightSecondary = Color(0xFF0284C7)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFE0F2FE)
val LightOnSecondaryContainer = Color(0xFF003554)

val LightTertiary = Color(0xFFD946EF)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFFFD7F5)
val LightOnTertiaryContainer = Color(0xFF37003B)

val LightBackground = Color(0xFFF8FAFC)
val LightOnBackground = Color(0xFF0F172A)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF0F172A)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightOnSurfaceVariant = Color(0xFF475569)
val LightOutline = Color(0xFFCBD5E1)

// Dark Theme Tokens
val DarkPrimary = Color(0xFF818CF8)
val DarkOnPrimary = Color(0xFF140D5E)
val DarkPrimaryContainer = Color(0xFF3730A3)
val DarkOnPrimaryContainer = Color(0xFFE0E0FF)

val DarkSecondary = Color(0xFF38BDF8)
val DarkOnSecondary = Color(0xFF003554)
val DarkSecondaryContainer = Color(0xFF075985)
val DarkOnSecondaryContainer = Color(0xFFE0F2FE)

val DarkTertiary = Color(0xFFF472B6)
val DarkOnTertiary = Color(0xFF500045)
val DarkTertiaryContainer = Color(0xFF700062)
val DarkOnTertiaryContainer = Color(0xFFFFD7F5)

val DarkBackground = Color(0xFF0B0F19)
val DarkOnBackground = Color(0xFFF8FAFC)
val DarkSurface = Color(0xFF111827)
val DarkOnSurface = Color(0xFFF8FAFC)
val DarkSurfaceVariant = Color(0xFF1F2937)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)
val DarkOutline = Color(0xFF334155)

// Status & Message bubble colors
val ChatBubbleSentLight = Color(0xFF6366F1)
val ChatBubbleSentDark = Color(0xFF4F46E5)
val ChatBubbleReceivedLight = Color(0xFFF1F5F9)
val ChatBubbleReceivedDark = Color(0xFF1E293B)

// Brand Gradients
val PulseGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF6366F1), Color(0xFF06B6D4))
)

val PulseVioletGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
)

val PulseDarkGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF0F172A), Color(0xFF080D1A))
)
