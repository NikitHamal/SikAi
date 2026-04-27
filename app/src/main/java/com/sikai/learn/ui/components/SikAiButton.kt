package com.sikai.learn.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.sikai.learn.ui.theme.SikAi

enum class SikAiButtonVariant { Primary, Secondary, Ghost, Danger }

@Composable
fun SikAiButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: SikAiButtonVariant = SikAiButtonVariant.Primary,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val colors = SikAi.colors
    val tokens = SikAi.tokens
    val type = SikAi.type

    val (bg, fg, borderColor) = when (variant) {
        SikAiButtonVariant.Primary -> Triple(colors.primary, colors.onPrimary, Color.Transparent)
        SikAiButtonVariant.Secondary -> Triple(colors.surface, colors.onSurface, colors.borderStrong)
        SikAiButtonVariant.Ghost -> Triple(Color.Transparent, colors.onSurface, Color.Transparent)
        SikAiButtonVariant.Danger -> Triple(colors.danger, Color.White, Color.Transparent)
    }

    val effectiveBg = if (enabled) bg else colors.surfaceMuted
    val effectiveFg = if (enabled) fg else colors.onSurfaceMuted

    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .heightIn(min = tokens.touchTarget)
            .clip(tokens.cornerPill)
            .background(effectiveBg)
            .let {
                if (variant == SikAiButtonVariant.Secondary) {
                    it.border(BorderStroke(1.dp, borderColor), tokens.cornerPill)
                } else it
            }
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                androidx.compose.material3.Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = effectiveFg,
                    modifier = Modifier.height(18.dp)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = type.label,
                color = effectiveFg
            )
        }
    }
}
