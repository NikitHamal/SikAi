package com.sikai.learn.ui.screens.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sikai.learn.ui.components.NeoVedicButton
import com.sikai.learn.ui.components.NeoVedicButtonStyle
import com.sikai.learn.ui.components.NeoVedicCard
import com.sikai.learn.ui.components.NeoVedicEmptyState
import com.sikai.learn.ui.components.NeoVedicPageHeader
import com.sikai.learn.ui.components.NeoVedicStatusPill
import com.sikai.learn.ui.components.StatusPillTone
import com.sikai.learn.ui.theme.NeoVedicTokens

@Composable
fun QuizScreen(
    vm: QuizViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by vm.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        NeoVedicPageHeader(
            title = "Quizzes",
            subtitle = "Sharpen with focused practice",
            eyebrow = "Practice",
            onBack = onBack,
        )

        when (state.phase) {
            QuizPhase.Setup -> SetupPhase(state, vm)
            QuizPhase.Running -> RunningPhase(state, vm)
            QuizPhase.Finished -> FinishedPhase(state, vm)
        }
    }
}

@Composable
private fun SetupPhase(state: QuizState, vm: QuizViewModel) {
    if (state.subjects.isEmpty()) {
        NeoVedicEmptyState(
            title = "No subjects yet",
            description = "Choose your subjects in onboarding or settings.",
            icon = Icons.Filled.Quiz,
        )
        return
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(NeoVedicTokens.SpaceLg),
        verticalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceMd),
    ) {
        Text(
            "Pick a subject to begin a 5-question quiz.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm),
        ) {
            items(state.subjects) { subject ->
                val selected = state.selectedSubject == subject
                NeoVedicCard(
                    modifier = Modifier.fillMaxWidth(),
                    showCornerMarkers = selected,
                    onClick = { vm.pickSubject(subject) },
                    border = BorderStroke(
                        NeoVedicTokens.StrokeHairline,
                        if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant,
                    ),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm),
                    ) {
                        Text(subject, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        if (selected) NeoVedicStatusPill("Selected", tone = StatusPillTone.Gold)
                        NeoVedicStatusPill("Class ${state.classLevel}", tone = StatusPillTone.Neutral)
                    }
                }
            }
        }
        NeoVedicButton(
            text = "Start quiz",
            onClick = vm::startQuiz,
            style = NeoVedicButtonStyle.Primary,
            enabled = state.selectedSubject != null,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun RunningPhase(state: QuizState, vm: QuizViewModel) {
    val q = state.questions.getOrNull(state.currentIndex) ?: return
    val options = vm.parseOptions(q)
    val selected = state.answers.getOrNull(state.currentIndex) ?: -1

    Column(
        modifier = Modifier.fillMaxSize().padding(NeoVedicTokens.SpaceLg),
        verticalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceMd),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm),
        ) {
            NeoVedicStatusPill("Q ${state.currentIndex + 1} / ${state.questions.size}", tone = StatusPillTone.Info)
            NeoVedicStatusPill(q.subject, tone = StatusPillTone.Neutral)
            if (q.topic.isNotBlank()) NeoVedicStatusPill(q.topic, tone = StatusPillTone.Gold)
        }
        LinearProgressIndicator(
            progress = { (state.currentIndex + 1).toFloat() / state.questions.size },
            modifier = Modifier.fillMaxWidth(),
        )
        NeoVedicCard(modifier = Modifier.fillMaxWidth(), showCornerMarkers = true) {
            Text(q.prompt, style = MaterialTheme.typography.titleMedium)
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm),
        ) {
            items(options) { option ->
                val idx = options.indexOf(option)
                val isSelected = idx == selected
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(2.dp))
                        .border(
                            BorderStroke(
                                NeoVedicTokens.StrokeHairline,
                                if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant,
                            ),
                            RoundedCornerShape(2.dp),
                        )
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.surface,
                        )
                        .clickable { vm.answer(idx) }
                        .padding(NeoVedicTokens.SpaceMd),
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(2.dp))
                            .border(
                                BorderStroke(NeoVedicTokens.StrokeHairline, MaterialTheme.colorScheme.outline),
                                RoundedCornerShape(2.dp),
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            ('A' + idx).toString(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Text(option, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                }
            }
        }
        NeoVedicButton(
            text = if (state.currentIndex == state.questions.lastIndex) "Finish" else "Next",
            onClick = vm::next,
            enabled = selected >= 0,
            modifier = Modifier.fillMaxWidth(),
            style = NeoVedicButtonStyle.Primary,
        )
    }
}

@Composable
private fun FinishedPhase(state: QuizState, vm: QuizViewModel) {
    val pct = if (state.totalCount == 0) 0 else (state.correctCount * 100) / state.totalCount
    Column(
        modifier = Modifier.fillMaxSize().padding(NeoVedicTokens.SpaceLg),
        verticalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceMd),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(NeoVedicTokens.SpaceLg))
        NeoVedicCard(modifier = Modifier.fillMaxWidth(), showCornerMarkers = true) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("RESULT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.height(NeoVedicTokens.SpaceXs))
                Text(
                    "${state.correctCount} / ${state.totalCount}",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text("$pct% correct", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(NeoVedicTokens.SpaceSm))
                Text(
                    "+${state.correctCount * 10} XP earned",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
        Spacer(Modifier.weight(1f))
        NeoVedicButton(
            text = "Try another",
            onClick = vm::restart,
            modifier = Modifier.fillMaxWidth(),
            style = NeoVedicButtonStyle.Primary,
        )
    }
}
