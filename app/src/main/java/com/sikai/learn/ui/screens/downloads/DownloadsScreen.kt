package com.sikai.learn.ui.screens.downloads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.sikai.learn.data.repository.DownloadProgress
import com.sikai.learn.ui.components.NeoVedicButton
import com.sikai.learn.ui.components.NeoVedicButtonStyle
import com.sikai.learn.ui.components.NeoVedicCard
import com.sikai.learn.ui.components.NeoVedicEmptyState
import com.sikai.learn.ui.components.NeoVedicPageHeader
import com.sikai.learn.ui.components.NeoVedicStatusPill
import com.sikai.learn.ui.components.StatusPillTone
import com.sikai.learn.ui.theme.NeoVedicTokens

@Composable
fun DownloadsScreen(
    vm: DownloadsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val manifest by vm.manifest.collectAsState(initial = emptyList())
    val downloaded by vm.downloads.collectAsState(initial = emptyList())
    val state by vm.state.collectAsState()
    val downloadedSet = downloaded.map { it.manifestId }.toSet()

    Column(modifier = Modifier.fillMaxSize()) {
        NeoVedicPageHeader(
            title = "Downloads",
            subtitle = "Offline-first content. Manifest-driven, SHA-256 verified.",
            eyebrow = "Library",
            onBack = onBack,
            actions = {
                IconButton(onClick = vm::refresh) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.secondary)
                }
            },
        )

        if (state.message != null) {
            Text(
                state.message ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = NeoVedicTokens.SpaceLg, vertical = NeoVedicTokens.SpaceXs),
            )
        }

        if (manifest.isEmpty()) {
            NeoVedicEmptyState(
                title = "No content yet",
                description = "Tap refresh to fetch the SikAi content manifest.",
                icon = Icons.Filled.CloudDownload,
                action = {
                    NeoVedicButton("Refresh manifest", onClick = vm::refresh, loading = state.refreshing)
                },
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(NeoVedicTokens.SpaceLg),
            verticalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm),
        ) {
            items(manifest) { entry ->
                val progress = state.activeDownloads[entry.id]
                val isDownloaded = downloadedSet.contains(entry.id)
                NeoVedicCard(modifier = Modifier.fillMaxWidth(), showCornerMarkers = isDownloaded) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm)) {
                            NeoVedicStatusPill(entry.type.uppercase(), tone = StatusPillTone.Info)
                            NeoVedicStatusPill("Class ${entry.classLevel}", tone = StatusPillTone.Neutral)
                            if (isDownloaded) NeoVedicStatusPill("Downloaded", tone = StatusPillTone.Success)
                        }
                        Spacer(Modifier.height(NeoVedicTokens.SpaceXs))
                        Text(entry.title, style = MaterialTheme.typography.titleMedium)
                        Text("${entry.subject} • ${formatBytes(entry.sizeBytes)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (entry.description.isNotBlank()) {
                            Spacer(Modifier.height(NeoVedicTokens.SpaceXs))
                            Text(entry.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        when (progress) {
                            is DownloadProgress.InProgress -> {
                                Spacer(Modifier.height(NeoVedicTokens.SpaceSm))
                                LinearProgressIndicator(progress = { progress.percent / 100f }, modifier = Modifier.fillMaxWidth())
                                Text("${progress.percent}%", style = MaterialTheme.typography.labelSmall)
                            }
                            is DownloadProgress.Error -> {
                                Spacer(Modifier.height(NeoVedicTokens.SpaceSm))
                                Text("Error: ${progress.message}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                            else -> {}
                        }
                        Spacer(Modifier.height(NeoVedicTokens.SpaceSm))
                        Row(horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm)) {
                            if (!isDownloaded) {
                                NeoVedicButton(
                                    "Download",
                                    onClick = { vm.startDownload(entry) },
                                    leadingIcon = { Icon(Icons.Filled.CloudDownload, null) },
                                    loading = progress is DownloadProgress.InProgress || progress is DownloadProgress.Queued,
                                )
                            } else {
                                NeoVedicButton(
                                    "Delete",
                                    style = NeoVedicButtonStyle.Outline,
                                    onClick = { vm.delete(entry) },
                                    leadingIcon = { Icon(Icons.Filled.DeleteOutline, null) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    val mb = bytes / (1024.0 * 1024.0)
    return if (mb < 1) "${bytes / 1024} KB" else "%.1f MB".format(mb)
}
