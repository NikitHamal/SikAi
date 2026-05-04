package com.sikai.learn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sikai.learn.ui.theme.SikAi

@Composable
fun SikAiCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: Dp = 16.dp,
    surfaceColor: Color? = null,
    emphasized: Boolean = false,
    content: @Composable () -> Unit,
) {
    val tokens = SikAi.tokens
    val shape = tokens.cornerMedium
    val bg = surfaceColor ?: if (emphasized) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }

    val baseModifier = modifier
        .clip(shape)
        .background(bg)
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }

    Box(modifier = baseModifier.padding(contentPadding)) {
        content()
    }
}
