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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.sikai.learn.ai.model.AiCapability
import com.sikai.learn.ui.components.NeoVedicButton
import com.sikai.learn.ui.components.NeoVedicButtonStyle
import com.sikai.learn.ui.components.NeoVedicCard
import com.sikai.learn.ui.components.NeoVedicPageHeader
import com.sikai.learn.ui.components.NeoVedicStatusPill
import com.sikai.learn.ui.components.StatusPillTone
import com.sikai.learn.ui.theme.NeoVedicTokens

@Composable
fun ProviderListScreen(
    vm: ProviderListViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onEdit: (String?) -> Unit,
) {
    val providers by vm.providers.collectAsState(initial = emptyList())
    val defaultId by vm.defaultId.collectAsState(initial = "")
    val defaultMmId by vm.defaultMmId.collectAsState(initial = "")

    Column(modifier = Modifier.fillMaxSize()) {
        NeoVedicPageHeader(
            title = "AI Providers",
            subtitle = "Configure built-in and custom providers. Keys stay on device.",
            eyebrow = "Settings",
            onBack = onBack,
            actions = {
                IconButton(onClick = { onEdit(null) }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.secondary)
                }
            },
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(NeoVedicTokens.SpaceLg),
            verticalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm),
        ) {
            items(providers) { p ->
                val isDefault = p.id == defaultId
                val isMmDefault = p.id == defaultMmId
                val masked = vm.maskedKey(p.id)
                NeoVedicCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onEdit(p.id) },
                    showCornerMarkers = isDefault || isMmDefault,
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.displayName, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${p.type.name} • ${p.textModel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Switch(checked = p.enabled, onCheckedChange = { vm.setEnabled(p, it) })
                        }
                        Spacer(Modifier.height(NeoVedicTokens.SpaceXs))
                        Row(horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm)) {
                            if (p.isBuiltIn) NeoVedicStatusPill("Built-in", tone = StatusPillTone.Gold)
                            if (!p.needsApiKey) NeoVedicStatusPill("No key", tone = StatusPillTone.Success)
                            if (AiCapability.VISION in p.capabilities) NeoVedicStatusPill("Vision", tone = StatusPillTone.Info)
                            if (AiCapability.STREAMING in p.capabilities) NeoVedicStatusPill("Stream", tone = StatusPillTone.Neutral)
                            if (isDefault) NeoVedicStatusPill("DEFAULT", tone = StatusPillTone.Gold)
                            if (isMmDefault) NeoVedicStatusPill("VISION DEFAULT", tone = StatusPillTone.Gold)
                        }
                        if (p.needsApiKey) {
                            Spacer(Modifier.height(NeoVedicTokens.SpaceXs))
                            Text(
                                if (masked.isBlank()) "No key set" else "Key: $masked",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.height(NeoVedicTokens.SpaceXs))
                        Row(horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm)) {
                            NeoVedicButton(
                                text = if (isDefault) "Default ✓" else "Set default",
                                style = if (isDefault) NeoVedicButtonStyle.Gold else NeoVedicButtonStyle.Outline,
                                onClick = { vm.setDefault(p.id) },
                            )
                            if (AiCapability.VISION in p.capabilities) {
                                NeoVedicButton(
                                    text = if (isMmDefault) "Vision ✓" else "Set vision",
                                    style = if (isMmDefault) NeoVedicButtonStyle.Gold else NeoVedicButtonStyle.Outline,
                                    onClick = { vm.setDefaultMm(p.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
