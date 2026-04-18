package com.aurora.music.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AuroraPurple = Color(0xFF7C5CFF)
private val AuroraMint = Color(0xFF3DDCB4)
private val Background = Color(0xFF070A12)
private val Surface = Color(0xFF0F1424)
private val SurfaceVariant = Color(0xFF171C2E)

private val AuroraDarkColors = darkColorScheme(
    primary = AuroraPurple,
    onPrimary = Color.White,
    secondary = AuroraMint,
    onSecondary = Color.Black,
    background = Background,
    onBackground = Color(0xFFE8EAFF),
    surface = Surface,
    onSurface = Color(0xFFE8EAFF),
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = Color(0xFFB4BAD0),
    outline = Color(0xFF2A3148),
)

@Composable
fun AuroraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AuroraDarkColors,
        typography = AuroraTypography,
        content = content,
    )
}
