package com.sikai.learn.presentation.screens.snap

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
import androidx.compose.material.icons.outlined.Image
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sikai.learn.ui.components.NeoVedicAiAnswerCard
import com.sikai.learn.ui.components.NeoVedicButton
import com.sikai.learn.ui.components.NeoVedicButtonVariant
import com.sikai.learn.ui.components.NeoVedicCard
import com.sikai.learn.ui.components.NeoVedicEmptyState
import com.sikai.learn.ui.components.NeoVedicPageHeader
import com.sikai.learn.ui.components.NeoVedicTextField
import com.sikai.learn.ui.theme.NeoVedic

@Composable
fun SnapAndSolveScreen(
    viewModel: SnapAndSolveViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val tokens = NeoVedic.tokens
    var extraPrompt by remember { mutableStateOf("") }

    val pickPhoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.setAttachment(uri, null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        NeoVedicPageHeader(
            title = "Snap & Solve",
            subtitle = "PHOTO · MULTIMODAL · WORK SHOWN",
            trailing = {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = NeoVedic.colors.onSurface,
                    modifier = Modifier.size(28.dp).clickable(onClick = onBack)
                )
            }
        )

        Column(modifier = Modifier.padding(horizontal = tokens.pageHorizontal, vertical = 12.dp)) {
            if (state.attachmentUri == null) {
                NeoVedicEmptyState(
                    title = "Attach a question",
                    description = "Pick an image or PDF — SikAi sends it to a multimodal provider so the work gets done end-to-end."
                )
                Spacer(Modifier.height(16.dp))
                NeoVedicButton(
                    text = "Pick from gallery",
                    onClick = {
                        pickPhoto.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                        )
                    },
                    leadingIcon = Icons.Outlined.Image,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                NeoVedicCard(modifier = Modifier.fillMaxWidth(), showCornerMarkers = true) {
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
                            style = NeoVedic.type.bodyMedium,
                            color = NeoVedic.colors.onSurface
                        )
                        Text(
                            text = state.mimeType ?: "image/jpeg",
                            style = NeoVedic.type.caption,
                            color = NeoVedic.colors.onSurfaceMuted
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                NeoVedicTextField(
                    value = extraPrompt,
                    onValueChange = { extraPrompt = it },
                    label = "Extra instruction (optional)",
                    placeholder = "e.g. solve part (b), explain step 3",
                    singleLine = false,
                )
                Spacer(Modifier.height(12.dp))
                Row {
                    NeoVedicButton(
                        text = "Replace",
                        onClick = {
                            pickPhoto.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        },
                        variant = NeoVedicButtonVariant.Secondary,
                    )
                    Spacer(Modifier.width(10.dp))
                    NeoVedicButton(
                        text = if (state.solving) "Solving…" else "Solve with AI",
                        onClick = { viewModel.solve(extraPrompt.ifBlank { null }) },
                        leadingIcon = Icons.Outlined.AutoFixHigh,
                        enabled = !state.solving,
                    )
                }
            }

            if (state.errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                NeoVedicCard(emphasized = true) {
                    Text(
                        text = state.errorMessage ?: "",
                        style = NeoVedic.type.bodyMedium,
                        color = NeoVedic.colors.danger
                    )
                }
            }

            if (state.answerMarkdown != null) {
                Spacer(Modifier.height(16.dp))
                NeoVedicAiAnswerCard(
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
