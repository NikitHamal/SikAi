package com.sikai.learn.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.sikai.learn.ai.model.AiCapability
import com.sikai.learn.ai.model.AiProviderType
import com.sikai.learn.ai.model.AiRequestFormat
import com.sikai.learn.ui.components.NeoVedicButton
import com.sikai.learn.ui.components.NeoVedicButtonStyle
import com.sikai.learn.ui.components.NeoVedicCard
import com.sikai.learn.ui.components.NeoVedicPageHeader
import com.sikai.learn.ui.components.NeoVedicStatusPill
import com.sikai.learn.ui.components.NeoVedicTextField
import com.sikai.learn.ui.components.StatusPillTone
import com.sikai.learn.ui.theme.NeoVedicTokens

@Composable
fun ProviderEditScreen(
    providerId: String?,
    vm: ProviderEditViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    LaunchedEffect(providerId) { vm.load(providerId) }
    val state by vm.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        NeoVedicPageHeader(
            title = if (state.isNew) "Add provider" else state.displayName.ifBlank { "Edit provider" },
            subtitle = if (state.isBuiltIn) "Built-in provider — model & key only" else "Custom OpenAI-compatible provider",
            eyebrow = if (state.isNew) "Add" else "Edit",
            onBack = onBack,
        )

        if (state.loading) {
            Text("Loading…", modifier = Modifier.padding(NeoVedicTokens.SpaceLg))
            return@Column
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(NeoVedicTokens.SpaceLg).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm),
        ) {
            NeoVedicTextField(
                value = state.displayName,
                onValueChange = { v -> vm.update { it.copy(displayName = v) } },
                label = "Display name",
                enabled = !state.isBuiltIn,
                modifier = Modifier.fillMaxWidth(),
            )
            NeoVedicTextField(
                value = state.baseUrl,
                onValueChange = { v -> vm.update { it.copy(baseUrl = v) } },
                label = "Base URL",
                enabled = !state.isBuiltIn,
                modifier = Modifier.fillMaxWidth(),
            )
            NeoVedicTextField(
                value = state.textModel,
                onValueChange = { v -> vm.update { it.copy(textModel = v) } },
                label = "Text model",
                modifier = Modifier.fillMaxWidth(),
            )
            NeoVedicTextField(
                value = state.multimodalModel,
                onValueChange = { v -> vm.update { it.copy(multimodalModel = v) } },
                label = "Multimodal model (optional)",
                modifier = Modifier.fillMaxWidth(),
            )

            NeoVedicCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("CAPABILITIES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.height(NeoVedicTokens.SpaceXs))
                    Row(horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm)) {
                        AiCapability.values().forEach { cap ->
                            val on = cap in state.capabilities
                            NeoVedicButton(
                                text = cap.name,
                                style = if (on) NeoVedicButtonStyle.Gold else NeoVedicButtonStyle.Outline,
                                onClick = { vm.toggleCapability(cap) },
                            )
                        }
                    }
                }
            }

            if (!state.isBuiltIn) {
                NeoVedicCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("FORMAT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.height(NeoVedicTokens.SpaceXs))
                        Row(horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm)) {
                            listOf(
                                AiRequestFormat.OPENAI_COMPATIBLE,
                                AiRequestFormat.GEMINI_NATIVE,
                                AiRequestFormat.QWEN_NATIVE,
                            ).forEach { f ->
                                NeoVedicButton(
                                    text = f.name,
                                    style = if (state.requestFormat == f) NeoVedicButtonStyle.Gold else NeoVedicButtonStyle.Outline,
                                    onClick = { vm.update { it.copy(requestFormat = f) } },
                                )
                            }
                        }
                        Spacer(Modifier.height(NeoVedicTokens.SpaceSm))
                        Text("TYPE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.height(NeoVedicTokens.SpaceXs))
                        Row(horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm)) {
                            listOf(AiProviderType.OPENAI_COMPATIBLE, AiProviderType.CUSTOM).forEach { t ->
                                NeoVedicButton(
                                    text = t.name,
                                    style = if (state.type == t) NeoVedicButtonStyle.Gold else NeoVedicButtonStyle.Outline,
                                    onClick = { vm.update { it.copy(type = t) } },
                                )
                            }
                        }
                    }
                }
            }

            NeoVedicCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Needs API key", modifier = Modifier.weight(1f))
                        Switch(
                            checked = state.needsApiKey,
                            onCheckedChange = { v -> vm.update { it.copy(needsApiKey = v) } },
                            enabled = !state.isBuiltIn,
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Enabled", modifier = Modifier.weight(1f))
                        Switch(checked = state.enabled, onCheckedChange = { v -> vm.update { it.copy(enabled = v) } })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Supports file upload", modifier = Modifier.weight(1f))
                        Switch(
                            checked = state.supportsFileUpload,
                            onCheckedChange = { v -> vm.update { it.copy(supportsFileUpload = v) } },
                        )
                    }
                }
            }

            if (state.needsApiKey) {
                NeoVedicCard(modifier = Modifier.fillMaxWidth(), showCornerMarkers = true) {
                    Column {
                        Row(horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm)) {
                            NeoVedicStatusPill("ON-DEVICE", tone = StatusPillTone.Gold)
                            NeoVedicStatusPill("ENCRYPTED", tone = StatusPillTone.Success)
                        }
                        Spacer(Modifier.height(NeoVedicTokens.SpaceXs))
                        Text(
                            "Your API key is stored locally with Android Keystore. It is NEVER sent to SikAi servers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(NeoVedicTokens.SpaceXs))
                        if (state.apiKeyMasked.isNotBlank()) {
                            Text(
                                "Saved: ${state.apiKeyMasked}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        NeoVedicTextField(
                            value = state.apiKeyInput,
                            onValueChange = { v -> vm.update { it.copy(apiKeyInput = v) } },
                            label = "API key",
                            isPassword = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(NeoVedicTokens.SpaceXs))
                        if (state.apiKeyMasked.isNotBlank()) {
                            NeoVedicButton("Clear key", onClick = vm::clearKey, style = NeoVedicButtonStyle.Outline)
                        }
                    }
                }
            }

            Spacer(Modifier.height(NeoVedicTokens.SpaceMd))
            Row(horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm)) {
                NeoVedicButton(
                    text = "Save",
                    onClick = { vm.save(onBack) },
                    style = NeoVedicButtonStyle.Primary,
                    modifier = Modifier.weight(1f),
                )
                if (!state.isNew && !state.isBuiltIn) {
                    NeoVedicButton(
                        text = "Delete",
                        onClick = { vm.delete(onBack) },
                        style = NeoVedicButtonStyle.Outline,
                    )
                }
            }
        }
    }
}

