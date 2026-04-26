package com.sikai.learn.presentation.notes

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.sikai.learn.core.design.*
import androidx.compose.ui.unit.dp

@Composable
fun NotesScreen(nav: NavHostController, vm: NotesViewModel = hiltViewModel()) {
    val notes by vm.notes.collectAsState()
    val saved by vm.savedAnswers.collectAsState()
    val summary by vm.summary
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Mathematics") }
    ScreenScaffold("Notes", "Offline notes, saved AI answers, summaries, and flashcards") {
        NeoVedicCard(Modifier.fillMaxWidth(), emphasized = true) {
            NeoVedicTextField(title, { title = it }, "Title", Modifier.fillMaxWidth())
            NeoVedicTextField(subject, { subject = it }, "Subject", Modifier.fillMaxWidth())
            NeoVedicTextField(body, { body = it }, "Write a note", Modifier.fillMaxWidth().heightIn(min = 120.dp), singleLine = false)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { NeoVedicButton("Save") { vm.save(title, body, subject) }; NeoVedicButton("AI summarize", secondary = true) { vm.summarize(body) } }
        }
        summary?.let { NeoVedicAiAnswerCard(it, "Selected provider") }
        NeoVedicSectionTitle("Manual notes")
        notes.forEach { NeoVedicCard(Modifier.fillMaxWidth()) { Text(it.title); Text(it.body.take(180)) } }
        NeoVedicSectionTitle("Saved AI answers")
        saved.forEach { NeoVedicCard(Modifier.fillMaxWidth()) { NeoVedicStatusPill(it.providerName); Text(it.prompt); MarkdownText(it.answer.take(500)) } }
    }
}
