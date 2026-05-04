package com.sikai.learn.presentation.screens.studyplan

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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sikai.learn.ui.components.SikAiButton
import com.sikai.learn.ui.components.SikAiCard
import com.sikai.learn.ui.components.SikAiEmptyState
import com.sikai.learn.ui.components.SikAiPageHeader
import com.sikai.learn.ui.components.SikAiStatusPill
import com.sikai.learn.ui.theme.SikAi

@Composable
fun StudyPlanScreen(
    viewModel: StudyPlanViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val tokens = SikAi.tokens

    Column(modifier = Modifier.fillMaxSize()) {
        SikAiPageHeader(
            title = "Study Plan"
        )

        if (state.plan == null) {
            Column(modifier = Modifier.padding(tokens.pageHorizontal)) {
                SikAiEmptyState(
                    title = "No plan yet",
                    description = "Generate a plan tailored to your subjects, study minutes, and exam date."
                )
                SikAiButton(
                    text = "Generate study plan",
                    onClick = viewModel::generatePlan,
                    leadingIcon = Icons.Outlined.AutoAwesome,
                    enabled = state.classLevel != null && state.subjectIds.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            return
        }

        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = tokens.pageHorizontal, vertical = 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SikAiCard() {
                    Column {
                        SikAiStatusPill(text = "ACTIVE PLAN")
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = state.plan?.title.orEmpty(), 
                            style = SikAi.type.titleLarge, 
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${state.tasks.size} tasks · ${state.studyMinutesPerDay} min / day",
                            style = SikAi.type.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            items(state.tasks, key = { it.id }) { task ->
                SikAiCard(onClick = { viewModel.toggleTaskComplete(task) }, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (task.isCompleted) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        )
                        Spacer(Modifier.size(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Day ${task.dayIndex + 1} · ${task.subject}",
                                style = SikAi.type.label,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = task.title, 
                                style = SikAi.type.titleMedium, 
                                color = MaterialTheme.colorScheme.onSurface, 
                                maxLines = 1, 
                                overflow = TextOverflow.Ellipsis
                            )
                            if (task.description != null) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = task.description,
                                    style = SikAi.type.bodySmall,
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
