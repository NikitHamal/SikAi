package com.sikai.learn.presentation.screens.aitutor

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sikai.learn.domain.model.AiMode
import com.sikai.learn.ui.components.NeoVedicAiAnswerCard
import com.sikai.learn.ui.components.NeoVedicButton
import com.sikai.learn.ui.components.NeoVedicCard
import com.sikai.learn.ui.components.NeoVedicEmptyState
import com.sikai.learn.ui.components.NeoVedicHairline
import com.sikai.learn.ui.components.NeoVedicPageHeader
import com.sikai.learn.ui.components.NeoVedicStatusPill
import com.sikai.learn.ui.components.NeoVedicTextField
import com.sikai.learn.ui.theme.NeoVedic

@Composable
fun AiTutorScreen(
    viewModel: AiTutorViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val tokens = NeoVedic.tokens
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.lastIndex)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        NeoVedicPageHeader(
            title = "AI Tutor",
            subtitle = "ASK · LEARN · UNDERSTAND",
            trailing = {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = NeoVedic.colors.onSurface,
                    modifier = Modifier.size(28.dp).clickable(onClick = onBack)
                )
            }
        )

        ModeRow(selected = state.mode, onSelect = viewModel::setMode)

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = tokens.pageHorizontal, vertical = 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (state.messages.isEmpty()) {
                item {
                    NeoVedicEmptyState(
                        title = "Ask anything",
                        description = "Type a question about your subject. SikAi explains in plain language.",
                    )
                }
            }
            items(state.messages, key = { it.id }) { msg ->
                if (msg.fromUser) {
                    NeoVedicCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("YOU", style = NeoVedic.type.sectionTitle, color = NeoVedic.colors.onSurfaceMuted)
                            Spacer(Modifier.height(6.dp))
                            Text(msg.text, style = NeoVedic.type.bodyLarge, color = NeoVedic.colors.onSurface)
                        }
                    }
                } else {
                    NeoVedicAiAnswerCard(
                        markdown = msg.text,
                        providerLabel = msg.providerLabel ?: (if (msg.isError) "ERROR" else "ASSISTANT"),
                        modelLabel = null,
                        isStreaming = false,
                    )
                }
            }
            if (state.sending) {
                item {
                    NeoVedicCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            NeoVedicStatusPill(text = "Thinking…")
                        }
                    }
                }
            }
        }

        NeoVedicHairline()
        Row(
            modifier = Modifier.fillMaxWidth().padding(tokens.space12),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NeoVedicTextField(
                value = draft,
                onValueChange = { draft = it },
                placeholder = "Ask a question…",
                singleLine = false,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            NeoVedicButton(
                text = "Send",
                onClick = {
                    val toSend = draft.trim()
                    if (toSend.isNotEmpty()) {
                        viewModel.send(toSend)
                        draft = ""
                    }
                },
                enabled = draft.isNotBlank() && !state.sending,
                leadingIcon = Icons.Outlined.Send,
            )
        }
    }
}

@Composable
private fun ModeRow(selected: AiMode, onSelect: (AiMode) -> Unit) {
    val tokens = NeoVedic.tokens
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.pageHorizontal, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AiMode.values().forEach { mode ->
            val label = when (mode) {
                AiMode.Socratic -> "Socratic"
                AiMode.DirectAnswer -> "Direct"
                AiMode.SimpleExplanation -> "Simple"
                AiMode.ExamFocused -> "Exam"
                AiMode.StepByStep -> "Steps"
            }
            val isSelected = mode == selected
            NeoVedicCard(
                onClick = { onSelect(mode) },
                emphasized = isSelected,
                contentPadding = 8.dp,
            ) {
                Text(label.uppercase(), style = NeoVedic.type.label, color = NeoVedic.colors.onSurface)
            }
        }
    }
}
