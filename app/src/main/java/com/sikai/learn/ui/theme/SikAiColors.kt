package com.sikai.learn.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

object SikAiPalette {
    // Light Scheme
    val PrimaryLight = Color(0xFF4A4AB2)
    val OnPrimaryLight = Color(0xFFFFFFFF)
    val PrimaryContainerLight = Color(0xFFE0E0FF)
    val OnPrimaryContainerLight = Color(0xFF00006E)
    
    val SecondaryLight = Color(0xFF5B5D72)
    val OnSecondaryLight = Color(0xFFFFFFFF)
    val SecondaryContainerLight = Color(0xFFE0E1F9)
    val OnSecondaryContainerLight = Color(0xFF181A2C)
    
    val TertiaryLight = Color(0xFF77536D)
    val OnTertiaryLight = Color(0xFFFFFFFF)
    val TertiaryContainerLight = Color(0xFFFFD7F1)
    val OnTertiaryContainerLight = Color(0xFF2D1228)
    
    val BackgroundLight = Color(0xFFFBF8FF)
    val OnBackgroundLight = Color(0xFF1B1B21)
    
    val SurfaceLight = Color(0xFFFBF8FF)
    val OnSurfaceLight = Color(0xFF1B1B21)
    val SurfaceVariantLight = Color(0xFFE2E1EC)
    val OnSurfaceVariantLight = Color(0xFF45464F)
    
    val OutlineLight = Color(0xFF767680)
    val ErrorLight = Color(0xFFBA1A1A)
    val OnErrorLight = Color(0xFFFFFFFF)

    // Dark Scheme
    val PrimaryDark = Color(0xFFBEC2FF)
    val OnPrimaryDark = Color(0xFF161672)
    val PrimaryContainerDark = Color(0xFF313198)
    val OnPrimaryContainerDark = Color(0xFFE0E0FF)
    
    val SecondaryDark = Color(0xFFC4C5DD)
    val OnSecondaryDark = Color(0xFF2D2F42)
    val SecondaryContainerDark = Color(0xFF434559)
    val OnSecondaryContainerDark = Color(0xFFE0E1F9)
    
    val TertiaryDark = Color(0xFFE6BAD7)
    val OnTertiaryDark = Color(0xFF45263D)
    val TertiaryContainerDark = Color(0xFF5D3C55)
    val OnTertiaryContainerDark = Color(0xFFFFD7F1)
    
    val BackgroundDark = Color(0xFF1B1B21)
    val OnBackgroundDark = Color(0xFFE4E1E9)
    
    val SurfaceDark = Color(0xFF1B1B21)
    val OnSurfaceDark = Color(0xFFE4E1E9)
    val SurfaceVariantDark = Color(0xFF45464F)
    val OnSurfaceVariantDark = Color(0xFFC6C5D0)
    
    val OutlineDark = Color(0xFF90909A)
    val ErrorDark = Color(0xFFFFB4AB)
    val OnErrorDark = Color(0xFF690005)
}

@Immutable
data class SikAiColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val error: Color,
    val onError: Color,
    val isDark: Boolean,
)

fun lightSikAiColors() = SikAiColors(
    primary = SikAiPalette.PrimaryLight,
    onPrimary = SikAiPalette.OnPrimaryLight,
    primaryContainer = SikAiPalette.PrimaryContainerLight,
    onPrimaryContainer = SikAiPalette.OnPrimaryContainerLight,
    secondary = SikAiPalette.SecondaryLight,
    onSecondary = SikAiPalette.OnSecondaryLight,
    secondaryContainer = SikAiPalette.SecondaryContainerLight,
    onSecondaryContainer = SikAiPalette.OnSecondaryContainerLight,
    tertiary = SikAiPalette.TertiaryLight,
    onTertiary = SikAiPalette.OnTertiaryLight,
    tertiaryContainer = SikAiPalette.TertiaryContainerLight,
    onTertiaryContainer = SikAiPalette.OnTertiaryContainerLight,
    background = SikAiPalette.BackgroundLight,
    onBackground = SikAiPalette.OnBackgroundLight,
    surface = SikAiPalette.SurfaceLight,
    onSurface = SikAiPalette.OnSurfaceLight,
    surfaceVariant = SikAiPalette.SurfaceVariantLight,
    onSurfaceVariant = SikAiPalette.OnSurfaceVariantLight,
    outline = SikAiPalette.OutlineLight,
    error = SikAiPalette.ErrorLight,
    onError = SikAiPalette.OnErrorLight,
    isDark = false,
)

fun darkSikAiColors() = SikAiColors(
    primary = SikAiPalette.PrimaryDark,
    onPrimary = SikAiPalette.OnPrimaryDark,
    primaryContainer = SikAiPalette.PrimaryContainerDark,
    onPrimaryContainer = SikAiPalette.OnPrimaryContainerDark,
    secondary = SikAiPalette.SecondaryDark,
    onSecondary = SikAiPalette.OnSecondaryDark,
    secondaryContainer = SikAiPalette.SecondaryContainerDark,
    onSecondaryContainer = SikAiPalette.OnSecondaryContainerDark,
    tertiary = SikAiPalette.TertiaryDark,
    onTertiary = SikAiPalette.OnTertiaryDark,
    tertiaryContainer = SikAiPalette.TertiaryContainerDark,
    onTertiaryContainer = SikAiPalette.OnTertiaryContainerDark,
    background = SikAiPalette.BackgroundDark,
    onBackground = SikAiPalette.OnBackgroundDark,
    surface = SikAiPalette.SurfaceDark,
    onSurface = SikAiPalette.OnSurfaceDark,
    surfaceVariant = SikAiPalette.SurfaceVariantDark,
    onSurfaceVariant = SikAiPalette.OnSurfaceVariantDark,
    outline = SikAiPalette.OutlineDark,
    error = SikAiPalette.ErrorDark,
    onError = SikAiPalette.OnErrorDark,
    isDark = true,
)
