package com.sikai.learn.presentation.tutor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.sikai.learn.core.design.*
import com.sikai.learn.domain.ai.AiPromptMode
import androidx.compose.ui.unit.dp

@Composable
fun TutorScreen(nav: NavHostController, vm: TutorViewModel = hiltViewModel()) {
    val state by vm.state
    val profile by vm.profile.collectAsState()
    var prompt by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf(profile?.subjectsCsv?.substringBefore(',') ?: "Mathematics") }
    ScreenScaffold("AI Tutor", "Socratic, simple, or exam-focused answers", action = { TextButton({ nav.navigate("settings") }) { Text("Providers") } }) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(AiPromptMode.SOCRATIC to "Socratic", AiPromptMode.SIMPLE to "Simple", AiPromptMode.EXAM to "Exam", AiPromptMode.DIRECT to "Direct").forEach { (mode, label) ->
                FilterChip(selected = state.mode == mode, onClick = { vm.setMode(mode) }, label = { Text(label) })
            }
        }
        NeoVedicTextField(subject, { subject = it }, "Subject", Modifier.fillMaxWidth())
        NeoVedicTextField(prompt, { prompt = it }, "Ask SikAi anything", Modifier.fillMaxWidth().heightIn(min = 120.dp), singleLine = false)
        NeoVedicButton(if (state.loading) "Thinking…" else "Ask tutor", Modifier.fillMaxWidth(), enabled = !state.loading) { vm.ask(prompt, subject) }
        state.error?.let { NeoVedicEmptyState("Provider failed", it) { NeoVedicButton("Open provider settings") { nav.navigate("settings") } } }
        state.answer?.let { NeoVedicAiAnswerCard(it.text, it.providerName, onSave = vm::saveAnswer) }
    }
}
