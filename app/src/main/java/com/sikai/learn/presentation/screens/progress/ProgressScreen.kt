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
import com.sikai.learn.ui.components.SikAiCard
import com.sikai.learn.ui.components.SikAiEmptyState
import com.sikai.learn.ui.components.SikAiPageHeader
import com.sikai.learn.ui.components.SikAiSectionTitle
import com.sikai.learn.ui.components.SikAiStatusPill
import com.sikai.learn.ui.theme.SikAi
import com.sikai.learn.util.formatTimestamp

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val tokens = SikAi.tokens

    Column(modifier = Modifier.fillMaxSize()) {
        SikAiPageHeader(
            title = "Progress",
            subtitle = "STREAK · ATTEMPTS · WEAK SPOTS",
            trailing = {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = SikAi.colors.onSurface,
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
                SikAiCard() {
                    Column {
                        SikAiStatusPill(text = "STREAK")
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "${state.streakDays} day${if (state.streakDays == 1) "" else "s"} active",
                            style = SikAi.type.displayMedium,
                            color = SikAi.colors.onSurface,
                        )
                    }
                }
            }
            item { SikAiSectionTitle("Recent attempts") }
            if (state.attempts.isEmpty()) {
                item {
                    SikAiEmptyState(
                        title = "No quiz attempts yet",
                        description = "Take a quiz from the Quizzes tab to see your scores here."
                    )
                }
            } else {
                items(state.attempts, key = { it.id }) { attempt ->
                    SikAiCard {
                        Row {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(attempt.subject, style = SikAi.type.titleMedium, color = SikAi.colors.onSurface)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = formatTimestamp(attempt.finishedAtMillis),
                                    style = SikAi.type.caption,
                                    color = SikAi.colors.onSurfaceMuted,
                                )
                            }
                            Text(
                                text = "${attempt.correct}/${attempt.total}",
                                style = SikAi.type.titleLarge,
                                color = SikAi.colors.accent,
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
            item { SikAiSectionTitle("Weak topics") }
            if (state.weakTopics.isEmpty()) {
                item {
                    SikAiEmptyState(title = "Nothing flagged yet", description = "Take a few quizzes and SikAi will surface weak topics here.")
                }
            } else {
                items(state.weakTopics, key = { it.id }) { row ->
                    SikAiCard {
                        Row {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(row.topic, style = SikAi.type.titleMedium, color = SikAi.colors.onSurface)
                                Spacer(Modifier.height(2.dp))
                                Text(row.subject, style = SikAi.type.bodySmall, color = SikAi.colors.onSurfaceMuted)
                            }
                            SikAiStatusPill(text = "${(row.strengthScore * 100).toInt()}%", accent = SikAi.colors.danger)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}
