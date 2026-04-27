package com.sikai.learn.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class SikAiTokens(
    val cornerSharp: RoundedCornerShape = RoundedCornerShape(8.dp),
    val cornerCard: RoundedCornerShape = RoundedCornerShape(12.dp),
    val cornerPill: RoundedCornerShape = RoundedCornerShape(999.dp),
    val space2: Dp = 2.dp,
    val space4: Dp = 4.dp,
    val space6: Dp = 6.dp,
    val space8: Dp = 8.dp,
    val space12: Dp = 12.dp,
    val space16: Dp = 16.dp,
    val space20: Dp = 20.dp,
    val space24: Dp = 24.dp,
    val space32: Dp = 32.dp,
    val space40: Dp = 40.dp,
    val pageHorizontal: Dp = 12.dp,
    val sectionGap: Dp = 24.dp,
    val touchTarget: Dp = 48.dp,
)
