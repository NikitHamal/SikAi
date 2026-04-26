package com.sikai.learn.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.sikai.learn.ui.components.NeoVedicCard
import com.sikai.learn.ui.components.NeoVedicPageHeader
import com.sikai.learn.ui.components.NeoVedicStatusPill
import com.sikai.learn.ui.components.StatusPillTone
import com.sikai.learn.ui.theme.NeoVedicTokens

@Composable
fun SettingsScreen(
    vm: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onOpenProviders: () -> Unit,
) {
    val theme by vm.themeMode.collectAsState(initial = "system")
    val language by vm.language.collectAsState(initial = "en")
    val classLevel by vm.classLevel.collectAsState(initial = 10)
    val nepali by vm.nepaliMode.collectAsState(initial = false)
    val defaultProvider by vm.defaultProviderId.collectAsState(initial = "qwen-builtin")
    val defaultMm by vm.defaultMultimodalProviderId.collectAsState(initial = "qwen-builtin")

    Column(modifier = Modifier.fillMaxSize()) {
        NeoVedicPageHeader(
            title = "Settings",
            subtitle = "Account, providers and preferences",
            eyebrow = "Configure",
            onBack = onBack,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(NeoVedicTokens.SpaceLg),
            verticalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm),
        ) {
            item {
                Section("PROFILE") {
                    Column {
                        SegmentedRow(
                            label = "Class",
                            options = listOf("8", "10", "12"),
                            selected = classLevel.toString(),
                            onSelect = { vm.setClassLevel(it.toInt()) },
                        )
                        Spacer(Modifier.height(NeoVedicTokens.SpaceSm))
                        SegmentedRow(
                            label = "Language",
                            options = listOf("en", "ne"),
                            selected = language,
                            onSelect = { vm.setLanguage(it) },
                        )
                    }
                }
            }
            item {
                Section("APPEARANCE") {
                    Column {
                        SegmentedRow(
                            label = "Theme",
                            options = listOf("system", "light", "dark"),
                            selected = theme,
                            onSelect = { vm.setTheme(it) },
                        )
                        Spacer(Modifier.height(NeoVedicTokens.SpaceSm))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Devanagari accents", modifier = Modifier.weight(1f))
                            Switch(checked = nepali, onCheckedChange = { vm.setNepaliMode(it) })
                        }
                    }
                }
            }
            item {
                Section("AI PROVIDERS") {
                    NeoVedicCard(modifier = Modifier.fillMaxWidth(), onClick = onOpenProviders) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Manage providers", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "Default: $defaultProvider • Vision: $defaultMm",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
            item {
                Section("ABOUT") {
                    Column {
                        Row(horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm)) {
                            NeoVedicStatusPill("v1.0.0", tone = StatusPillTone.Gold)
                            NeoVedicStatusPill("OFFLINE-FIRST", tone = StatusPillTone.Info)
                        }
                        Spacer(Modifier.height(NeoVedicTokens.SpaceXs))
                        Text(
                            "SikAi — सिकाइ — for Class 8, SEE & NEB students. API keys stay on this device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(NeoVedicTokens.SpaceXs))
        NeoVedicCard(modifier = Modifier.fillMaxWidth()) { content() }
    }
}

@Composable
private fun SegmentedRow(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(NeoVedicTokens.SpaceXs))
        Row(horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm)) {
            options.forEach { opt ->
                val isSel = opt == selected
                com.sikai.learn.ui.components.NeoVedicButton(
                    text = opt.uppercase(),
                    onClick = { onSelect(opt) },
                    style = if (isSel) com.sikai.learn.ui.components.NeoVedicButtonStyle.Gold
                    else com.sikai.learn.ui.components.NeoVedicButtonStyle.Outline,
                )
            }
        }
    }
}
