package com.sikai.learn.presentation.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sikai.learn.ui.components.SikAiButton
import com.sikai.learn.ui.components.SikAiButtonVariant
import com.sikai.learn.ui.components.SikAiCard
import com.sikai.learn.ui.components.SikAiSectionTitle
import com.sikai.learn.ui.theme.SikAi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onCompleted: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.finished) { if (state.finished) onCompleted() }

    val colors = SikAi.colors
    val tokens = SikAi.tokens

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = tokens.pageHorizontal)) {
        Spacer(Modifier.height(48.dp))
        Text(text = "Welcome to SikAi", style = SikAi.type.displayLarge, color = colors.onSurface)
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Your AI study companion for Class 8, SEE, and NEB.",
            style = SikAi.type.bodyLarge,
            color = colors.onSurfaceMuted
        )
        Spacer(Modifier.height(28.dp))
        StepIndicator(step = state.step, total = 3)
        Spacer(Modifier.height(20.dp))

        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when (state.step) {
                0 -> ClassPicker(state, viewModel::selectClass)
                1 -> SubjectPicker(state, viewModel::toggleSubject)
                else -> StudyHabitPicker(state, viewModel::setStudyMinutes)
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
            if (state.step > 0) {
                SikAiButton(
                    text = "Back",
                    onClick = viewModel::previous,
                    variant = SikAiButtonVariant.Secondary,
                    leadingIcon = Icons.Outlined.ArrowBack,
                )
                Spacer(Modifier.width(12.dp))
            }
            Spacer(Modifier.weight(1f))
            if (state.step < 2) {
                SikAiButton(
                    text = "Continue",
                    onClick = viewModel::next,
                    enabled = state.canAdvance,
                    leadingIcon = Icons.Outlined.ArrowForward,
                )
            } else {
                SikAiButton(
                    text = if (state.saving) "Saving…" else "Get started",
                    onClick = viewModel::finish,
                    enabled = state.canAdvance && !state.saving,
                    leadingIcon = Icons.Outlined.Check,
                )
            }
        }
    }
}

@Composable
private fun StepIndicator(step: Int, total: Int) {
    val colors = SikAi.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(total) { idx ->
            val active = idx <= step
            Box(
                modifier = Modifier
                    .height(2.dp)
                    .width(48.dp)
                    .padding(end = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 0.dp)
                        .size(48.dp, 2.dp),
                ) {
                    androidx.compose.foundation.layout.Spacer(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding()
                    )
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(if (active) colors.accent else colors.borderSubtle)
                    }
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(text = "Step ${step + 1} of $total", style = SikAi.type.label, color = colors.onSurfaceMuted)
    }
}

@Composable
private fun ClassPicker(state: OnboardingState, onSelect: (Int) -> Unit) {
    val colors = SikAi.colors
    Column {
        SikAiSectionTitle("Pick your class")
        Spacer(Modifier.height(12.dp))
        listOf(
            8 to "Class 8" to "Lower secondary basics",
            10 to "Class 10 (SEE)" to "Secondary Education Examination",
            12 to "Class 12 (NEB)" to "National Examinations Board",
        ).forEach { (pair, subtitle) ->
            val (cls, title) = pair
            val selected = state.classLevel == cls
            SikAiCard(
                onClick = { onSelect(cls) },
                emphasized = selected,
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(text = title, style = SikAi.type.titleMedium, color = colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(2.dp))
                        Text(text = subtitle, style = SikAi.type.bodySmall, color = colors.onSurfaceMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (selected) {
                        Icon(Icons.Outlined.Check, contentDescription = null, tint = colors.accent)
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectPicker(state: OnboardingState, onToggle: (String) -> Unit) {
    val colors = SikAi.colors
    Column {
        SikAiSectionTitle("Pick your subjects")
        Spacer(Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(state.availableSubjects, key = { it.id }) { subject ->
                val selected = subject.id in state.selectedSubjectIds
                SikAiCard(
                    onClick = { onToggle(subject.id) },
                    emphasized = selected,
                    modifier = Modifier.heightIn(min = 64.dp).fillMaxWidth(),
                    contentPadding = 12.dp,
                ) {
                    Text(
                        text = subject.displayName,
                        style = SikAi.type.bodyMedium,
                        color = if (selected) colors.onSurface else colors.onSurfaceMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun StudyHabitPicker(state: OnboardingState, onSet: (Int) -> Unit) {
    val colors = SikAi.colors
    Column {
        SikAiSectionTitle("Daily study target")
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(listOf(15, 30, 60, 90, 120)) { mins ->
                val selected = state.studyMinutes == mins
                SikAiCard(
                    onClick = { onSet(mins) },
                    emphasized = selected,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$mins min / day",
                            style = SikAi.type.titleMedium,
                            color = colors.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (selected) Icon(Icons.Outlined.Check, contentDescription = null, tint = colors.accent)
                    }
                }
            }
        }
    }
}
