package com.rostry.prototype.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = DarkGreen,
    onPrimary = Color.White,
    primaryContainer = DarkGreenContainer,
    onPrimaryContainer = DarkGreenDark,
    secondary = Terracotta,
    secondaryContainer = TerracottaContainer,
    onSecondary = Color.White,
    onSecondaryContainer = TerracottaDark,
    background = WarmWhite,
    surface = WarmWhite,
    surfaceVariant = Color(0xFFF0EBE6),
    error = Error,
    onBackground = OnSurface,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline
)

@Composable
fun RostryPrototypeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
