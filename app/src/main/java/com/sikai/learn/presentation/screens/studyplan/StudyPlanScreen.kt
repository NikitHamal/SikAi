package com.sikai.learn.presentation.screens.studyplan

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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sikai.learn.ui.components.NeoVedicButton
import com.sikai.learn.ui.components.NeoVedicCard
import com.sikai.learn.ui.components.NeoVedicEmptyState
import com.sikai.learn.ui.components.NeoVedicPageHeader
import com.sikai.learn.ui.components.NeoVedicStatusPill
import com.sikai.learn.ui.theme.NeoVedic

@Composable
fun StudyPlanScreen(
    viewModel: StudyPlanViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val tokens = NeoVedic.tokens

    Column(modifier = Modifier.fillMaxSize()) {
        NeoVedicPageHeader(
            title = "Study Plan",
            subtitle = "DAY-BY-DAY",
            trailing = {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = NeoVedic.colors.onSurface,
                    modifier = Modifier.size(28.dp).clickable(onClick = onBack)
                )
            }
        )

        if (state.plan == null) {
            Column(modifier = Modifier.padding(tokens.pageHorizontal)) {
                NeoVedicEmptyState(
                    title = "No plan yet",
                    description = "Generate a plan tailored to your subjects, study minutes, and exam date."
                )
                NeoVedicButton(
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
                NeoVedicCard(showCornerMarkers = true) {
                    Column {
                        NeoVedicStatusPill(text = "ACTIVE PLAN")
                        Spacer(Modifier.height(8.dp))
                        Text(state.plan?.title.orEmpty(), style = NeoVedic.type.titleLarge, color = NeoVedic.colors.onSurface)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${state.tasks.size} tasks · ${state.studyMinutesPerDay} min / day",
                            style = NeoVedic.type.bodySmall,
                            color = NeoVedic.colors.onSurfaceMuted
                        )
                    }
                }
            }
            items(state.tasks, key = { it.id }) { task ->
                NeoVedicCard(onClick = { viewModel.toggleTaskComplete(task) }, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (task.isCompleted) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (task.isCompleted) NeoVedic.colors.accent else NeoVedic.colors.borderStrong,
                        )
                        Spacer(Modifier.size(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Day ${task.dayIndex + 1} · ${task.subject}",
                                style = NeoVedic.type.label,
                                color = NeoVedic.colors.onSurfaceMuted,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(task.title, style = NeoVedic.type.titleMedium, color = NeoVedic.colors.onSurface)
                            if (task.description != null) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = task.description,
                                    style = NeoVedic.type.bodySmall,
                                    color = NeoVedic.colors.onSurfaceMuted,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
