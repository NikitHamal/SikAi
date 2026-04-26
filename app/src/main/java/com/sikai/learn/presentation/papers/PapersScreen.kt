package com.sikai.learn.presentation.papers

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.sikai.learn.core.design.*
import com.sikai.learn.presentation.downloads.DownloadsViewModel
import androidx.compose.ui.unit.dp

@Composable
fun PapersScreen(nav: NavHostController, vm: DownloadsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    var query by remember { mutableStateOf("") }
    var classFilter by remember { mutableIntStateOf(10) }
    val papers = state.items.filter { it.type == "past_paper" && it.classLevel == classFilter && (query.isBlank() || it.title.contains(query, true) || it.subject.contains(query, true)) }
    ScreenScaffold("Past Papers", "Download once, revise offline") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf(8, 10, 12).forEach { FilterChip(classFilter == it, { classFilter = it }, label = { Text("Class $it") }) } }
        NeoVedicTextField(query, { query = it }, "Search subject, year, title", Modifier.fillMaxWidth())
        if (papers.isEmpty()) NeoVedicEmptyState("No paper found", "Try another class or refresh downloads manifest.", Icons.Outlined.Article) { NeoVedicButton("Open downloads") { nav.navigate("downloads") } }
        papers.forEach { item ->
            val downloaded = state.downloads.containsKey(item.id)
            NeoVedicDownloadCard(item.title, "${item.subject} · ${item.year ?: "Sample"}", if (downloaded) "Offline" else "Download") { if (!downloaded) vm.download(item) }
        }
    }
}
