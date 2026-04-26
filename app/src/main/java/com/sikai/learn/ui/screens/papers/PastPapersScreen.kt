package com.sikai.learn.ui.screens.papers

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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.sikai.learn.ui.components.NeoVedicCard
import com.sikai.learn.ui.components.NeoVedicEmptyState
import com.sikai.learn.ui.components.NeoVedicPageHeader
import com.sikai.learn.ui.components.NeoVedicStatusPill
import com.sikai.learn.ui.components.StatusPillTone
import com.sikai.learn.ui.theme.NeoVedicTokens

@Composable
fun PastPapersScreen(
    vm: PastPapersViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onOpenDownloads: () -> Unit,
) {
    val papers by vm.papers.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        NeoVedicPageHeader(
            title = "Past Papers",
            subtitle = "Browse SEE & NEB archives. Download to study offline.",
            eyebrow = "Archive",
            onBack = onBack,
            actions = {
                IconButton(onClick = onOpenDownloads) {
                    Icon(Icons.Filled.CloudDownload, contentDescription = "Downloads", tint = MaterialTheme.colorScheme.secondary)
                }
            },
        )

        if (papers.isEmpty()) {
            NeoVedicEmptyState(
                title = "No papers yet",
                description = "Pull-to-refresh or check your connection.",
                icon = Icons.Filled.Description,
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(NeoVedicTokens.SpaceLg),
            verticalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm),
        ) {
            items(papers) { paper ->
                NeoVedicCard(modifier = Modifier.fillMaxWidth(), showCornerMarkers = true) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm)) {
                            NeoVedicStatusPill(paper.board, tone = StatusPillTone.Gold)
                            Text(
                                "${paper.year}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.height(NeoVedicTokens.SpaceXs))
                        Text(paper.title, style = MaterialTheme.typography.titleMedium)
                        Text(paper.subject, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
