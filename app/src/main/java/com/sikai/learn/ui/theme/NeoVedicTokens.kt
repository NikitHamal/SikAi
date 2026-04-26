package com.sikai.learn.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object NeoVedicTokens {
    // Spacing scale (8dp base with 4dp halves)
    val SpaceXxs = 2.dp
    val SpaceXs = 4.dp
    val SpaceSm = 8.dp
    val SpaceMd = 12.dp
    val SpaceLg = 16.dp
    val SpaceXl = 24.dp
    val Space2xl = 32.dp
    val Space3xl = 48.dp

    // Corner radius — Neo-Vedic prefers sharp 2dp
    val CornerSharp = 2.dp
    val CornerSoft = 4.dp
    val CornerCard = 2.dp
    val CornerPill = 999.dp

    val ShapeSharp = RoundedCornerShape(CornerSharp)
    val ShapeSoft = RoundedCornerShape(CornerSoft)
    val ShapeCard = RoundedCornerShape(CornerCard)
    val ShapePill = RoundedCornerShape(CornerPill)

    // Stroke widths — hairline is the signature
    val StrokeHairline = 1.dp
    val StrokeStrong = 1.5.dp

    // Common card heights / minimums
    val MinTouchTarget = 48.dp
    val IconSm = 16.dp
    val IconMd = 20.dp
    val IconLg = 24.dp

    // L-shaped corner marker dimensions
    val CornerMarkerLength = 14.dp
    val CornerMarkerThickness = 1.dp
    val CornerMarkerInset = 6.dp
}
