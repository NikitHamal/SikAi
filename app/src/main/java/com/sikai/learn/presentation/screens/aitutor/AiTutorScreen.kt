package com.sikai.learn.presentation.screens.aitutor

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
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
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiTutorScreen(
    viewModel: AiTutorViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val tokens = SikAi.tokens
    var draft by remember { mutableStateOf("") }
    var cameraPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val pickPhoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.setAttachment(uri, null)
    }

    val takePhoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = cameraPhotoUri
        if (success && uri != null) viewModel.setAttachment(uri, "image/jpeg")
    }

    val cameraPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera(context) { uri ->
                cameraPhotoUri = uri
                takePhoto.launch(uri)
            }
        }
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
            if (state.messages.isEmpty() && state.attachmentUri == null) {
                item {
                    SikAiEmptyState(
                        title = "Ask anything",
                        description = "Type a question or attach an image/file. SikAi explains in plain language.",
                    )
                }
            }
            if (state.attachmentUri != null) {
                item {
                    SikAiCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (state.attachmentMimeType?.startsWith("image/") == true) {
                                AsyncImage(
                                    model = state.attachmentUri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(64.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.AttachFile,
                                    contentDescription = null,
                                    tint = SikAi.colors.onSurfaceMuted,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = state.attachmentFileName ?: "attachment",
                                    style = SikAi.type.bodyMedium,
                                    color = SikAi.colors.onSurface
                                )
                                Text(
                                    text = state.attachmentMimeType ?: "",
                                    style = SikAi.type.caption,
                                    color = SikAi.colors.onSurfaceMuted
                                )
                            }
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Remove",
                                tint = SikAi.colors.onSurfaceMuted,
                                modifier = Modifier.size(20.dp).clickable { viewModel.clearAttachment() }
                            )
                        }
                    }
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
        Column(modifier = Modifier.padding(horizontal = tokens.pageHorizontal, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SikAiButton(
                    text = "",
                    onClick = { cameraPermission.launch(Manifest.permission.CAMERA) },
                    variant = SikAiButtonVariant.Ghost,
                    leadingIcon = Icons.Outlined.CameraAlt,
                )
                SikAiButton(
                    text = "",
                    onClick = {
                        pickPhoto.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                        )
                    },
                    variant = SikAiButtonVariant.Ghost,
                    leadingIcon = Icons.Outlined.AttachFile,
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        if (toSend.isNotEmpty() || state.attachmentUri != null) {
                            viewModel.send(toSend.ifBlank { "Describe this" })
                            draft = ""
                        }
                    },
                    enabled = (draft.isNotBlank() || state.attachmentUri != null) && !state.sending,
                    leadingIcon = Icons.Outlined.Send,
                )
            }
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
                                            AiCapability.FILE_UPLOAD -> "Upload"
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

private fun launchCamera(
    context: android.content.Context,
    onUri: (Uri) -> Unit,
) {
    val photoFile = File(context.cacheDir, "tutor_${System.currentTimeMillis()}.jpg").also {
        it.parentFile?.mkdirs()
    }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
    onUri(uri)
}