package com.sikai.learn.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PsychologyAlt
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sikai.learn.ui.components.HairlineDivider
import com.sikai.learn.ui.components.NeoVedicCard
import com.sikai.learn.ui.components.NeoVedicSectionTitle
import com.sikai.learn.ui.components.NeoVedicStatusPill
import com.sikai.learn.ui.components.StatusPillTone
import com.sikai.learn.ui.theme.NeoVedicTokens

@Composable
fun HomeScreen(
    vm: HomeViewModel = hiltViewModel(),
    onOpenTutor: () -> Unit,
    onOpenSolve: () -> Unit,
    onOpenPapers: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenQuiz: () -> Unit,
    onOpenPlan: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val state by vm.state.collectAsState(initial = HomeState())

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceMd),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = NeoVedicTokens.SpaceXl),
    ) {
        item { GreetingHeader(state) }
        item { StreakRow(state) }
        item {
            NeoVedicSectionTitle("Quick actions", accent = "Tap to begin")
        }
        item { QuickActions(onOpenTutor, onOpenSolve, onOpenQuiz, onOpenPapers) }
        item {
            NeoVedicSectionTitle("Today", accent = if (state.examInDays != null) "Plan ahead" else null)
        }
        item {
            TodayCard(state, onOpenPlan)
        }
        item {
            NeoVedicSectionTitle("Library")
        }
        item {
            LibraryGrid(
                onOpenPapers = onOpenPapers,
                onOpenDownloads = onOpenDownloads,
                onOpenNotes = onOpenNotes,
                onOpenProgress = onOpenProgress,
                onOpenSettings = onOpenSettings,
            )
        }
        item { Spacer(Modifier.height(NeoVedicTokens.SpaceXl)) }
    }
}

@Composable
private fun GreetingHeader(state: HomeState) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = NeoVedicTokens.SpaceLg, vertical = NeoVedicTokens.SpaceLg)) {
        Text(
            "नमस्ते,".takeIf { state.nepaliMode } ?: "Welcome back,",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
        Text(
            state.displayName,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            "Class ${state.classLevel} • ${if (state.classLevel == 10) "SEE" else if (state.classLevel == 12) "NEB" else "Lower Secondary"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(NeoVedicTokens.SpaceMd))
        Box(modifier = Modifier.height(2.dp).fillMaxWidth(0.18f).background(MaterialTheme.colorScheme.secondary))
    }
}

@Composable
private fun StreakRow(state: HomeState) {
    Row(
        modifier = Modifier.padding(horizontal = NeoVedicTokens.SpaceLg),
        horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm),
    ) {
        NeoVedicStatusPill("${state.streakDays} day streak", tone = StatusPillTone.Gold)
        NeoVedicStatusPill("${state.xp} XP", tone = StatusPillTone.Info)
        if (state.examInDays != null) {
            NeoVedicStatusPill("Exam in ${state.examInDays}d", tone = StatusPillTone.Warn)
        }
    }
}

@Composable
private fun QuickActions(
    onOpenTutor: () -> Unit,
    onOpenSolve: () -> Unit,
    onOpenQuiz: () -> Unit,
    onOpenPapers: () -> Unit,
) {
    val items = listOf(
        QuickAction("AI Tutor", "Ask questions, learn concepts", Icons.Filled.PsychologyAlt, onOpenTutor),
        QuickAction("Snap & Solve", "Camera + AI", Icons.Filled.CameraAlt, onOpenSolve),
        QuickAction("Quick quiz", "5-question MCQ drill", Icons.Filled.Quiz, onOpenQuiz),
        QuickAction("Past papers", "SEE & NEB archive", Icons.Filled.Description, onOpenPapers),
    )
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm),
        horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm),
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(horizontal = NeoVedicTokens.SpaceLg),
    ) {
        items(items) { ActionTile(it) }
    }
}

private data class QuickAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

@Composable
private fun ActionTile(action: QuickAction) {
    NeoVedicCard(
        modifier = Modifier.fillMaxWidth(),
        showCornerMarkers = true,
        onClick = action.onClick,
    ) {
        Column(verticalArrangement = Arrangement.SpaceBetween) {
            Icon(action.icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(NeoVedicTokens.SpaceSm))
            Text(action.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(action.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TodayCard(state: HomeState, onOpenPlan: () -> Unit) {
    NeoVedicCard(modifier = Modifier.fillMaxWidth().padding(horizontal = NeoVedicTokens.SpaceLg), onClick = onOpenPlan) {
        Column {
            Text("Today's plan", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(NeoVedicTokens.SpaceXs))
            HairlineDivider()
            Spacer(Modifier.height(NeoVedicTokens.SpaceSm))
            if (state.subjects.isEmpty()) {
                Text("No subjects yet — add some from Settings.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                state.subjects.take(3).forEach {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm)) {
                        Box(modifier = Modifier.height(8.dp).padding(end = NeoVedicTokens.SpaceXs).background(MaterialTheme.colorScheme.secondary))
                        Text(it, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(Modifier.height(NeoVedicTokens.SpaceSm))
            NeoVedicStatusPill("Open study plan →", tone = StatusPillTone.Gold)
        }
    }
}

@Composable
private fun LibraryGrid(
    onOpenPapers: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val items = listOf(
        QuickAction("Past Papers", "Browse SEE/NEB", Icons.Filled.Description, onOpenPapers),
        QuickAction("Downloads", "Offline library", Icons.Filled.CloudDownload, onOpenDownloads),
        QuickAction("Notes", "Your study notes", Icons.Filled.AutoStories, onOpenNotes),
        QuickAction("Progress", "Track weak topics", Icons.Filled.Insights, onOpenProgress),
        QuickAction("Study plan", "Build a roadmap", Icons.Filled.EditCalendar, {}),
        QuickAction("Settings", "Theme, AI, language", Icons.Filled.Settings, onOpenSettings),
    )
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm),
        horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm),
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
            .padding(horizontal = NeoVedicTokens.SpaceLg),
    ) {
        items(items) { ActionTile(it) }
    }
}
