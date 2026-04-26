package com.sikai.learn.presentation.screens.papers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sikai.learn.ui.components.DownloadCardState
import com.sikai.learn.ui.components.NeoVedicButton
import com.sikai.learn.ui.components.NeoVedicButtonVariant
import com.sikai.learn.ui.components.NeoVedicDownloadCard
import com.sikai.learn.ui.components.NeoVedicEmptyState
import com.sikai.learn.ui.components.NeoVedicPageHeader
import com.sikai.learn.ui.theme.NeoVedic
import com.sikai.learn.util.formatBytes

@Composable
fun PastPapersScreen(
    viewModel: PastPapersViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onOpenDownloads: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val tokens = NeoVedic.tokens

    Column(modifier = Modifier.fillMaxSize()) {
        NeoVedicPageHeader(
            title = "Past Papers",
            subtitle = state.classLevel?.let { "CLASS $it · OFFICIAL ARCHIVE" } ?: "PICK A CLASS FIRST",
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
                text = if (state.refreshing) "Syncing…" else "Sync from server",
                onClick = viewModel::refresh,
                variant = NeoVedicButtonVariant.Secondary,
                leadingIcon = Icons.Outlined.CloudSync,
                enabled = state.classLevel != null && !state.refreshing,
            )
            Spacer(Modifier.weight(1f))
            NeoVedicButton(
                text = "Downloads",
                onClick = onOpenDownloads,
                variant = NeoVedicButtonVariant.Ghost,
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

        if (state.papers.isEmpty()) {
            NeoVedicEmptyState(
                title = "No past papers yet",
                description = "Tap 'Sync from server' to fetch the manifest, then download what you need."
            )
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = tokens.pageHorizontal, vertical = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.papers, key = { it.id }) { row ->
                    NeoVedicDownloadCard(
                        title = row.title,
                        subtitle = listOfNotNull(row.subject, row.year?.toString()).joinToString(" · "),
                        sizeLabel = formatBytes(row.sizeBytes),
                        state = if (row.isDownloaded) DownloadCardState.Downloaded else DownloadCardState.Available,
                        onClick = onOpenDownloads,
                    )
                }
            }
        }
    }
}
