package com.sikai.learn.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Neo-Vedic typography. We use system-safe families as fallbacks because bundled
 * fonts (Cinzel Decorative, Cormorant Garamond, Space Grotesk, Poppins, Noto Sans
 * Devanagari) can be added under res/font and swapped here without touching call sites.
 */
private val Display = FontFamily.Serif      // Cinzel-like fallback
private val Serif = FontFamily.Serif         // Cormorant-like fallback
private val Mono = FontFamily.Monospace      // Data labels
private val Sans = FontFamily.SansSerif      // Primary UI

val NeoVedicTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.Medium,
        fontSize = 38.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.Medium,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = Serif,
        fontWeight = FontWeight.Medium,
        fontSize = 26.sp,
        lineHeight = 32.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Serif,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = Serif,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.4.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Mono,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.2.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Mono,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.2.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Mono,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        letterSpacing = 1.4.sp,
    ),
)
