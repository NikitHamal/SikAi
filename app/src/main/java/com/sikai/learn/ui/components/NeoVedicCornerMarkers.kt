package com.sikai.learn.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sikai.learn.ui.theme.LocalNeoVedicAccent
import com.sikai.learn.ui.theme.NeoVedicTokens

/**
 * Adds L-shaped Vedic corner markers in the four corners. The markers are drawn as
 * hairline strokes using the gold accent by default. They sit slightly inside the
 * card edges to feel like sacred margin marks rather than borders.
 */
fun Modifier.neoVedicCornerMarkers(
    color: Color? = null,
    length: Dp = NeoVedicTokens.CornerMarkerLength,
    inset: Dp = NeoVedicTokens.CornerMarkerInset,
    thickness: Dp = NeoVedicTokens.CornerMarkerThickness,
): Modifier = composed {
    val markerColor = color ?: LocalNeoVedicAccent.current
    drawWithContent {
        drawContent()
        val len = length.toPx()
        val ins = inset.toPx()
        val thick = thickness.toPx()

        val w = size.width
        val h = size.height

        // Top-left
        drawLine(markerColor, Offset(ins, ins), Offset(ins + len, ins), thick)
        drawLine(markerColor, Offset(ins, ins), Offset(ins, ins + len), thick)
        // Top-right
        drawLine(markerColor, Offset(w - ins, ins), Offset(w - ins - len, ins), thick)
        drawLine(markerColor, Offset(w - ins, ins), Offset(w - ins, ins + len), thick)
        // Bottom-left
        drawLine(markerColor, Offset(ins, h - ins), Offset(ins + len, h - ins), thick)
        drawLine(markerColor, Offset(ins, h - ins), Offset(ins, h - ins - len), thick)
        // Bottom-right
        drawLine(markerColor, Offset(w - ins, h - ins), Offset(w - ins - len, h - ins), thick)
        drawLine(markerColor, Offset(w - ins, h - ins), Offset(w - ins, h - ins - len), thick)
    }
}

@Composable
fun NeoVedicSpacer(width: Dp = 0.dp, height: Dp = 0.dp) {
    androidx.compose.foundation.layout.Spacer(Modifier.size(width, height))
}
