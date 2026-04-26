package com.sikai.learn.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private fun lightPalette() = NeoVedicPalette(Vellum, PressedPaper, PaperHover, CosmicIndigo, CosmicIndigoLight, CosmicIndigo, CosmicIndigoLight, VedicGold, MarsRed, BorderSubtle, BorderStrong)
private fun darkPalette() = NeoVedicPalette(DarkVellum, DarkPaper, DarkPaperHover, Vellum, DarkSlateMuted, DarkGold, DarkPaperHover, DarkGold, MarsRed, DarkBorderSubtle, DarkBorderStrong)

private fun materialLight(p: NeoVedicPalette): ColorScheme = lightColorScheme(primary = p.primary, secondary = p.accent, background = p.background, surface = p.surface, onPrimary = Vellum, onSecondary = CosmicIndigo, onBackground = p.text, onSurface = p.text, error = p.danger)
private fun materialDark(p: NeoVedicPalette): ColorScheme = darkColorScheme(primary = p.primary, secondary = p.accent, background = p.background, surface = p.surface, onPrimary = CosmicIndigo, onSecondary = CosmicIndigo, onBackground = p.text, onSurface = p.text, error = p.danger)

@Composable
fun NeoVedicTheme(theme: String = "system", content: @Composable () -> Unit) {
    val dark = when (theme) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    val palette = if (dark) darkPalette() else lightPalette()
    CompositionLocalProvider(LocalNeoVedicPalette provides palette) {
        MaterialTheme(colorScheme = if (dark) materialDark(palette) else materialLight(palette), typography = NeoVedicTypography, content = content)
    }
}
