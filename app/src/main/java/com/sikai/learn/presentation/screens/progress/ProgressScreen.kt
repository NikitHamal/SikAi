package com.sikai.learn.presentation.screens.progress

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sikai.learn.ui.components.NeoVedicCard
import com.sikai.learn.ui.components.NeoVedicEmptyState
import com.sikai.learn.ui.components.NeoVedicPageHeader
import com.sikai.learn.ui.components.NeoVedicSectionTitle
import com.sikai.learn.ui.components.NeoVedicStatusPill
import com.sikai.learn.ui.theme.NeoVedic
import com.sikai.learn.util.formatTimestamp

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val tokens = NeoVedic.tokens

    Column(modifier = Modifier.fillMaxSize()) {
        NeoVedicPageHeader(
            title = "Progress",
            subtitle = "STREAK · ATTEMPTS · WEAK SPOTS",
            trailing = {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = NeoVedic.colors.onSurface,
                    modifier = Modifier.size(28.dp).clickable(onClick = onBack)
                )
            }
        )
        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = tokens.pageHorizontal, vertical = 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                NeoVedicCard(showCornerMarkers = true) {
                    Column {
                        NeoVedicStatusPill(text = "STREAK")
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "${state.streakDays} day${if (state.streakDays == 1) "" else "s"} active",
                            style = NeoVedic.type.displayMedium,
                            color = NeoVedic.colors.onSurface,
                        )
                    }
                }
            }
            item { NeoVedicSectionTitle("Recent attempts") }
            if (state.attempts.isEmpty()) {
                item {
                    NeoVedicEmptyState(
                        title = "No quiz attempts yet",
                        description = "Take a quiz from the Quizzes tab to see your scores here."
                    )
                }
            } else {
                items(state.attempts, key = { it.id }) { attempt ->
                    NeoVedicCard {
                        Row {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(attempt.subject, style = NeoVedic.type.titleMedium, color = NeoVedic.colors.onSurface)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = formatTimestamp(attempt.finishedAtMillis),
                                    style = NeoVedic.type.caption,
                                    color = NeoVedic.colors.onSurfaceMuted,
                                )
                            }
                            Text(
                                text = "${attempt.correct}/${attempt.total}",
                                style = NeoVedic.type.titleLarge,
                                color = NeoVedic.colors.accent,
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
            item { NeoVedicSectionTitle("Weak topics") }
            if (state.weakTopics.isEmpty()) {
                item {
                    NeoVedicEmptyState(title = "Nothing flagged yet", description = "Take a few quizzes and SikAi will surface weak topics here.")
                }
            } else {
                items(state.weakTopics, key = { it.id }) { row ->
                    NeoVedicCard {
                        Row {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(row.topic, style = NeoVedic.type.titleMedium, color = NeoVedic.colors.onSurface)
                                Spacer(Modifier.height(2.dp))
                                Text(row.subject, style = NeoVedic.type.bodySmall, color = NeoVedic.colors.onSurfaceMuted)
                            }
                            NeoVedicStatusPill(text = "${(row.strengthScore * 100).toInt()}%", accent = NeoVedic.colors.danger)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}
