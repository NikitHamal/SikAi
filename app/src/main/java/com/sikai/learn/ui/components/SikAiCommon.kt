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
import com.sikai.learn.ui.theme.SikAi

@Composable
fun SikAiSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = SikAi.colors
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
                style = SikAi.type.sectionTitle,
                color = colors.onSurfaceMuted
            )
        }
        trailing?.invoke()
    }
}

@Composable
fun SikAiStatusPill(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color? = null,
    leadingIcon: ImageVector? = null,
) {
    val colors = SikAi.colors
    val pillColor = accent ?: colors.accent
    Row(
        modifier = modifier
            .clip(SikAi.tokens.cornerPill)
            .background(colors.surfaceMuted)

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
            style = SikAi.type.label,
            color = colors.onSurface
        )
    }
}

@Composable
fun SikAiEmptyState(
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
    icon: ImageVector = Icons.Outlined.Inbox,
) {
    val colors = SikAi.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(SikAi.tokens.cornerSharp)
                .background(colors.surfaceMuted)
                ,
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
        Text(text = title, style = SikAi.type.titleLarge, color = colors.onSurface)
        if (description != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = description,
                style = SikAi.type.bodyMedium,
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
fun SikAiPageHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = SikAi.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = SikAi.tokens.pageHorizontal,
                vertical = SikAi.tokens.space20
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
                        style = SikAi.type.sectionTitle,
                        color = colors.accent
                    )
                    Spacer(Modifier.height(4.dp))
                }
                Text(
                    text = title,
                    style = SikAi.type.displayMedium,
                    color = colors.onSurface
                )
            }
            trailing?.invoke()
        }
    }

}

@Composable
fun SikAiHairline(modifier: Modifier = Modifier) {

}
