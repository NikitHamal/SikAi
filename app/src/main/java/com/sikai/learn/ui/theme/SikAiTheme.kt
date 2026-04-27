package com.sikai.learn.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

enum class ThemeMode { System, Light, Dark }

val LocalSikAiColors = staticCompositionLocalOf<SikAiColors> {
    error("SikAiColors not provided")
}

val LocalSikAiType = staticCompositionLocalOf<SikAiType> {
    error("SikAiType not provided")
}

val LocalSikAiTokens = staticCompositionLocalOf { SikAiTokens() }

object SikAi {
    val colors: SikAiColors
        @Composable get() = LocalSikAiColors.current

    val type: SikAiType
        @Composable get() = LocalSikAiType.current

    val tokens: SikAiTokens
        @Composable get() = LocalSikAiTokens.current
}

@Composable
fun SikAiTheme(
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val colors = if (isDark) darkSikAiColors() else lightSikAiColors()
    val type = defaultSikAiType()
    val tokens = SikAiTokens()

    val materialColorScheme = if (isDark) {
        darkColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            secondary = colors.accent,
            onSecondary = colors.onAccent,
            tertiary = colors.accent,
            background = colors.background,
            onBackground = colors.onSurface,
            surface = colors.surface,
            onSurface = colors.onSurface,
            surfaceVariant = colors.surfaceMuted,
            onSurfaceVariant = colors.onSurfaceMuted,
            outline = colors.borderStrong,
            outlineVariant = colors.borderSubtle,
            error = colors.danger,
        )
    } else {
        lightColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            secondary = colors.accent,
            onSecondary = colors.onAccent,
            tertiary = colors.accent,
            background = colors.background,
            onBackground = colors.onSurface,
            surface = colors.surface,
            onSurface = colors.onSurface,
            surfaceVariant = colors.surfaceMuted,
            onSurfaceVariant = colors.onSurfaceMuted,
            outline = colors.borderStrong,
            outlineVariant = colors.borderSubtle,
            error = colors.danger,
        )
    }

    CompositionLocalProvider(
        LocalSikAiColors provides colors,
        LocalSikAiType provides type,
        LocalSikAiTokens provides tokens,
        LocalContentColor provides colors.onSurface,
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = type.toMaterialTypography(),
            shapes = androidx.compose.material3.Shapes(
                extraSmall = tokens.cornerSharp,
                small = tokens.cornerSharp,
                medium = tokens.cornerCard,
                large = tokens.cornerCard,
                extraLarge = tokens.cornerCard,
            ),
            content = content
        )
    }
}
