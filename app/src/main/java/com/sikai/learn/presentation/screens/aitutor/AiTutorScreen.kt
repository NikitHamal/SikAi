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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.sikai.learn.domain.model.AiCapability
import com.sikai.learn.domain.model.AiMode
import com.sikai.learn.domain.model.AiModel
import com.sikai.learn.ui.components.SikAiAiAnswerCard
import com.sikai.learn.ui.components.SikAiButton
import com.sikai.learn.ui.components.SikAiButtonVariant
import com.sikai.learn.ui.components.SikAiCard
import com.sikai.learn.ui.components.SikAiEmptyState
import com.sikai.learn.ui.components.SikAiHairline
import com.sikai.learn.ui.components.SikAiPageHeader
import com.sikai.learn.ui.components.SikAiStatusPill
import com.sikai.learn.ui.components.SikAiTextField
import com.sikai.learn.ui.theme.SikAi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiTutorScreen(
    viewModel: AiTutorViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val tokens = SikAi.tokens
    var draft by remember { mutableStateOf("") }

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

        ModelSelector(
            models = state.availableModels,
            selectedModel = state.selectedModel,
            onSelectModel = viewModel::selectModel,
            isRefreshing = state.refreshingModels,
            onRefresh = viewModel::refreshModels,
        )

        ModeRow(selected = state.mode, onSelect = viewModel::setMode)

        LazyColumn(
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
                        modelLabel = msg.modelId,
                        isStreaming = false,
                    )
                }
            }
            if (state.sending) {
                item {
                    SikAiCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SikAiStatusPill(text = "Thinking...")
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
                placeholder = "Ask a question...",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelSelector(
    models: List<AiModel>,
    selectedModel: AiModel?,
    onSelectModel: (AiModel) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
) {
    val tokens = SikAi.tokens
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = selectedModel?.displayName ?: "Default"

    Column(modifier = Modifier.padding(horizontal = tokens.pageHorizontal)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.weight(1f)
            ) {
                SikAiTextField(
                    value = selectedLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = "Model",
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    if (models.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Loading models...") },
                            onClick = {},
                            enabled = false,
                        )
                    }
                    models.forEach { model ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(model.displayName, style = SikAi.type.bodyMedium)
                                    val caps = model.capabilities.mapNotNull {
                                        when (it) {
                                            AiCapability.VISION -> "Vision"
                                            AiCapability.THINKING -> "Thinking"
                                            AiCapability.PDF -> "PDF"
                                            AiCapability.SEARCH -> "Search"
                                            else -> null
                                        }
                                    }
                                    if (caps.isNotEmpty()) {
                                        Text(
                                            caps.joinToString(" · "),
                                            style = SikAi.type.caption,
                                            color = SikAi.colors.onSurfaceMuted,
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onSelectModel(model)
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            SikAiButton(
                text = if (isRefreshing) "..." else "Refresh",
                onClick = onRefresh,
                variant = SikAiButtonVariant.Ghost,
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