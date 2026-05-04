package com.sikai.learn.presentation.screens.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
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
            title = "Progress"
        )
        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = tokens.pageHorizontal, vertical = 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                SikAiCard {
                    Column {
                        SikAiStatusPill(text = "STREAK")
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "${state.streakDays} day${if (state.streakDays == 1) "" else "s"} active",
                            style = SikAi.type.displayMedium,
                            color = MaterialTheme.colorScheme.onSurface,
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
                                Text(
                                    text = attempt.subject, 
                                    style = SikAi.type.titleMedium, 
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = formatTimestamp(attempt.finishedAtMillis),
                                    style = SikAi.type.caption,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                text = "${attempt.correct}/${attempt.total}",
                                style = SikAi.type.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
            item { SikAiSectionTitle("Weak topics") }
            if (state.weakTopics.isEmpty()) {
                item {
                    SikAiEmptyState(
                        title = "Nothing flagged yet", 
                        description = "Take a few quizzes and SikAi will surface weak topics here."
                    )
                }
            } else {
                items(state.weakTopics, key = { it.id }) { row ->
                    SikAiCard {
                        Row {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = row.topic, 
                                    style = SikAi.type.titleMedium, 
                                    color = MaterialTheme.colorScheme.onSurface, 
                                    maxLines = 1, 
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = row.subject, 
                                    style = SikAi.type.bodySmall, 
                                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
                                    maxLines = 1, 
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            SikAiStatusPill(
                                text = "${(row.strengthScore * 100).toInt()}%", 
                                accent = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}
