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
import com.sikai.learn.ui.components.SikAiAiAnswerCard
import com.sikai.learn.ui.components.SikAiButton
import com.sikai.learn.ui.components.SikAiCard
import com.sikai.learn.ui.components.SikAiTextField
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
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sikai",
                        style = SikAi.type.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3F3F91) // Deep Indigo
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color(0xFFF8FAFF) // Very light blue/white background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                            color = Color(0xFFCCD2E3),
                            style = stroke,
                            cornerRadius = CornerRadius(24.dp.toPx())
                        )
                    }
                    .background(Color.White, shape = RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .clickable {
                        pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                contentAlignment = Alignment.Center
            ) {
                if (state.attachmentUri == null) {
                    // Empty State Content
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            color = Color(0xFFE8EAF6)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.CenterFocusStrong,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = Color(0xFF1A1A5E)
                                )
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(
                            text = "Attach a question",
                            style = SikAi.type.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF1A1C1E)
                        )
                    }

                    // Sigma Icon Top-Left
                    Icon(
                        imageVector = Icons.Outlined.Functions,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(24.dp)
                            .size(32.dp),
                        tint = Color(0xFFE0E0E0)
                    )

                    // Document Icon Bottom-Right
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(24.dp)
                            .size(32.dp),
                        tint = Color(0xFFE0E0E0)
                    )
                } else {
                    // Selected Image Content
                    AsyncImage(
                        model = state.attachmentUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Camera and Gallery Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Camera Button
                Button(
                    onClick = { cameraPermission.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF000051),
                        contentColor = Color.White
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CameraAlt, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Camera", style = SikAi.type.label.copy(fontSize = 16.sp))
                    }
                }

                // Gallery Button
                Button(
                    onClick = {
                        pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF94A3FF),
                        contentColor = Color(0xFF000051)
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Image, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Gallery", style = SikAi.type.label.copy(fontSize = 16.sp))
                    }
                }
            }

            // Results and Prompt (if image attached)
            if (state.attachmentUri != null) {
                Column(modifier = Modifier.padding(24.dp)) {
                    SikAiTextField(
                        value = extraPrompt,
                        onValueChange = { extraPrompt = it },
                        label = "Extra instruction (optional)",
                        placeholder = "e.g. solve part (b), explain step 3",
                        singleLine = false,
                    )
                    Spacer(Modifier.height(16.dp))
                    SikAiButton(
                        text = if (state.solving) "Solving..." else "Solve with AI",
                        onClick = { viewModel.solve(extraPrompt.ifBlank { null }) },
                        leadingIcon = Icons.Outlined.AutoFixHigh,
                        enabled = !state.solving,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (state.errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                SikAiCard(emphasized = true, modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        text = state.errorMessage ?: "",
                        style = SikAi.type.bodyMedium,
                        color = SikAi.colors.danger
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
        Surface(
            modifier = Modifier
                .menuAnchor()
                .clip(RoundedCornerShape(24.dp))
                .clickable { expanded = !expanded },
            color = Color(0xFFF1F3F4),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Spa, // Sprout/Leaf icon
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Black
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = selectedLabel,
                    style = SikAi.type.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    color = Color.Black
                )
                Spacer(Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Outlined.ExpandMore, // Smaller chevron
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.Black
                )
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
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
                                    color = Color.Gray,
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