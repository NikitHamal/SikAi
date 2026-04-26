package com.sikai.learn.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.sikai.learn.ui.theme.NeoVedic

@Composable
fun NeoVedicSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = NeoVedic.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(colors.accent)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text.uppercase(),
                style = NeoVedic.type.sectionTitle,
                color = colors.onSurfaceMuted
            )
        }
        trailing?.invoke()
    }
}

@Composable
fun NeoVedicStatusPill(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color? = null,
    leadingIcon: ImageVector? = null,
) {
    val colors = NeoVedic.colors
    val pillColor = accent ?: colors.accent
    Row(
        modifier = modifier
            .clip(NeoVedic.tokens.cornerPill)
            .background(colors.surfaceMuted)
            .border(
                BorderStroke(NeoVedic.tokens.borderHairline, colors.borderSubtle),
                NeoVedic.tokens.cornerPill
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(pillColor)
        )
        Spacer(Modifier.width(6.dp))
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = colors.onSurfaceMuted,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = text,
            style = NeoVedic.type.label,
            color = colors.onSurface
        )
    }
}

@Composable
fun NeoVedicEmptyState(
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
    icon: ImageVector = Icons.Outlined.Inbox,
) {
    val colors = NeoVedic.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(NeoVedic.tokens.cornerSharp)
                .background(colors.surfaceMuted)
                .border(
                    BorderStroke(NeoVedic.tokens.borderHairline, colors.borderSubtle),
                    NeoVedic.tokens.cornerSharp
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(text = title, style = NeoVedic.type.titleLarge, color = colors.onSurface)
        if (description != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = description,
                style = NeoVedic.type.bodyMedium,
                color = colors.onSurfaceMuted
            )
        }
        if (action != null) {
            Spacer(Modifier.height(20.dp))
            action()
        }
    }
}

@Composable
fun NeoVedicPageHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = NeoVedic.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = NeoVedic.tokens.pageHorizontal,
                vertical = NeoVedic.tokens.space20
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                if (subtitle != null) {
                    Text(
                        text = subtitle.uppercase(),
                        style = NeoVedic.type.sectionTitle,
                        color = colors.accent
                    )
                    Spacer(Modifier.height(4.dp))
                }
                Text(
                    text = title,
                    style = NeoVedic.type.displayMedium,
                    color = colors.onSurface
                )
            }
            trailing?.invoke()
        }
    }
    HorizontalDivider(thickness = 0.5.dp, color = colors.borderSubtle)
}

@Composable
fun NeoVedicHairline(modifier: Modifier = Modifier) {
    HorizontalDivider(thickness = 0.5.dp, color = NeoVedic.colors.borderSubtle, modifier = modifier)
}
