package com.sikai.learn.presentation.downloads

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.sikai.learn.core.design.*

@Composable
fun DownloadsScreen(nav: NavHostController, vm: DownloadsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val busy by vm.busy
    val message by vm.message
    ScreenScaffold("Downloads", "Manifest-driven textbooks, papers, MCQs, syllabus, and notes") {
        message?.let { NeoVedicStatusPill(it) }
        if (state.items.isEmpty()) NeoVedicEmptyState("No manifest yet", "SikAi will use backend content or bundled sample data.", Icons.Outlined.CloudDownload) { NeoVedicButton("Refresh") { vm.refresh() } }
        state.items.forEach { item ->
            val downloaded = state.downloads.containsKey(item.id)
            NeoVedicDownloadCard(item.title, "Class ${item.classLevel} · ${item.subject} · ${item.type} · ${item.sizeBytes / 1024} KB", if (busy == item.id) "Working" else if (downloaded) "Delete" else "Download") {
                if (downloaded) vm.delete(item.id) else vm.download(item)
            }
        }
    }
}
