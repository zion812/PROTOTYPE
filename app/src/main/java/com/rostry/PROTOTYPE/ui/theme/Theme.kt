package com.rostry.prototype.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = OnPrimary,
    primaryContainer = GreenContainer,
    onPrimaryContainer = GreenDark,
    secondary = Secondary,
    secondaryContainer = SecondaryContainer,
    background = Background,
    surface = Surface,
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
