package com.sikai.learn.ui.screens.plan

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
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.sikai.learn.ui.components.NeoVedicButton
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
fun StudyPlanScreen(
    vm: StudyPlanViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val plan by vm.activePlan.collectAsState(initial = null)
    val tasks by vm.tasks.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        NeoVedicPageHeader(
            title = "Study Plan",
            subtitle = "Adaptive schedule mapped to your exam date",
            eyebrow = "Plan",
            onBack = onBack,
        )

        if (plan == null) {
            NeoVedicEmptyState(
                title = "No active plan",
                description = "Generate a personalized schedule from your class & exam date.",
                icon = Icons.Filled.AutoStories,
                action = { NeoVedicButton("Generate plan", onClick = vm::generatePlan) },
            )
            return@Column
        }

        Column(modifier = Modifier.fillMaxSize().padding(NeoVedicTokens.SpaceLg)) {
            NeoVedicCard(modifier = Modifier.fillMaxWidth(), showCornerMarkers = true) {
                Column {
                    Text(plan!!.title, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(NeoVedicTokens.SpaceXs))
                    Row(horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm)) {
                        NeoVedicStatusPill("Class ${plan!!.classLevel}", tone = StatusPillTone.Neutral)
                        plan!!.examDate?.let {
                            NeoVedicStatusPill("Exam ${formatDate(it)}", tone = StatusPillTone.Gold)
                        }
                        NeoVedicStatusPill("${tasks.count { !it.completed }} pending", tone = StatusPillTone.Info)
                    }
                }
            }
            Spacer(Modifier.height(NeoVedicTokens.SpaceMd))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm),
            ) {
                items(tasks) { task ->
                    NeoVedicCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = task.completed,
                                onCheckedChange = { vm.toggle(task, it) },
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(task.title, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "${formatDate(task.scheduledAt)} • ${task.durationMinutes} min",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(epoch: Long): String =
    SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(epoch))
