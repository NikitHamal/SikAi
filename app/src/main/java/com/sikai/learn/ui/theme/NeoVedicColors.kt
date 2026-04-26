package com.sikai.learn.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Neo-Vedic palette. Light theme uses warm parchment surfaces with cosmic indigo
 * primary text; dark theme uses muted cosmic surfaces with a soft gold accent.
 */
object NeoVedicPalette {
    // Cosmic indigo family (text + primary action)
    val CosmicIndigo = Color(0xFF1A233A)
    val CosmicIndigoLight = Color(0xFF2D3654)
    val CosmicIndigoDark = Color(0xFF0F1520)

    // Parchment surfaces
    val Vellum = Color(0xFFF2EFE9)
    val PressedPaper = Color(0xFFEBE7DE)
    val PaperHover = Color(0xFFE0DCCF)
    val PaperDark = Color(0xFFD8D3C8)

    // Sacred gold
    val VedicGold = Color(0xFFC5A059)
    val VedicGoldDark = Color(0xFFD4B574)

    // Status / signals
    val MarsRed = Color(0xFFB85C5C)

    // Borders
    val BorderSubtle = Color(0xFFC8C4BA)
    val BorderStrong = Color(0xFFA9A598)

    // Dark variants
    val DarkVellum = Color(0xFF1A1E2E)
    val DarkPaper = Color(0xFF232840)
    val DarkPaperHover = Color(0xFF2D3352)
    val DarkBorderSubtle = Color(0xFF3A3F55)
    val DarkBorderStrong = Color(0xFF4A5070)
    val DarkSlateMuted = Color(0xFF7A7E8A)
}

@Immutable
data class NeoVedicColors(
    val background: Color,
    val surface: Color,
    val surfaceMuted: Color,
    val surfaceHover: Color,
    val pressed: Color,
    val onSurface: Color,
    val onSurfaceMuted: Color,
    val primary: Color,
    val onPrimary: Color,
    val accent: Color,
    val onAccent: Color,
    val borderSubtle: Color,
    val borderStrong: Color,
    val danger: Color,
    val isDark: Boolean,
)

fun lightNeoVedicColors() = NeoVedicColors(
    background = NeoVedicPalette.Vellum,
    surface = NeoVedicPalette.Vellum,
    surfaceMuted = NeoVedicPalette.PressedPaper,
    surfaceHover = NeoVedicPalette.PaperHover,
    pressed = NeoVedicPalette.PaperDark,
    onSurface = NeoVedicPalette.CosmicIndigo,
    onSurfaceMuted = NeoVedicPalette.CosmicIndigoLight,
    primary = NeoVedicPalette.CosmicIndigo,
    onPrimary = NeoVedicPalette.Vellum,
    accent = NeoVedicPalette.VedicGold,
    onAccent = NeoVedicPalette.CosmicIndigoDark,
    borderSubtle = NeoVedicPalette.BorderSubtle,
    borderStrong = NeoVedicPalette.BorderStrong,
    danger = NeoVedicPalette.MarsRed,
    isDark = false,
)

fun darkNeoVedicColors() = NeoVedicColors(
    background = NeoVedicPalette.DarkVellum,
    surface = NeoVedicPalette.DarkPaper,
    surfaceMuted = NeoVedicPalette.DarkPaperHover,
    surfaceHover = NeoVedicPalette.DarkPaperHover,
    pressed = NeoVedicPalette.DarkBorderSubtle,
    onSurface = NeoVedicPalette.Vellum,
    onSurfaceMuted = NeoVedicPalette.DarkSlateMuted,
    primary = NeoVedicPalette.Vellum,
    onPrimary = NeoVedicPalette.CosmicIndigoDark,
    accent = NeoVedicPalette.VedicGoldDark,
    onAccent = NeoVedicPalette.CosmicIndigoDark,
    borderSubtle = NeoVedicPalette.DarkBorderSubtle,
    borderStrong = NeoVedicPalette.DarkBorderStrong,
    danger = NeoVedicPalette.MarsRed,
    isDark = true,
)
