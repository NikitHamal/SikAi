package com.sikai.learn.presentation.screens.downloads

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sikai.learn.data.download.DownloadProgress
import com.sikai.learn.ui.components.DownloadCardState
import com.sikai.learn.ui.components.NeoVedicButton
import com.sikai.learn.ui.components.NeoVedicButtonVariant
import com.sikai.learn.ui.components.NeoVedicDownloadCard
import com.sikai.learn.ui.components.NeoVedicEmptyState
import com.sikai.learn.ui.components.NeoVedicPageHeader
import com.sikai.learn.ui.theme.NeoVedic
import com.sikai.learn.util.formatBytes

@Composable
fun DownloadsScreen(
    viewModel: DownloadsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val tokens = NeoVedic.tokens

    Column(modifier = Modifier.fillMaxSize()) {
        NeoVedicPageHeader(
            title = "Downloads",
            subtitle = "OFFLINE READY · VERIFIED",
            trailing = {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = NeoVedic.colors.onSurface,
                    modifier = Modifier.size(28.dp).clickable(onClick = onBack)
                )
            }
        )
        Row(modifier = Modifier.fillMaxWidth().padding(tokens.pageHorizontal)) {
            NeoVedicButton(
                text = if (state.refreshing) "Syncing…" else "Sync manifest",
                onClick = viewModel::refresh,
                variant = NeoVedicButtonVariant.Secondary,
                leadingIcon = Icons.Outlined.CloudSync,
                enabled = !state.refreshing,
            )
        }

        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage ?: "",
                style = NeoVedic.type.bodySmall,
                color = NeoVedic.colors.danger,
                modifier = Modifier.padding(horizontal = tokens.pageHorizontal)
            )
        }

        if (state.rows.isEmpty()) {
            NeoVedicEmptyState(
                title = "Nothing to download yet",
                description = "Sync the manifest to see textbooks, past papers, and MCQ packs available for offline use."
            )
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = tokens.pageHorizontal, vertical = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.rows, key = { it.id }) { row ->
                    val progress = state.progressById[row.id]
                    val cardState = when {
                        row.isDownloaded -> DownloadCardState.Downloaded
                        progress == DownloadProgress.Running || progress == DownloadProgress.Queued ->
                            DownloadCardState.Downloading
                        else -> DownloadCardState.Available
                    }
                    NeoVedicDownloadCard(
                        title = row.title,
                        subtitle = listOfNotNull(
                            row.subject.takeIf { it.isNotBlank() },
                            row.year?.toString(),
                            row.type.replace('_', ' '),
                        ).joinToString(" · "),
                        sizeLabel = formatBytes(row.sizeBytes),
                        state = cardState,
                        onClick = {
                            when (cardState) {
                                DownloadCardState.Available -> viewModel.startDownload(row.id)
                                DownloadCardState.Downloading -> viewModel.cancelDownload(row.id)
                                DownloadCardState.Downloaded -> Unit
                            }
                        },
                    )
                }
                item { Spacer(Modifier.size(40.dp)) }
            }
        }
    }
}
