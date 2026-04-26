package com.sikai.learn.ui.screens.tutor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sikai.learn.ai.prompt.TutorMode
import com.sikai.learn.ui.components.NeoVedicCard
import com.sikai.learn.ui.components.NeoVedicPageHeader
import com.sikai.learn.ui.components.NeoVedicStatusPill
import com.sikai.learn.ui.components.NeoVedicTextField
import com.sikai.learn.ui.components.StatusPillTone
import com.sikai.learn.ui.theme.NeoVedicTokens

@Composable
fun TutorScreen(
    vm: TutorViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by vm.state.collectAsState()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(state.turns.size) {
        if (state.turns.isNotEmpty()) listState.animateScrollToItem(state.turns.lastIndex)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        NeoVedicPageHeader(
            title = "AI Tutor",
            subtitle = "Free, exam-aware help for Class ${state.classLevel}",
            eyebrow = "SikAi",
            onBack = onBack,
            actions = {
                IconButton(onClick = vm::saveLastAsNote) {
                    Icon(Icons.Filled.BookmarkAdd, contentDescription = "Save", tint = MaterialTheme.colorScheme.secondary)
                }
                IconButton(onClick = vm::clear) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
        )

        ModeRow(state.mode, vm::setMode)

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (state.turns.isEmpty()) {
                EmptyTutor(onPickStarter = { input = it; vm.send(it); input = "" })
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = NeoVedicTokens.SpaceLg),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm),
                ) {
                    items(state.turns) { turn -> ChatBubble(turn = turn) }
                    if (state.pending) item { ThinkingBubble() }
                    item { Spacer(Modifier.height(NeoVedicTokens.SpaceLg)) }
                }
            }
        }

        InputBar(
            value = input,
            onChange = { input = it },
            sending = state.pending,
            onSend = {
                if (input.isNotBlank()) {
                    vm.send(input.trim())
                    input = ""
                }
            },
        )
    }
}

@Composable
private fun ModeRow(mode: TutorMode, onSelect: (TutorMode) -> Unit) {
    val modes = listOf(
        TutorMode.Direct to "Direct",
        TutorMode.SimpleExplain to "Simple",
        TutorMode.Socratic to "Socratic",
        TutorMode.SolveStepByStep to "Solve step",
        TutorMode.ExamAnswer to "Exam answer",
        TutorMode.SummarizeNote to "Summarize",
    )
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll)
            .padding(horizontal = NeoVedicTokens.SpaceLg, vertical = NeoVedicTokens.SpaceSm),
        horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm),
    ) {
        modes.forEach { (m, label) ->
            val selected = mode == m
            Box(
                modifier = Modifier
                    .clip(NeoVedicTokens.ShapeSharp)
                    .border(
                        androidx.compose.foundation.BorderStroke(
                            NeoVedicTokens.StrokeHairline,
                            if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant,
                        ),
                        NeoVedicTokens.ShapeSharp,
                    )
                    .background(if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface)
                    .clickable { onSelect(m) }
                    .padding(horizontal = NeoVedicTokens.SpaceMd, vertical = NeoVedicTokens.SpaceXs),
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EmptyTutor(onPickStarter: (String) -> Unit) {
    val starters = listOf(
        "Explain Newton's third law with an example",
        "Solve: 2x² − 5x + 3 = 0",
        "Summarize the structure of a SEE essay",
        "Give 5 likely SEE Math algebra questions",
        "Help me revise photosynthesis for NEB Biology",
    )
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(NeoVedicTokens.SpaceLg),
        verticalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm)) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Text(
                    "Try one of these to start, or just type a question.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(starters) { s ->
            NeoVedicCard(modifier = Modifier.fillMaxWidth(), onClick = { onPickStarter(s) }, showCornerMarkers = true) {
                Text(s, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun ChatBubble(turn: ChatTurn) {
    val isUser = turn.role == "user"
    val bg = when {
        turn.isError -> MaterialTheme.colorScheme.error.copy(alpha = 0.10f)
        isUser -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val fg = when {
        turn.isError -> MaterialTheme.colorScheme.error
        isUser -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.92f),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(bg)
                    .border(
                        androidx.compose.foundation.BorderStroke(
                            NeoVedicTokens.StrokeHairline,
                            MaterialTheme.colorScheme.outlineVariant,
                        ),
                        RoundedCornerShape(2.dp),
                    )
                    .padding(NeoVedicTokens.SpaceMd),
            ) {
                Text(turn.text, style = MaterialTheme.typography.bodyMedium, color = fg)
            }
            if (!isUser && turn.providerId != null) {
                Spacer(Modifier.height(2.dp))
                NeoVedicStatusPill("${turn.providerId} · ${turn.modelId}", tone = StatusPillTone.Info)
            }
        }
    }
}

@Composable
private fun ThinkingBubble() {
    Row {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(NeoVedicTokens.SpaceMd),
        ) {
            Text("Thinking…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun InputBar(value: String, onChange: (String) -> Unit, sending: Boolean, onSend: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(NeoVedicTokens.SpaceMd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm),
    ) {
        Box(modifier = Modifier.weight(1f)) {
            NeoVedicTextField(
                value = value,
                onValueChange = onChange,
                placeholder = "Ask SikAi anything…",
                singleLine = false,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        IconButton(onClick = onSend, enabled = !sending && value.isNotBlank()) {
            Icon(
                Icons.Filled.Send,
                contentDescription = "Send",
                tint = if (sending) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.secondary,
            )
        }
    }
}
