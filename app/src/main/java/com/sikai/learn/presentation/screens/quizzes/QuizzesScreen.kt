package com.sikai.learn.presentation.screens.quizzes

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
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sikai.learn.domain.model.QuizQuestion
import com.sikai.learn.ui.components.NeoVedicButton
import com.sikai.learn.ui.components.NeoVedicCard
import com.sikai.learn.ui.components.NeoVedicEmptyState
import com.sikai.learn.ui.components.NeoVedicPageHeader
import com.sikai.learn.ui.components.NeoVedicSectionTitle
import com.sikai.learn.ui.components.NeoVedicStatusPill
import com.sikai.learn.ui.theme.NeoVedic

@Composable
fun QuizzesScreen(
    viewModel: QuizzesViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val tokens = NeoVedic.tokens

    Column(modifier = Modifier.fillMaxSize()) {
        NeoVedicPageHeader(
            title = "Quizzes",
            subtitle = "MCQ · MIXED · AI",
            trailing = {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = NeoVedic.colors.onSurface,
                    modifier = Modifier.size(28.dp).clickable(onClick = onBack)
                )
            }
        )

        if (state.questions.isEmpty() && !state.loading) {
            Column(modifier = Modifier.padding(horizontal = tokens.pageHorizontal, vertical = 12.dp)) {
                NeoVedicSectionTitle("Pick a subject")
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.subjects.forEach { subject ->
                        NeoVedicCard(
                            onClick = { viewModel.selectSubject(subject.id) },
                            emphasized = subject.id == state.selectedSubjectId,
                            contentPadding = 10.dp,
                        ) {
                            Text(subject.displayName, style = NeoVedic.type.label, color = NeoVedic.colors.onSurface)
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                NeoVedicButton(
                    text = "Start a 8-question quiz",
                    onClick = viewModel::startQuiz,
                    leadingIcon = Icons.Outlined.PlayArrow,
                    enabled = state.classLevel != null,
                )
                if (state.errorMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(state.errorMessage!!, color = NeoVedic.colors.danger, style = NeoVedic.type.bodySmall)
                }
            }
            return
        }

        if (state.loading) {
            NeoVedicEmptyState(title = "Building quiz…", description = "SikAi is generating MCQs.")
            return
        }

        if (state.finished) {
            ResultsBlock(state, viewModel::reset)
            return
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = tokens.pageHorizontal, vertical = 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.questions, key = { it.id }) { q ->
                QuestionCard(
                    question = q,
                    selectedIndex = state.selected[q.id],
                    onSelect = { idx -> viewModel.selectAnswer(q.id, idx) }
                )
            }
            item {
                Spacer(Modifier.height(12.dp))
                NeoVedicButton(
                    text = "Submit",
                    onClick = viewModel::submit,
                    enabled = state.selected.size == state.questions.size,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun QuestionCard(question: QuizQuestion, selectedIndex: Int?, onSelect: (Int) -> Unit) {
    NeoVedicCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(question.prompt, style = NeoVedic.type.titleMedium, color = NeoVedic.colors.onSurface)
            Spacer(Modifier.height(10.dp))
            question.options.forEachIndexed { idx, option ->
                val isSelected = selectedIndex == idx
                NeoVedicCard(
                    onClick = { onSelect(idx) },
                    emphasized = isSelected,
                    contentPadding = 10.dp,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${('A' + idx)}.",
                            style = NeoVedic.type.label,
                            color = NeoVedic.colors.accent,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(option, style = NeoVedic.type.bodyMedium, color = NeoVedic.colors.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultsBlock(state: QuizzesState, onReset: () -> Unit) {
    val tokens = NeoVedic.tokens
    val correct = state.questions.count { state.selected[it.id] == it.correctIndex }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = tokens.pageHorizontal, vertical = 12.dp)) {
        NeoVedicCard(showCornerMarkers = true, emphasized = true) {
            Column {
                NeoVedicStatusPill(text = "RESULT")
                Spacer(Modifier.height(10.dp))
                Text("$correct / ${state.questions.size} correct",
                    style = NeoVedic.type.displayMedium,
                    color = NeoVedic.colors.onSurface)
            }
        }
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(state.questions, key = { it.id }) { q ->
                val chosen = state.selected[q.id]
                NeoVedicCard {
                    Column {
                        Text(q.prompt, style = NeoVedic.type.titleMedium, color = NeoVedic.colors.onSurface)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Correct: ${q.options.getOrNull(q.correctIndex) ?: "—"}",
                            style = NeoVedic.type.bodySmall,
                            color = NeoVedic.colors.accent,
                        )
                        if (chosen != null && chosen != q.correctIndex) {
                            Text(
                                text = "You: ${q.options.getOrNull(chosen) ?: "—"}",
                                style = NeoVedic.type.bodySmall,
                                color = NeoVedic.colors.danger,
                            )
                        }
                        if (q.explanation != null) {
                            Spacer(Modifier.height(4.dp))
                            Text(q.explanation!!, style = NeoVedic.type.bodySmall, color = NeoVedic.colors.onSurfaceMuted)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        NeoVedicButton(text = "New quiz", onClick = onReset, leadingIcon = Icons.Outlined.RestartAlt, modifier = Modifier.fillMaxWidth())
    }
}
