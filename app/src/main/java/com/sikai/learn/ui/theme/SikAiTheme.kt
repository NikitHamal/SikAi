package com.sikai.learn.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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
            primaryContainer = colors.primaryContainer,
            onPrimaryContainer = colors.onPrimaryContainer,
            secondary = colors.secondary,
            onSecondary = colors.onSecondary,
            secondaryContainer = colors.secondaryContainer,
            onSecondaryContainer = colors.onSecondaryContainer,
            tertiary = colors.tertiary,
            onTertiary = colors.onTertiary,
            tertiaryContainer = colors.tertiaryContainer,
            onTertiaryContainer = colors.onTertiaryContainer,
            background = colors.background,
            onBackground = colors.onBackground,
            surface = colors.surface,
            onSurface = colors.onSurface,
            surfaceVariant = colors.surfaceVariant,
            onSurfaceVariant = colors.onSurfaceVariant,
            outline = colors.outline,
            error = colors.error,
            onError = colors.onError,
        )
    } else {
        lightColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            primaryContainer = colors.primaryContainer,
            onPrimaryContainer = colors.onPrimaryContainer,
            secondary = colors.secondary,
            onSecondary = colors.onSecondary,
            secondaryContainer = colors.secondaryContainer,
            onSecondaryContainer = colors.onSecondaryContainer,
            tertiary = colors.tertiary,
            onTertiary = colors.onTertiary,
            tertiaryContainer = colors.tertiaryContainer,
            onTertiaryContainer = colors.onTertiaryContainer,
            background = colors.background,
            onBackground = colors.onBackground,
            surface = colors.surface,
            onSurface = colors.onSurface,
            surfaceVariant = colors.surfaceVariant,
            onSurfaceVariant = colors.onSurfaceVariant,
            outline = colors.outline,
            error = colors.error,
            onError = colors.onError,
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
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
                extraSmall = tokens.cornerSmall,
                small = tokens.cornerSmall,
                medium = tokens.cornerMedium,
                large = tokens.cornerLarge,
                extraLarge = tokens.cornerExtraLarge,
            ),
            content = content
        )
    }
}
