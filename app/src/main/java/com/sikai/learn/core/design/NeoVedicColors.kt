package com.sikai.learn.core.design

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val CosmicIndigo = Color(0xFF1A233A)
val CosmicIndigoLight = Color(0xFF2D3654)
val CosmicIndigoDark = Color(0xFF0F1520)
val Vellum = Color(0xFFF2EFE9)
val PressedPaper = Color(0xFFEBE7DE)
val PaperHover = Color(0xFFE0DCCF)
val PaperDark = Color(0xFFD8D3C8)
val VedicGold = Color(0xFFC5A059)
val MarsRed = Color(0xFFB85C5C)
val BorderSubtle = Color(0xFFC8C4BA)
val BorderStrong = Color(0xFFA9A598)
val DarkVellum = Color(0xFF1A1E2E)
val DarkPaper = Color(0xFF232840)
val DarkPaperHover = Color(0xFF2D3352)
val DarkBorderSubtle = Color(0xFF3A3F55)
val DarkBorderStrong = Color(0xFF4A5070)
val DarkSlateMuted = Color(0xFF7A7E8A)
val DarkGold = Color(0xFFD4B574)

data class NeoVedicPalette(
    val background: Color,
    val surface: Color,
    val surfaceHover: Color,
    val text: Color,
    val textMuted: Color,
    val primary: Color,
    val primarySoft: Color,
    val accent: Color,
    val danger: Color,
    val border: Color,
    val borderStrong: Color
)

val LocalNeoVedicPalette = staticCompositionLocalOf {
    NeoVedicPalette(Vellum, PressedPaper, PaperHover, CosmicIndigo, CosmicIndigoLight, CosmicIndigo, CosmicIndigoLight, VedicGold, MarsRed, BorderSubtle, BorderStrong)
}
