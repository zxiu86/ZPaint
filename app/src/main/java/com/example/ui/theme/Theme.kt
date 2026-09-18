package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ZPaintDarkColorScheme = darkColorScheme(
    primary = WhitePure,
    onPrimary = DarkBg,
    primaryContainer = DarkSurfaceHighlight,
    onPrimaryContainer = WhitePure,
    secondary = WhiteComfortable,
    onSecondary = DarkBg,
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = WhiteSoft,
    tertiary = WhiteMuted,
    onTertiary = DarkBg,
    background = DarkBg,
    onBackground = WhiteSoft,
    surface = DarkSurface,
    onSurface = WhiteSoft,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = WhiteMuted,
    outline = GrayBorderComfortable,
    outlineVariant = GrayBorderSubtle
)

@Composable
fun ZPaintTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ZPaintDarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ZPaintTheme(content = content)
}

