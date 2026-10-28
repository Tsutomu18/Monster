package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AetheronColorScheme = darkColorScheme(
    primary = AetherCyan,
    onPrimary = MidnightSlate,
    primaryContainer = AetherCyanDark,
    onPrimaryContainer = TextPrimary,
    secondary = BadgeGold,
    onSecondary = MidnightSlate,
    secondaryContainer = Color(0xFF4A3500),
    onSecondaryContainer = BadgeGold,
    tertiary = CrimsonAccent,
    onTertiary = Color.White,
    background = MidnightSlate,
    onBackground = TextPrimary,
    surface = DeepTacticalSurface,
    onSurface = TextPrimary,
    surfaceVariant = ElevatedTacticalCard,
    onSurfaceVariant = TextSecondary,
    outline = TacticalBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AetheronColorScheme,
        typography = Typography,
        content = content
    )
}
