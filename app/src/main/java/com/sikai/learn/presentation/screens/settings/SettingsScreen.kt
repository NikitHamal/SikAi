package com.sikai.learn.presentation.screens.settings

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
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sikai.learn.ui.components.SikAiButton
import com.sikai.learn.ui.components.SikAiButtonVariant
import com.sikai.learn.ui.components.SikAiCard
import com.sikai.learn.ui.components.SikAiEmptyState
import com.sikai.learn.ui.components.SikAiPageHeader
import com.sikai.learn.ui.components.SikAiSectionTitle
import com.sikai.learn.ui.components.SikAiStatusPill
import com.sikai.learn.ui.components.SikAiTextField
import com.sikai.learn.ui.theme.SikAi
import com.sikai.learn.ui.theme.ThemeMode
import com.sikai.learn.util.formatTimestamp

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onOpenDownloads: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val tokens = SikAi.tokens

    // Per-provider draft API key, keyed by provider id.
    val drafts = remember { mutableStateMapOf<String, String>() }

    Column(modifier = Modifier.fillMaxSize()) {
        SikAiPageHeader(
            title = "Settings"
        )
        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = tokens.pageHorizontal, vertical = 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { SikAiSectionTitle("Appearance") }
            item {
                SikAiCard {
                    Column {
                        Text(
                            text = "Theme",
                            style = SikAi.type.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemeMode.entries.forEach { mode ->
                                val selected = state.themeMode == mode
                                SikAiButton(
                                    text = mode.name,
                                    onClick = { viewModel.setThemeMode(mode) },
                                    variant = if (selected) SikAiButtonVariant.Primary else SikAiButtonVariant.Secondary,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
            item {
                SikAiSectionTitle(text = "AI providers", trailing = {
                    SikAiStatusPill(text = "${state.providers.count { it.config.enabled }}/${state.providers.size} ON")
                })
            }
            if (state.providers.isEmpty()) {
                item {
                    SikAiEmptyState(
                        title = "No providers configured",
                        description = "Built-in providers will load on first launch.",
                    )
                }
            } else {
                items(state.providers, key = { it.config.id }) { row ->
                    val cfg = row.config
                    SikAiCard {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = cfg.displayName,
                                            style = SikAi.type.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        if (cfg.isBuiltIn) {
                                            SikAiStatusPill(text = "BUILT-IN")
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = cfg.type,
                                        style = SikAi.type.caption,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    if (cfg.baseUrl != null) {
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = cfg.baseUrl,
                                            style = SikAi.type.caption,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                Switch(
                                    checked = cfg.enabled,
                                    onCheckedChange = { viewModel.setProviderEnabled(cfg.id, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    ),
                                )
                            }

                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "API KEY",
                                style = SikAi.type.sectionTitle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = row.maskedKey ?: if (cfg.apiKeyAlias == null) "Not required" else "Not set",
                                style = SikAi.type.bodyMedium,
                                color = if (row.maskedKey != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            // Built-in keyless providers (Qwen, DeepInfra) don't need an API key.
                            val needsKey = cfg.type !in setOf("qwen", "deepinfra")
                            if (needsKey) {
                                Spacer(Modifier.height(10.dp))
                                SikAiTextField(
                                    value = drafts[cfg.id].orEmpty(),
                                    onValueChange = { drafts[cfg.id] = it },
                                    label = "Paste new API key",
                                    placeholder = "sk-…",
                                    masked = true,
                                    keyboardType = KeyboardType.Password,
                                )
                                Spacer(Modifier.height(8.dp))
                                SikAiButton(
                                    text = "Save key",
                                    leadingIcon = Icons.Outlined.Save,
                                    enabled = !drafts[cfg.id].isNullOrBlank(),
                                    onClick = {
                                        val v = drafts[cfg.id]?.trim().orEmpty()
                                        if (v.isNotBlank()) {
                                            viewModel.saveApiKey(cfg.id, v)
                                            drafts[cfg.id] = ""
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
            item { SikAiSectionTitle("Data") }
            item {
                SikAiCard(onClick = onOpenDownloads, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CloudDownload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Downloaded materials",
                                style = SikAi.type.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Manage offline papers and notes",
                                style = SikAi.type.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
            item {
                SikAiSectionTitle(text = "Recent provider activity", trailing = {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                })
            }
            if (state.recentLogs.isEmpty()) {
                item {
                    SikAiEmptyState(
                        title = "No activity yet",
                        description = "Provider attempts will appear here once you start chatting or solving.",
                    )
                }
            } else {
                items(state.recentLogs, key = { it.id }) { log ->
                    SikAiCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = log.providerId,
                                        style = SikAi.type.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    SikAiStatusPill(
                                        text = if (log.success) "OK" else "FAIL",
                                        accent = if (log.success) null else MaterialTheme.colorScheme.error,
                                    )
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "${log.task} · ${log.tookMs} ms",
                                    style = SikAi.type.caption,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (!log.success && !log.message.isNullOrBlank()) {
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = log.message,
                                        style = SikAi.type.caption,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                            Text(
                                text = formatTimestamp(log.timestampMillis),
                                style = SikAi.type.caption,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}
