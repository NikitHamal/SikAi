package com.sikai.learn.ui.screens.progress

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
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.LinearProgressIndicator
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProgressScreen(
    vm: ProgressViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val attempts by vm.attempts.collectAsState(initial = emptyList())
    val weak by vm.weakTopics.collectAsState(initial = emptyList())
    val streak by vm.streak.collectAsState(initial = 0)
    val xp by vm.xp.collectAsState(initial = 0)

    Column(modifier = Modifier.fillMaxSize()) {
        NeoVedicPageHeader(
            title = "Progress",
            subtitle = "Streak, XP, recent attempts and weak topics",
            eyebrow = "Insights",
            onBack = onBack,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(NeoVedicTokens.SpaceLg),
            verticalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm),
        ) {
            item {
                NeoVedicCard(modifier = Modifier.fillMaxWidth(), showCornerMarkers = true) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceLg),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Stat("STREAK", "$streak d")
                        Stat("XP", "$xp")
                        Stat("ATTEMPTS", "${attempts.size}")
                    }
                }
            }
            if (weak.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(NeoVedicTokens.SpaceSm))
                    Text("WEAK TOPICS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                }
                items(weak) { w ->
                    NeoVedicCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(w.topic, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                NeoVedicStatusPill(w.subject, tone = StatusPillTone.Neutral)
                            }
                            Spacer(Modifier.height(NeoVedicTokens.SpaceXs))
                            LinearProgressIndicator(
                                progress = { w.score },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Text(
                                "${(w.score * 100).toInt()}% mastery • ${w.attempts} questions",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            if (attempts.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(NeoVedicTokens.SpaceSm))
                    Text("RECENT ATTEMPTS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                }
                items(attempts) { a ->
                    NeoVedicCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(a.subject, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                NeoVedicStatusPill(
                                    "${a.correctCount}/${a.totalQuestions}",
                                    tone = if (a.correctCount * 2 >= a.totalQuestions) StatusPillTone.Success else StatusPillTone.Warn,
                                )
                            }
                            Text(
                                formatTime(a.startedAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                item {
                    NeoVedicEmptyState(
                        title = "No attempts yet",
                        description = "Take a quiz to start tracking progress.",
                        icon = Icons.Filled.Insights,
                    )
                }
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
    }
}

private fun formatTime(epoch: Long): String =
    SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(epoch))
