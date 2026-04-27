package com.sikai.learn.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

object SikAiPalette {
    val PrimaryLight = Color(0xFF0061A4)
    val OnPrimaryLight = Color(0xFFFFFFFF)
    val SecondaryLight = Color(0xFF535F70)
    val OnSecondaryLight = Color(0xFFFFFFFF)
    val BackgroundLight = Color(0xFFFDFBFF)
    val OnBackgroundLight = Color(0xFF1A1C1E)
    val SurfaceLight = Color(0xFFFDFBFF)
    val OnSurfaceLight = Color(0xFF1A1C1E)
    val SurfaceVariantLight = Color(0xFFDFE2EB)
    val OnSurfaceVariantLight = Color(0xFF43474E)
    val BorderLight = Color(0xFFC3C7CF)
    val ErrorLight = Color(0xFFBA1A1A)

    val PrimaryDark = Color(0xFF9ECAFF)
    val OnPrimaryDark = Color(0xFF003258)
    val SecondaryDark = Color(0xFFBBC7DB)
    val OnSecondaryDark = Color(0xFF253140)
    val BackgroundDark = Color(0xFF1A1C1E)
    val OnBackgroundDark = Color(0xFFE2E2E6)
    val SurfaceDark = Color(0xFF1A1C1E)
    val OnSurfaceDark = Color(0xFFE2E2E6)
    val SurfaceVariantDark = Color(0xFF43474E)
    val OnSurfaceVariantDark = Color(0xFFC3C7CF)
    val BorderDark = Color(0xFF8D9199)
    val ErrorDark = Color(0xFFFFB4AB)
}

@Immutable
data class SikAiColors(
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

fun lightSikAiColors() = SikAiColors(
    background = SikAiPalette.BackgroundLight,
    surface = SikAiPalette.SurfaceLight,
    surfaceMuted = SikAiPalette.SurfaceVariantLight,
    surfaceHover = SikAiPalette.SurfaceVariantLight,
    pressed = SikAiPalette.SurfaceVariantLight,
    onSurface = SikAiPalette.OnSurfaceLight,
    onSurfaceMuted = SikAiPalette.OnSurfaceVariantLight,
    primary = SikAiPalette.PrimaryLight,
    onPrimary = SikAiPalette.OnPrimaryLight,
    accent = SikAiPalette.SecondaryLight,
    onAccent = SikAiPalette.OnSecondaryLight,
    borderSubtle = SikAiPalette.BorderLight,
    borderStrong = SikAiPalette.BorderLight,
    danger = SikAiPalette.ErrorLight,
    isDark = false,
)

fun darkSikAiColors() = SikAiColors(
    background = SikAiPalette.BackgroundDark,
    surface = SikAiPalette.SurfaceDark,
    surfaceMuted = SikAiPalette.SurfaceVariantDark,
    surfaceHover = SikAiPalette.SurfaceVariantDark,
    pressed = SikAiPalette.SurfaceVariantDark,
    onSurface = SikAiPalette.OnSurfaceDark,
    onSurfaceMuted = SikAiPalette.OnSurfaceVariantDark,
    primary = SikAiPalette.PrimaryDark,
    onPrimary = SikAiPalette.OnPrimaryDark,
    accent = SikAiPalette.SecondaryDark,
    onAccent = SikAiPalette.OnSecondaryDark,
    borderSubtle = SikAiPalette.BorderDark,
    borderStrong = SikAiPalette.BorderDark,
    danger = SikAiPalette.ErrorDark,
    isDark = true,
)
