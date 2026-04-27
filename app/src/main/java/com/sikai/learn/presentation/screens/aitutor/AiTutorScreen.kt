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
import com.sikai.learn.ui.components.SikAiAiAnswerCard
import com.sikai.learn.ui.components.SikAiButton
import com.sikai.learn.ui.components.SikAiCard
import com.sikai.learn.ui.components.SikAiEmptyState
import com.sikai.learn.ui.components.SikAiHairline
import com.sikai.learn.ui.components.SikAiPageHeader
import com.sikai.learn.ui.components.SikAiStatusPill
import com.sikai.learn.ui.components.SikAiTextField
import com.sikai.learn.ui.theme.SikAi

@Composable
fun AiTutorScreen(
    viewModel: AiTutorViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val tokens = SikAi.tokens
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.lastIndex)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SikAiPageHeader(
            title = "AI Tutor",
            subtitle = "ASK · LEARN · UNDERSTAND",
            trailing = {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = SikAi.colors.onSurface,
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
                    SikAiEmptyState(
                        title = "Ask anything",
                        description = "Type a question about your subject. SikAi explains in plain language.",
                    )
                }
            }
            items(state.messages, key = { it.id }) { msg ->
                if (msg.fromUser) {
                    SikAiCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("YOU", style = SikAi.type.sectionTitle, color = SikAi.colors.onSurfaceMuted)
                            Spacer(Modifier.height(6.dp))
                            Text(msg.text, style = SikAi.type.bodyLarge, color = SikAi.colors.onSurface)
                        }
                    }
                } else {
                    SikAiAiAnswerCard(
                        markdown = msg.text,
                        providerLabel = msg.providerLabel ?: (if (msg.isError) "ERROR" else "ASSISTANT"),
                        modelLabel = null,
                        isStreaming = false,
                    )
                }
            }
            if (state.sending) {
                item {
                    SikAiCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SikAiStatusPill(text = "Thinking…")
                        }
                    }
                }
            }
        }

        SikAiHairline()
        Row(
            modifier = Modifier.fillMaxWidth().padding(tokens.space12),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SikAiTextField(
                value = draft,
                onValueChange = { draft = it },
                placeholder = "Ask a question…",
                singleLine = false,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            SikAiButton(
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
    val tokens = SikAi.tokens
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
            SikAiCard(
                onClick = { onSelect(mode) },
                emphasized = isSelected,
                contentPadding = 8.dp,
            ) {
                Text(label.uppercase(), style = SikAi.type.label, color = SikAi.colors.onSurface)
            }
        }
    }
}
