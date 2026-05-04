package com.sikai.learn.presentation.screens.aitutor

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sikai.learn.domain.model.AiCapability
import com.sikai.learn.domain.model.AiMode
import com.sikai.learn.domain.model.AiModel
import com.sikai.learn.ui.components.*
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
            title = "AI Tutor"
        )

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            ModelSelectorPill(
                models = state.availableModels,
                selectedModel = state.selectedModel,
                onSelectModel = viewModel::selectModel,
                isRefreshing = state.refreshingModels,
                onRefresh = viewModel::refreshModels,
            )
        }

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
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = state.attachmentFileName ?: "attachment",
                                    style = SikAi.type.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = state.attachmentMimeType ?: "",
                                    style = SikAi.type.caption,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Remove",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            Text(
                                text = "YOU", 
                                style = SikAi.type.sectionTitle, 
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = msg.text, 
                                style = SikAi.type.bodyLarge, 
                                color = MaterialTheme.colorScheme.onSurface
                            )
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
private fun ModelSelectorPill(
    models: List<AiModel>,
    selectedModel: AiModel?,
    onSelectModel: (AiModel) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = selectedModel?.displayName ?: "Academic Model-v2"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        Box(
            modifier = Modifier
                .menuAnchor()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Psychiatry,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = selectedLabel,
                    style = SikAi.type.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(if (isRefreshing) "Refreshing..." else "Refresh List", style = SikAi.type.label) },
                onClick = {
                    onRefresh()
                    expanded = false
                }
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
        AiMode.entries.forEach { mode ->
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
                Text(
                    text = label.uppercase(), 
                    style = SikAi.type.label, 
                    color = MaterialTheme.colorScheme.onSurface
                )
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