package com.sikai.learn.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sikai.learn.ui.theme.NeoVedic

/**
 * Draws subtle L-shaped Vedic markers in each corner of the modified element.
 * Use on important cards/panels that should feel framed without heavy borders.
 */
fun Modifier.neoVedicCornerMarkers(
    color: Color? = null,
    size: Dp = 12.dp,
    stroke: Dp = 1.5.dp,
    inset: Dp = 6.dp,
): Modifier = composed {
    val markerColor = color ?: NeoVedic.colors.accent
    val density = LocalDensity.current
    val sizePx = with(density) { size.toPx() }
    val strokePx = with(density) { stroke.toPx() }
    val insetPx = with(density) { inset.toPx() }

    this
        .padding(inset)
        .drawWithContent {
            drawContent()
            val w = this.size.width
            val h = this.size.height

            // top-left
            drawLine(markerColor, Offset(insetPx, insetPx), Offset(insetPx + sizePx, insetPx), strokePx)
            drawLine(markerColor, Offset(insetPx, insetPx), Offset(insetPx, insetPx + sizePx), strokePx)
            // top-right
            drawLine(markerColor, Offset(w - insetPx - sizePx, insetPx), Offset(w - insetPx, insetPx), strokePx)
            drawLine(markerColor, Offset(w - insetPx, insetPx), Offset(w - insetPx, insetPx + sizePx), strokePx)
            // bottom-left
            drawLine(markerColor, Offset(insetPx, h - insetPx - sizePx), Offset(insetPx, h - insetPx), strokePx)
            drawLine(markerColor, Offset(insetPx, h - insetPx), Offset(insetPx + sizePx, h - insetPx), strokePx)
            // bottom-right
            drawLine(markerColor, Offset(w - insetPx, h - insetPx - sizePx), Offset(w - insetPx, h - insetPx), strokePx)
            drawLine(markerColor, Offset(w - insetPx - sizePx, h - insetPx), Offset(w - insetPx, h - insetPx), strokePx)
        }
}
