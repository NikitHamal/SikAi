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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sikai.learn.ui.components.SikAiButton
import com.sikai.learn.ui.components.SikAiButtonVariant
import com.sikai.learn.ui.components.SikAiCard
import com.sikai.learn.ui.components.SikAiPageHeader
import com.sikai.learn.ui.components.SikAiSectionTitle
import com.sikai.learn.ui.theme.SikAi
import com.sikai.learn.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onOpenDownloads: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val tokens = SikAi.tokens

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
            item { SikAiSectionTitle("About") }
            item {
                SikAiCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text = "SikAi",
                            style = SikAi.type.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Version 1.0.0",
                            style = SikAi.type.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Nepali education powered by AI",
                            style = SikAi.type.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}