package com.sikai.learn.presentation.quiz

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.FilterChip
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.sikai.learn.core.design.*
import androidx.compose.ui.unit.dp

@Composable
fun QuizScreen(nav: NavHostController, vm: QuizViewModel = hiltViewModel()) {
    val state by vm.state
    var classLevel by remember { mutableIntStateOf(10) }
    var subject by remember { mutableStateOf("Mathematics") }
    LaunchedEffect(classLevel, subject) { vm.load(classLevel, subject) }
    ScreenScaffold("Quizzes", "Local question bank with AI practice expansion") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf(8, 10, 12).forEach { FilterChip(classLevel == it, { classLevel = it }, label = { Text("Class $it") }) } }
        NeoVedicTextField(subject, { subject = it }, "Subject", Modifier.fillMaxWidth())
        if (state.questions.isEmpty()) NeoVedicEmptyState("No local questions", "Try Mathematics, Science, English, or Physics.")
        else if (state.completed) NeoVedicCard(Modifier.fillMaxWidth(), emphasized = true) { Text("Score: ${state.correct}/${state.questions.size}"); NeoVedicButton("Practice weak topics") { nav.navigate("progress") } }
        else {
            val q = state.questions[state.index]
            NeoVedicCard(Modifier.fillMaxWidth(), emphasized = true) {
                NeoVedicStatusPill("${state.index + 1}/${state.questions.size} · ${q.topic}")
                Text(q.prompt)
                q.optionsCsv.split("|").forEachIndexed { index, option ->
                    Row(Modifier.fillMaxWidth().clickable { vm.answer(q, index) }, verticalAlignment = Alignment.CenterVertically) { RadioButton(state.selected[q.id] == index, { vm.answer(q, index) }); Text(option) }
                }
                state.selected[q.id]?.let { Text(q.explanation) }
                NeoVedicButton(if (state.index == state.questions.lastIndex) "Finish" else "Next", Modifier.fillMaxWidth(), enabled = state.selected[q.id] != null) { vm.nextOrFinish(classLevel, subject) }
            }
        }
    }
}
