package com.sikai.learn.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.sikai.learn.core.design.*
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(nav: NavHostController, vm: HomeViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val profile = state.profile
    ScreenScaffold("Namaste, student", "Class ${profile?.classLevel ?: 10} · ${profile?.subjectsCsv ?: "Mathematics"}", action = { IconButton({ nav.navigate("settings") }) { androidx.compose.material3.Icon(Icons.Outlined.Settings, null) } }) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NeoVedicCard(Modifier.weight(1f), emphasized = true) { Text("${profile?.streak ?: 0}", style = MaterialTheme.typography.headlineLarge); Text("day streak") }
            NeoVedicCard(Modifier.weight(1f)) { Text("${profile?.xp ?: 0}", style = MaterialTheme.typography.headlineLarge); Text("XP earned") }
        }
        NeoVedicLearningCard("Continue studying", "Algebra fundamentals and exam patterns", "20 min", Icons.Outlined.AutoStories) { nav.navigate("quiz") }
        NeoVedicLearningCard("Ask AI Tutor", "Get step-by-step help in your selected subject", "Qwen ready", Icons.Outlined.Psychology) { nav.navigate("tutor") }
        NeoVedicLearningCard("Snap & Solve", "Capture image or upload PDF directly to a multimodal provider", "No OCR", Icons.Outlined.PhotoCamera) { nav.navigate("solve") }
        NeoVedicSectionTitle("Study shortcuts")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NeoVedicCard(Modifier.weight(1f), onClick = { nav.navigate("papers") }) { androidx.compose.material3.Icon(Icons.Outlined.Article, null); Text("Past papers") }
            NeoVedicCard(Modifier.weight(1f), onClick = { nav.navigate("downloads") }) { androidx.compose.material3.Icon(Icons.Outlined.Download, null); Text("Downloads · ${state.downloads}") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NeoVedicCard(Modifier.weight(1f), onClick = { nav.navigate("study") }) { androidx.compose.material3.Icon(Icons.Outlined.CalendarMonth, null); Text("Study plan") }
            NeoVedicCard(Modifier.weight(1f), onClick = { nav.navigate("notes") }) { androidx.compose.material3.Icon(Icons.Outlined.Notes, null); Text("Notes") }
        }
        NeoVedicSectionTitle("Weak topics")
        if (state.weakTopics.isEmpty()) NeoVedicEmptyState("No weak topics yet", "Take a quiz and SikAi will map what needs revision.", Icons.Outlined.Radar)
        else state.weakTopics.forEach { NeoVedicLearningCard(it.topic, "${it.subject} · ${it.misses} misses", "Practice", Icons.Outlined.TrendingDown) { nav.navigate("quiz") } }
    }
}
