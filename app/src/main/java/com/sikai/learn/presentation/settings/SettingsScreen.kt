package com.sikai.learn.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.sikai.learn.core.design.*
import com.sikai.learn.domain.ai.AiRequestFormat
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(nav: NavHostController, vm: SettingsViewModel = hiltViewModel()) {
    val providers by vm.providers.collectAsState()
    val profile by vm.profile.collectAsState()
    val prefs by vm.preferences.collectAsState()
    val status by vm.status
    var classLevel by remember(profile) { mutableStateOf((profile?.classLevel ?: 10).toString()) }
    var subjects by remember(profile) { mutableStateOf(profile?.subjectsCsv ?: "Mathematics,Science,English") }
    ScreenScaffold("Settings", "Theme, class, secure local API keys, and provider fallback") {
        status?.let { NeoVedicStatusPill(it) }
        NeoVedicSectionTitle("Appearance")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("system", "light", "dark").forEach { FilterChip(selected = prefs?.theme == it, onClick = { vm.saveTheme(it) }, label = { Text(it.replaceFirstChar(Char::titlecase)) }) } }
        NeoVedicSectionTitle("Class and subjects")
        NeoVedicCard(Modifier.fillMaxWidth()) {
            NeoVedicTextField(classLevel, { classLevel = it }, "Class level", Modifier.fillMaxWidth())
            NeoVedicTextField(subjects, { subjects = it }, "Subjects CSV", Modifier.fillMaxWidth())
            NeoVedicButton("Save learning profile") { vm.saveProfile(classLevel.toIntOrNull() ?: 10, subjects) }
        }
        NeoVedicSectionTitle("Provider priority")
        providers.forEach { provider ->
            NeoVedicCard(Modifier.fillMaxWidth(), emphasized = prefs?.defaultProviderId == provider.id) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(provider.name, style = MaterialTheme.typography.titleMedium); NeoVedicStatusPill(vm.masked(provider.apiKeyAlias)) }
                Text(provider.baseUrl, style = MaterialTheme.typography.bodySmall)
                Text("Text: ${provider.textModel} · Vision: ${provider.multimodalModel}")
                Text("Capabilities: ${provider.capabilities.joinToString { it.name }}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NeoVedicButton("Default", secondary = true) { vm.setDefaultProvider(provider.id) }
                    NeoVedicButton("Test", secondary = true) { vm.test(provider) }
                }
                var key by remember(provider.id) { mutableStateOf("") }
                NeoVedicTextField(key, { key = it }, "Local API key for ${provider.name}", Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
                NeoVedicButton("Save key locally", secondary = true) { vm.saveKey(provider.apiKeyAlias ?: provider.id, key) }
            }
        }
        AddCustomProviderCard(vm)
        NeoVedicCard(Modifier.fillMaxWidth()) {
            Text("Export/import can safely include non-secret settings only. User API keys stay in Android Keystore-backed encrypted storage and are never sent to SikAi backend.")
            NeoVedicButton("Clear local API keys", secondary = true) { vm.clearKeys() }
        }
        NeoVedicEmptyState("About SikAi", "SikAi means learning in Nepali. Built for student-first, offline-capable board exam preparation.")
    }
}

@Composable
private fun AddCustomProviderCard(vm: SettingsViewModel) {
    var name by remember { mutableStateOf("") }
    var baseUrl by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var textModel by remember { mutableStateOf("") }
    var visionModel by remember { mutableStateOf("") }
    var format by remember { mutableStateOf(AiRequestFormat.OPENAI_COMPATIBLE) }
    var upload by remember { mutableStateOf(false) }
    NeoVedicCard(Modifier.fillMaxWidth(), emphasized = true) {
        Text("Add custom provider", style = MaterialTheme.typography.titleLarge)
        NeoVedicTextField(name, { name = it }, "Provider name", Modifier.fillMaxWidth())
        NeoVedicTextField(baseUrl, { baseUrl = it }, "Base URL", Modifier.fillMaxWidth())
        NeoVedicTextField(textModel, { textModel = it }, "Default text model", Modifier.fillMaxWidth())
        NeoVedicTextField(visionModel, { visionModel = it }, "Default multimodal model", Modifier.fillMaxWidth())
        NeoVedicTextField(apiKey, { apiKey = it }, "Local API key", Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { AiRequestFormat.entries.forEach { FilterChip(format == it, { format = it }, label = { Text(it.name.removeSuffix("_COMPATIBLE")) }) } }
        Row { Checkbox(upload, { upload = it }); Text("Supports file upload/PDF") }
        NeoVedicButton("Add provider", Modifier.fillMaxWidth(), enabled = name.isNotBlank() && baseUrl.isNotBlank() && textModel.isNotBlank()) { vm.addCustom(name, baseUrl, apiKey, textModel, visionModel.ifBlank { textModel }, format, upload) }
    }
}
