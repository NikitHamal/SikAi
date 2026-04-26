package com.sikai.learn.ui.screens.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.sikai.learn.ui.components.NeoVedicCard
import com.sikai.learn.ui.components.NeoVedicEmptyState
import com.sikai.learn.ui.components.NeoVedicPageHeader
import com.sikai.learn.ui.components.NeoVedicStatusPill
import com.sikai.learn.ui.components.NeoVedicTextField
import com.sikai.learn.ui.components.StatusPillTone
import com.sikai.learn.ui.theme.NeoVedicTokens

@Composable
fun NotesScreen(
    vm: NotesViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val notes by vm.notes.collectAsState(initial = emptyList())
    val savedAi by vm.savedAi.collectAsState(initial = emptyList())
    var editingId by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        NeoVedicPageHeader(
            title = "Notes",
            subtitle = "Notebook & saved AI answers",
            eyebrow = "Library",
            onBack = onBack,
            actions = {
                IconButton(onClick = { vm.newNote { id -> editingId = id } }) {
                    Icon(Icons.Filled.Add, contentDescription = "New note", tint = MaterialTheme.colorScheme.secondary)
                }
            },
        )

        if (notes.isEmpty() && savedAi.isEmpty()) {
            NeoVedicEmptyState(
                title = "No notes yet",
                description = "Tap + to write a note, or save AI tutor answers.",
                icon = Icons.Filled.AutoStories,
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(NeoVedicTokens.SpaceLg),
            verticalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm),
        ) {
            if (notes.isNotEmpty()) {
                item {
                    Text("YOUR NOTES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                }
                items(notes) { note ->
                    val isEditing = editingId == note.id
                    NeoVedicCard(modifier = Modifier.fillMaxWidth(), showCornerMarkers = isEditing) {
                        Column {
                            if (isEditing) {
                                var title by remember(note.id) { mutableStateOf(note.title) }
                                var body by remember(note.id) { mutableStateOf(note.body) }
                                NeoVedicTextField(value = title, onValueChange = { title = it }, label = "Title")
                                Spacer(Modifier.height(NeoVedicTokens.SpaceXs))
                                NeoVedicTextField(
                                    value = body,
                                    onValueChange = { body = it },
                                    label = "Body",
                                    singleLine = false,
                                    minLines = 4,
                                )
                                Spacer(Modifier.height(NeoVedicTokens.SpaceXs))
                                Row(horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm)) {
                                    com.sikai.learn.ui.components.NeoVedicButton(
                                        "Save",
                                        onClick = {
                                            vm.update(note, title, body)
                                            editingId = null
                                        },
                                    )
                                    com.sikai.learn.ui.components.NeoVedicButton(
                                        "Delete",
                                        style = com.sikai.learn.ui.components.NeoVedicButtonStyle.Outline,
                                        onClick = {
                                            vm.delete(note.id)
                                            editingId = null
                                        },
                                        leadingIcon = { Icon(Icons.Filled.DeleteOutline, null) },
                                    )
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(note.title.ifBlank { "Untitled" }, style = MaterialTheme.typography.titleMedium)
                                        if (note.body.isNotBlank()) {
                                            Text(
                                                note.body.take(140),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                    com.sikai.learn.ui.components.NeoVedicButton(
                                        "Edit",
                                        style = com.sikai.learn.ui.components.NeoVedicButtonStyle.Text,
                                        onClick = { editingId = note.id },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (savedAi.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(NeoVedicTokens.SpaceMd))
                    Text(
                        "SAVED AI ANSWERS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                items(savedAi) { entry ->
                    NeoVedicCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm)) {
                                NeoVedicStatusPill(entry.providerId, tone = StatusPillTone.Info)
                                NeoVedicStatusPill(entry.mode, tone = StatusPillTone.Gold)
                            }
                            Spacer(Modifier.height(NeoVedicTokens.SpaceXs))
                            Text(entry.question, style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(NeoVedicTokens.SpaceXs))
                            Text(entry.answer, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
