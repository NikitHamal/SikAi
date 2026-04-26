package com.sikai.learn.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sikai.learn.ui.theme.NeoVedicTokens

/**
 * Neo-Vedic card: flat surface, hairline border, sharp 2dp corners, no shadow.
 * Optional L-shaped corner markers can be enabled for sacred/important cards.
 */
@Composable
fun NeoVedicCard(
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.surface,
    border: BorderStroke = BorderStroke(NeoVedicTokens.StrokeHairline, MaterialTheme.colorScheme.outlineVariant),
    shape: RoundedCornerShape = NeoVedicTokens.ShapeCard,
    contentPadding: PaddingValues = PaddingValues(NeoVedicTokens.SpaceLg),
    showCornerMarkers: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val base = modifier
        .clip(shape)
        .background(background)
        .border(border, shape)

    val withCorners = if (showCornerMarkers) base.neoVedicCornerMarkers() else base
    val clickable = if (onClick != null) withCorners.clickable(onClick = onClick) else withCorners

    androidx.compose.foundation.layout.Box(
        modifier = clickable.padding(contentPadding),
    ) { content() }
}
