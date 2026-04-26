package com.sikai.learn.presentation.studyplan

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.sikai.learn.core.design.*

@Composable
fun StudyPlanScreen(nav: NavHostController, vm: StudyPlanViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    var classLevel by remember { mutableStateOf("10") }
    var date by remember { mutableStateOf("2026-06-15") }
    var minutes by remember { mutableStateOf("90") }
    ScreenScaffold("Study Plan", "Adaptive daily tasks based on exams and quiz progress") {
        NeoVedicCard(Modifier.fillMaxWidth(), emphasized = true) {
            NeoVedicTextField(classLevel, { classLevel = it }, "Class", Modifier.fillMaxWidth())
            NeoVedicTextField(date, { date = it }, "Exam date", Modifier.fillMaxWidth())
            NeoVedicTextField(minutes, { minutes = it }, "Daily minutes", Modifier.fillMaxWidth())
            NeoVedicButton("Generate local plan", Modifier.fillMaxWidth()) { vm.generate(classLevel.toIntOrNull() ?: 10, date, minutes.toIntOrNull() ?: 90) }
        }
        state.plan?.let { NeoVedicStatusPill("${it.title} · ${it.dailyMinutes} min/day") }
        state.tasks.forEach { task ->
            NeoVedicCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(task.done, { vm.setDone(task.id, it) }); Column { Text(task.title); Text("${task.subject} · ${task.dueDate}") } }
            }
        }
    }
}
