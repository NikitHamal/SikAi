package com.sikai.learn.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sikai.learn.ui.theme.SikAi

@Composable
fun SikAiLearningCard(
    title: String,
    subtitle: String,
    eyebrow: String? = null,
    cta: String? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors = SikAi.colors
    SikAiCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = 20.dp,
        emphasized = true
    ) {
        Column {
            if (eyebrow != null) {
                Text(
                    text = eyebrow.uppercase(),
                    style = SikAi.type.sectionTitle,
                    color = colors.accent
                )
                Spacer(Modifier.height(8.dp))
            }
            Text(
                text = title,
                style = SikAi.type.titleLarge,
                color = colors.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = SikAi.type.bodyMedium,
                color = colors.onSurfaceMuted
            )
            if (cta != null) {
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = cta.uppercase(),
                        style = SikAi.type.label,
                        color = colors.accent
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(text = "→", color = colors.accent)
                }
            }
        }
    }
}
