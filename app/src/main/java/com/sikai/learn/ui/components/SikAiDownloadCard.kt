package com.sikai.learn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.sikai.learn.ui.theme.SikAi

enum class DownloadCardState { Available, Downloading, Downloaded }

@Composable
fun SikAiDownloadCard(
    title: String,
    subtitle: String,
    sizeLabel: String,
    state: DownloadCardState,
    progress: Float? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors = SikAi.colors
    val (icon, label) = when (state) {
        DownloadCardState.Available -> Icons.Outlined.CloudDownload to "GET"
        DownloadCardState.Downloading -> Icons.Outlined.HourglassTop to "FETCHING"
        DownloadCardState.Downloaded -> Icons.Outlined.CheckCircle to "OFFLINE"
    }
    SikAiCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = SikAi.type.titleMedium, color = colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(text = subtitle, style = SikAi.type.bodySmall, color = colors.onSurfaceMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SikAiStatusPill(text = sizeLabel)
                    Spacer(Modifier.width(8.dp))
                    SikAiStatusPill(text = label, accent = colors.accent, leadingIcon = icon)
                }
                if (state == DownloadCardState.Downloading && progress != null) {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(colors.surfaceMuted)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                .height(2.dp)
                                .background(colors.accent)
                        )
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.onSurface
            )
        }
    }
}
