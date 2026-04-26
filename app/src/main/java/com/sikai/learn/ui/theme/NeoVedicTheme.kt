package com.sikai.learn.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

private val LightColors = lightColorScheme(
    primary = NeoVedicColors.CosmicIndigo,
    onPrimary = NeoVedicColors.Vellum,
    primaryContainer = NeoVedicColors.CosmicIndigoLight,
    onPrimaryContainer = NeoVedicColors.Vellum,
    secondary = NeoVedicColors.VedicGold,
    onSecondary = NeoVedicColors.CosmicIndigo,
    secondaryContainer = NeoVedicColors.PaperHover,
    onSecondaryContainer = NeoVedicColors.CosmicIndigo,
    tertiary = NeoVedicColors.MarsRed,
    onTertiary = NeoVedicColors.Vellum,
    background = NeoVedicColors.Vellum,
    onBackground = NeoVedicColors.CosmicIndigo,
    surface = NeoVedicColors.Vellum,
    onSurface = NeoVedicColors.CosmicIndigo,
    surfaceVariant = NeoVedicColors.PressedPaper,
    onSurfaceVariant = NeoVedicColors.SlateOnPaper,
    outline = NeoVedicColors.BorderStrong,
    outlineVariant = NeoVedicColors.BorderSubtle,
    error = NeoVedicColors.MarsRed,
    onError = NeoVedicColors.Vellum,
)

private val DarkColors = darkColorScheme(
    primary = NeoVedicColors.DarkOnPaper,
    onPrimary = NeoVedicColors.DarkVellum,
    primaryContainer = NeoVedicColors.DarkPaper,
    onPrimaryContainer = NeoVedicColors.DarkOnPaper,
    secondary = NeoVedicColors.DarkGold,
    onSecondary = NeoVedicColors.DarkVellum,
    secondaryContainer = NeoVedicColors.DarkPaperHover,
    onSecondaryContainer = NeoVedicColors.DarkOnPaper,
    tertiary = NeoVedicColors.MarsRed,
    onTertiary = NeoVedicColors.DarkVellum,
    background = NeoVedicColors.DarkVellum,
    onBackground = NeoVedicColors.DarkOnPaper,
    surface = NeoVedicColors.DarkVellum,
    onSurface = NeoVedicColors.DarkOnPaper,
    surfaceVariant = NeoVedicColors.DarkPaper,
    onSurfaceVariant = NeoVedicColors.DarkSlateMuted,
    outline = NeoVedicColors.DarkBorderStrong,
    outlineVariant = NeoVedicColors.DarkBorderSubtle,
    error = NeoVedicColors.MarsRed,
    onError = NeoVedicColors.DarkVellum,
)

val LocalNeoVedicAccent = staticCompositionLocalOf { NeoVedicColors.VedicGold }

@Composable
fun SikAiTheme(
    themeMode: String = "system",
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> systemDark
    }
    val colors = if (isDark) DarkColors else LightColors
    val accent = if (isDark) NeoVedicColors.DarkGold else NeoVedicColors.VedicGold

    CompositionLocalProvider(LocalNeoVedicAccent provides accent) {
        MaterialTheme(
            colorScheme = colors,
            typography = NeoVedicTypography,
            content = content,
        )
    }
}
