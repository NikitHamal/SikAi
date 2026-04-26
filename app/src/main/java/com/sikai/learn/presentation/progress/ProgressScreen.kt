package com.sikai.learn.presentation.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.sikai.learn.core.design.*
import com.sikai.learn.data.local.ContentDao
import com.sikai.learn.data.local.LearningDao
import com.sikai.learn.data.local.UserDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ProgressUiState(val xp: Int = 0, val streak: Int = 0, val attempts: Int = 0, val downloads: Int = 0, val weak: List<Pair<String, Int>> = emptyList())

@HiltViewModel
class ProgressViewModel @Inject constructor(userDao: UserDao, learningDao: LearningDao, contentDao: ContentDao) : ViewModel() {
    val state = combine(userDao.profile(), learningDao.recentAttempts(), contentDao.downloads(), learningDao.weakTopics()) { profile, attempts, downloads, weak ->
        ProgressUiState(profile?.xp ?: attempts.sumOf { it.correct * 10 }, profile?.streak ?: 0, attempts.size, downloads.size, weak.map { it.topic to it.misses })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgressUiState())
}

@Composable
fun ProgressScreen(nav: NavHostController, vm: ProgressViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    ScreenScaffold("Progress", "Streaks, XP, weak topics, and downloaded content") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NeoVedicCard(Modifier.weight(1f), emphasized = true) { Text("${state.xp}"); Text("XP") }
            NeoVedicCard(Modifier.weight(1f)) { Text("${state.streak}"); Text("Streak") }
            NeoVedicCard(Modifier.weight(1f)) { Text("${state.downloads}"); Text("Offline") }
        }
        NeoVedicCard(Modifier.fillMaxWidth(), emphasized = true) {
            Text("Weakness radar")
            if (state.weak.isEmpty()) NeoVedicEmptyState("Clean slate", "Quiz attempts will reveal topics to revise.", Icons.Outlined.Insights)
            else WeakChart(state.weak)
        }
        NeoVedicLearningCard("Recent quizzes", "${state.attempts} attempts saved locally", "Room", Icons.Outlined.Insights) { nav.navigate("quiz") }
    }
}

@Composable
private fun WeakChart(items: List<Pair<String, Int>>) {
    val p = LocalNeoVedicPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { (topic, misses) ->
            Text(topic)
            Canvas(Modifier.fillMaxWidth().height(10.dp)) {
                drawLine(p.border, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 5.dp.toPx(), StrokeCap.Square)
                drawRect(p.accent, size = Size(size.width * (misses.coerceAtMost(5) / 5f), size.height))
            }
        }
    }
}
