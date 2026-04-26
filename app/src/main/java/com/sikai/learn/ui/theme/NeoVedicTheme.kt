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

val LocalNeoVedicColors = staticCompositionLocalOf<NeoVedicColors> {
    error("NeoVedicColors not provided")
}

val LocalNeoVedicType = staticCompositionLocalOf<NeoVedicType> {
    error("NeoVedicType not provided")
}

val LocalNeoVedicTokens = staticCompositionLocalOf { NeoVedicTokens() }

object NeoVedic {
    val colors: NeoVedicColors
        @Composable get() = LocalNeoVedicColors.current

    val type: NeoVedicType
        @Composable get() = LocalNeoVedicType.current

    val tokens: NeoVedicTokens
        @Composable get() = LocalNeoVedicTokens.current
}

@Composable
fun NeoVedicTheme(
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val colors = if (isDark) darkNeoVedicColors() else lightNeoVedicColors()
    val type = defaultNeoVedicType()
    val tokens = NeoVedicTokens()

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
        LocalNeoVedicColors provides colors,
        LocalNeoVedicType provides type,
        LocalNeoVedicTokens provides tokens,
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
