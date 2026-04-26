package com.sikai.learn.presentation.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.sikai.learn.core.design.*
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(onDone: () -> Unit, vm: OnboardingViewModel = hiltViewModel()) {
    var classLevel by remember { mutableIntStateOf(10) }
    var language by remember { mutableStateOf("en") }
    var examDate by remember { mutableStateOf("") }
    val available = listOf("English", "Nepali", "Mathematics", "Science", "Social Studies", "Physics", "Chemistry", "Biology", "Computer Science", "Account", "Economics")
    val selected = remember { mutableStateListOf("Mathematics", "Science", "English") }
    ScreenScaffold("SikAi", "AI learning for Nepal.") {
        NeoVedicCard(Modifier.fillMaxWidth(), emphasized = true) {
            Text("A calm, offline-first study companion for Class 8, SEE, and NEB board preparation.", style = MaterialTheme.typography.bodyLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NeoVedicStatusPill("No login needed")
                NeoVedicStatusPill("Secure local keys")
            }
        }
        NeoVedicSectionTitle("Choose class")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf(8, 10, 12).forEach { FilterChip(selected = classLevel == it, onClick = { classLevel = it }, label = { Text("Class $it") }) } }
        NeoVedicSectionTitle("Language")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("en" to "English", "ne" to "Nepali-ready").forEach { FilterChip(selected = language == it.first, onClick = { language = it.first }, label = { Text(it.second) }) } }
        NeoVedicSectionTitle("Subjects")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            available.forEach { subject -> FilterChip(selected = subject in selected, onClick = { if (subject in selected) selected.remove(subject) else selected.add(subject) }, label = { Text(subject) }) }
        }
        NeoVedicTextField(examDate, { examDate = it }, "Optional exam date (YYYY-MM-DD)", Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NeoVedicCard(Modifier.weight(1f)) { androidx.compose.material3.Icon(Icons.Outlined.DownloadForOffline, null); Text("Download packs for offline study.") }
            NeoVedicCard(Modifier.weight(1f)) { androidx.compose.material3.Icon(Icons.Outlined.Key, null); Text("Add provider keys only on this device.") }
        }
        NeoVedicButton("Start learning", Modifier.fillMaxWidth()) { vm.complete(classLevel, language, selected, examDate, onDone) }
    }
}
