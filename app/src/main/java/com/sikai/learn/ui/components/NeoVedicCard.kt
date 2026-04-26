package com.sikai.learn.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sikai.learn.ui.theme.NeoVedic

/**
 * Flat surface card with hairline border, sharp 2dp corners, no elevation.
 */
@Composable
fun NeoVedicCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: Dp = 16.dp,
    surfaceColor: Color? = null,
    showCornerMarkers: Boolean = false,
    emphasized: Boolean = false,
    content: @Composable () -> Unit,
) {
    val tokens = NeoVedic.tokens
    val colors = NeoVedic.colors
    val shape = tokens.cornerCard
    val borderWidth = if (emphasized) tokens.borderEmphasis else tokens.borderHairline
    val borderColor = if (emphasized) colors.borderStrong else colors.borderSubtle
    val bg = surfaceColor ?: colors.surface

    val markersModifier = if (showCornerMarkers) Modifier.neoVedicCornerMarkers() else Modifier

    val baseModifier = modifier
        .clip(shape)
        .background(bg)
        .border(BorderStroke(borderWidth, borderColor), shape)
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }

    Box(modifier = baseModifier.then(markersModifier).padding(contentPadding)) {
        content()
    }
}
