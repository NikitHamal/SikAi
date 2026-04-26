package com.sikai.learn.presentation.screens.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sikai.learn.ui.components.NeoVedicButton
import com.sikai.learn.ui.components.NeoVedicButtonVariant
import com.sikai.learn.ui.components.NeoVedicCard
import com.sikai.learn.ui.components.NeoVedicEmptyState
import com.sikai.learn.ui.components.NeoVedicPageHeader
import com.sikai.learn.ui.components.NeoVedicTextField
import com.sikai.learn.ui.theme.NeoVedic
import com.sikai.learn.util.formatTimestamp

@Composable
fun NotesScreen(
    viewModel: NotesViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val tokens = NeoVedic.tokens

    Column(modifier = Modifier.fillMaxSize()) {
        NeoVedicPageHeader(
            title = "Notes",
            subtitle = "SAVE WHAT MATTERS",
            trailing = {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = NeoVedic.colors.onSurface,
                    modifier = Modifier.size(28.dp).clickable(onClick = onBack)
                )
            }
        )

        if (state.editingId != null) {
            Column(modifier = Modifier.padding(tokens.pageHorizontal)) {
                NeoVedicTextField(
                    value = state.draftTitle,
                    onValueChange = viewModel::setTitle,
                    label = "Title",
                    placeholder = "Title",
                )
                Spacer(Modifier.height(12.dp))
                NeoVedicTextField(
                    value = state.draftBody,
                    onValueChange = viewModel::setBody,
                    label = "Body",
                    placeholder = "Markdown supported",
                    singleLine = false,
                )
                Spacer(Modifier.height(12.dp))
                Row {
                    NeoVedicButton(
                        text = "Cancel",
                        onClick = viewModel::cancel,
                        variant = NeoVedicButtonVariant.Secondary,
                    )
                    Spacer(Modifier.width(10.dp))
                    NeoVedicButton(text = "Save", onClick = viewModel::save)
                }
            }
            return
        }

        Row(modifier = Modifier.fillMaxWidth().padding(tokens.pageHorizontal)) {
            NeoVedicButton(
                text = "New note",
                onClick = viewModel::startNew,
                leadingIcon = Icons.Outlined.Add,
            )
        }

        if (state.notes.isEmpty()) {
            NeoVedicEmptyState(title = "No notes yet", description = "Capture key formulas, doubts, or AI replies you want to keep.")
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = tokens.pageHorizontal, vertical = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.notes, key = { it.id }) { note ->
                    NeoVedicCard(onClick = { viewModel.edit(note.id) }, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(note.title, style = NeoVedic.type.titleMedium, color = NeoVedic.colors.onSurface)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = note.body.take(120).ifBlank { "(no body)" },
                                    style = NeoVedic.type.bodySmall,
                                    color = NeoVedic.colors.onSurfaceMuted,
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = formatTimestamp(note.updatedAtMillis),
                                    style = NeoVedic.type.caption,
                                    color = NeoVedic.colors.onSurfaceMuted,
                                )
                            }
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete",
                                tint = NeoVedic.colors.danger,
                                modifier = Modifier.size(20.dp).clickable { viewModel.delete(note.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
