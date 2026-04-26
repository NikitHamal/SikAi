package com.sikai.learn.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sikai.learn.ui.theme.NeoVedicTokens

/**
 * Page header — serif headline + small uppercase data label, with subtle hairline divider.
 */
@Composable
fun NeoVedicPageHeader(
    title: String,
    subtitle: String? = null,
    eyebrow: String? = null,
    actions: (@Composable () -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = NeoVedicTokens.SpaceLg, vertical = NeoVedicTokens.SpaceMd)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        androidx.compose.material.icons.Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = if (onBack != null) 0.dp else NeoVedicTokens.SpaceXs)) {
                if (eyebrow != null) {
                    Text(
                        eyebrow.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                Text(
                    title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            actions?.invoke()
        }
        androidx.compose.foundation.layout.Spacer(Modifier.padding(top = NeoVedicTokens.SpaceMd))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = NeoVedicTokens.StrokeHairline)
    }
}

@Composable
fun NeoVedicSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    accent: String? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm),
        modifier = modifier.fillMaxWidth().padding(horizontal = NeoVedicTokens.SpaceLg, vertical = NeoVedicTokens.SpaceSm),
    ) {
        Text(
            text.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant, thickness = NeoVedicTokens.StrokeHairline)
        if (accent != null) {
            Text(
                accent,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

enum class StatusPillTone { Neutral, Gold, Success, Warn, Info, Error }

@Composable
fun NeoVedicStatusPill(
    text: String,
    tone: StatusPillTone = StatusPillTone.Neutral,
    icon: ImageVector? = null,
) {
    val (bg, fg) = when (tone) {
        StatusPillTone.Gold -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        StatusPillTone.Success -> Color(0x336B8E5A) to Color(0xFF3F5A30)
        StatusPillTone.Warn -> Color(0x33D4A04A) to Color(0xFF8C6420)
        StatusPillTone.Info -> Color(0x335A7A8E) to Color(0xFF324A60)
        StatusPillTone.Error -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f) to MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(2.dp))
            .background(bg)
            .border(BorderStroke(NeoVedicTokens.StrokeHairline, fg.copy(alpha = 0.25f)), RoundedCornerShape(2.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.padding(end = 2.dp))
        }
        Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = fg)
    }
}

@Composable
fun NeoVedicEmptyState(
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceMd),
        modifier = Modifier.fillMaxWidth().padding(NeoVedicTokens.Space2xl),
    ) {
        if (icon != null) {
            Icon(
                icon, contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(NeoVedicTokens.SpaceSm),
            )
        }
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        if (description != null) {
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (action != null) action()
    }
}

@Composable
fun HairlineDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = NeoVedicTokens.StrokeHairline,
    )
}

@Composable
fun NeoVedicSkeleton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}
