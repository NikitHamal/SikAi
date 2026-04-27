package com.sikai.learn.presentation.screens.snap

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Image
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
import com.sikai.learn.domain.model.AiModel
import com.sikai.learn.ui.components.SikAiAiAnswerCard
import com.sikai.learn.ui.components.SikAiButton
import com.sikai.learn.ui.components.SikAiButtonVariant
import com.sikai.learn.ui.components.SikAiCard
import com.sikai.learn.ui.components.SikAiEmptyState
import com.sikai.learn.ui.components.SikAiPageHeader
import com.sikai.learn.ui.components.SikAiTextField
import com.sikai.learn.ui.theme.SikAi
import java.io.File

@Composable
fun SnapAndSolveScreen(
    viewModel: SnapAndSolveViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val tokens = SikAi.tokens
    var extraPrompt by remember { mutableStateOf("") }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SikAiPageHeader(
            title = "Snap & Solve",
            subtitle = "PHOTO · MULTIMODAL · WORK SHOWN",
            trailing = {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = SikAi.colors.onSurface,
                    modifier = Modifier.size(28.dp).clickable(onClick = onBack)
                )
            }
        )

        Column(modifier = Modifier.padding(horizontal = tokens.pageHorizontal, vertical = 12.dp)) {
            ModelSelector(
                models = state.availableModels,
                selectedModel = state.selectedModel,
                onSelectModel = viewModel::selectModel,
                isRefreshing = state.refreshingModels,
                onRefresh = viewModel::refreshModels,
            )

            Spacer(Modifier.height(12.dp))

            if (state.attachmentUri == null) {
                SikAiEmptyState(
                    title = "Attach a question",
                    description = "Take a photo or pick an image — SikAi sends it to a multimodal provider so the work gets done end-to-end."
                )
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SikAiButton(
                        text = "Camera",
                        onClick = {
                            cameraPermission.launch(Manifest.permission.CAMERA)
                        },
                        leadingIcon = Icons.Outlined.CameraAlt,
                        modifier = Modifier.weight(1f)
                    )
                    SikAiButton(
                        text = "Gallery",
                        onClick = {
                            pickPhoto.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        },
                        leadingIcon = Icons.Outlined.Image,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                SikAiCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        AsyncImage(
                            model = state.attachmentUri,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = state.fileName ?: "attachment",
                            style = SikAi.type.bodyMedium,
                            color = SikAi.colors.onSurface
                        )
                        Text(
                            text = state.mimeType ?: "image/jpeg",
                            style = SikAi.type.caption,
                            color = SikAi.colors.onSurfaceMuted
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                SikAiTextField(
                    value = extraPrompt,
                    onValueChange = { extraPrompt = it },
                    label = "Extra instruction (optional)",
                    placeholder = "e.g. solve part (b), explain step 3",
                    singleLine = false,
                )
                Spacer(Modifier.height(12.dp))
                Row {
                    SikAiButton(
                        text = "Replace",
                        onClick = {
                            pickPhoto.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        },
                        variant = SikAiButtonVariant.Secondary,
                    )
                    Spacer(Modifier.width(10.dp))
                    SikAiButton(
                        text = if (state.solving) "Solving..." else "Solve with AI",
                        onClick = { viewModel.solve(extraPrompt.ifBlank { null }) },
                        leadingIcon = Icons.Outlined.AutoFixHigh,
                        enabled = !state.solving,
                    )
                }
            }

            if (state.errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                SikAiCard(emphasized = true) {
                    Text(
                        text = state.errorMessage ?: "",
                        style = SikAi.type.bodyMedium,
                        color = SikAi.colors.danger
                    )
                }
            }

            if (state.answerMarkdown != null) {
                Spacer(Modifier.height(16.dp))
                SikAiAiAnswerCard(
                    markdown = state.answerMarkdown!!,
                    providerLabel = state.providerLabel ?: "ASSISTANT",
                    modelLabel = null,
                    isStreaming = false,
                )
            }

            Spacer(Modifier.height(40.dp))
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

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
        SikAiButton(
            text = if (isRefreshing) "..." else "Refresh",
            onClick = onRefresh,
            variant = SikAiButtonVariant.Ghost,
        )
    }
}

private fun launchCamera(
    context: android.content.Context,
    onUri: (Uri) -> Unit,
) {
    val photoFile = File(context.cacheDir, "snap_${System.currentTimeMillis()}.jpg").also {
        it.parentFile?.mkdirs()
    }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
    onUri(uri)
}