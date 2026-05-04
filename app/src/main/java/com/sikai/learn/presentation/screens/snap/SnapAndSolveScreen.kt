package com.sikai.learn.presentation.screens.snap

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sikai.learn.domain.model.AiCapability
import com.sikai.learn.domain.model.AiModel
import com.sikai.learn.ui.components.*
import com.sikai.learn.ui.theme.SikAi
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnapAndSolveScreen(
    viewModel: SnapAndSolveViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
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

    Scaffold(
        topBar = { /* Header removed as requested */ },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // Model Selector Pill
            ModelSelectorPill(
                models = state.availableModels,
                selectedModel = state.selectedModel,
                onSelectModel = viewModel::selectModel,
                isRefreshing = state.refreshingModels,
                onRefresh = viewModel::refreshModels,
            )

            Spacer(Modifier.height(24.dp))

            // Attachment Area
            val outlineColor = MaterialTheme.colorScheme.outlineVariant
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .aspectRatio(0.75f)
                    .drawBehind {
                        val stroke = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                        )
                        drawRoundRect(
                            color = outlineColor,
                            style = stroke,
                            cornerRadius = CornerRadius(24.dp.toPx())
                        )
                    }
                    .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .clickable {
                        if (state.attachmentUri == null) {
                            pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (state.attachmentUri == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.CenterFocusStrong,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(
                            text = "Attach a question",
                            style = SikAi.type.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Icon(
                        imageVector = Icons.Outlined.Functions,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(24.dp)
                            .size(32.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )

                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(24.dp)
                            .size(32.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                } else {
                    AsyncImage(
                        model = state.attachmentUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    IconButton(
                        onClick = { viewModel.clearAttachment() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(36.dp)
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Clear",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            if (state.attachmentUri == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { cameraPermission.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CameraAlt, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Camera", style = SikAi.type.label.copy(fontSize = 16.sp))
                        }
                    }

                    Button(
                        onClick = {
                            pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Image, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Gallery", style = SikAi.type.label.copy(fontSize = 16.sp))
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SikAiTextField(
                        value = extraPrompt,
                        onValueChange = { extraPrompt = it },
                        label = "Extra instruction (optional)",
                        placeholder = "e.g. solve part (b), explain step 3",
                        singleLine = false,
                    )
                    Spacer(Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SikAiButton(
                            text = "Replace",
                            onClick = { pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            variant = SikAiButtonVariant.Secondary,
                            leadingIcon = Icons.Outlined.Refresh,
                            modifier = Modifier.weight(1f)
                        )
                        
                        SikAiButton(
                            text = if (state.solving) "Solving..." else "Solve Now",
                            onClick = { viewModel.solve(extraPrompt.ifBlank { null }) },
                            variant = SikAiButtonVariant.Primary,
                            leadingIcon = Icons.Outlined.AutoFixHigh,
                            enabled = !state.solving,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (state.errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                SikAiCard(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        text = state.errorMessage ?: "",
                        style = SikAi.type.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (state.answerMarkdown != null) {
                Spacer(Modifier.height(16.dp))
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SikAiAiAnswerCard(
                        markdown = state.answerMarkdown!!,
                        providerLabel = state.providerLabel ?: "ASSISTANT",
                        modelLabel = null,
                        isStreaming = false,
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
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
        // Use Box with menuAnchor() directly for custom UI
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
                    imageVector = Icons.Outlined.AutoAwesome,
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