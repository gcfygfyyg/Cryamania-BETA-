package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CryaColorScheme = darkColorScheme(
    primary = RobloxPureWhite,
    onPrimary = RobloxBlack,
    primaryContainer = RobloxMidGray,
    onPrimaryContainer = RobloxPureWhite,
    secondary = RobloxOffWhite,
    onSecondary = RobloxBlack,
    secondaryContainer = RobloxDarkGray,
    onSecondaryContainer = RobloxOffWhite,
    tertiary = RobloxLightGray,
    onTertiary = RobloxBlack,
    background = RobloxBlack,
    onBackground = RobloxPureWhite,
    surface = RobloxDarkGray,
    onSurface = RobloxPureWhite,
    surfaceVariant = RobloxMidGray,
    onSurfaceVariant = RobloxLightGray,
    outline = RobloxBorder,
    error = RobloxSilver,
    onError = RobloxBlack
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CryaColorScheme,
        typography = Typography,
        content = content
    )
}
