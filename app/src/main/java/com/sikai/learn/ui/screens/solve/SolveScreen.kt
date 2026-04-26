package com.sikai.learn.ui.screens.solve

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.sikai.learn.ui.components.NeoVedicButton
import com.sikai.learn.ui.components.NeoVedicButtonStyle
import com.sikai.learn.ui.components.NeoVedicCard
import com.sikai.learn.ui.components.NeoVedicEmptyState
import com.sikai.learn.ui.components.NeoVedicPageHeader
import com.sikai.learn.ui.components.NeoVedicStatusPill
import com.sikai.learn.ui.components.StatusPillTone
import com.sikai.learn.ui.theme.NeoVedicTokens
import java.io.File
import java.util.concurrent.Executors
import kotlinx.coroutines.launch

@Composable
fun SolveScreen(
    vm: SolveViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }
    LaunchedEffect(Unit) { if (!hasCameraPermission) launcher.launch(Manifest.permission.CAMERA) }

    Column(modifier = Modifier.fillMaxSize()) {
        NeoVedicPageHeader(
            title = "Snap & Solve",
            subtitle = "Take a photo of any problem — SikAi will solve it step by step.",
            eyebrow = "Multimodal",
            onBack = onBack,
        )
        if (!hasCameraPermission) {
            NeoVedicEmptyState(
                title = "Camera permission required",
                description = "Allow camera access to capture problems for SikAi to solve.",
                action = {
                    NeoVedicButton(
                        "Allow camera",
                        onClick = { launcher.launch(Manifest.permission.CAMERA) },
                    )
                },
            )
            return@Column
        }

        if (state.capturedImagePath == null) {
            CameraStage(
                modifier = Modifier.weight(1f).padding(NeoVedicTokens.SpaceLg),
                onCaptured = vm::setImagePath,
            )
        } else {
            ResultStage(
                state = state,
                onRetake = vm::reset,
                onSolve = vm::solve,
            )
        }
    }
}

@Composable
private fun CameraStage(modifier: Modifier = Modifier, onCaptured: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val imageCapture = remember { ImageCapture.Builder().setTargetRotation(android.view.Surface.ROTATION_0).build() }
    val previewView = remember { PreviewView(context) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val provider = ProcessCameraProvider.getInstance(context).get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        provider.unbindAll()
        provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceLg)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .border(
                    androidx.compose.foundation.BorderStroke(NeoVedicTokens.StrokeHairline, MaterialTheme.colorScheme.outline),
                    NeoVedicTokens.ShapeSharp,
                )
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        }

        Row(horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm)) {
            NeoVedicStatusPill("Hold steady", tone = StatusPillTone.Info)
            NeoVedicStatusPill("Good light helps", tone = StatusPillTone.Gold)
        }

        NeoVedicButton(
            "Capture",
            onClick = {
                val outFile = File(context.cacheDir, "solve_${System.currentTimeMillis()}.jpg")
                val output = ImageCapture.OutputFileOptions.Builder(outFile).build()
                imageCapture.takePicture(output, executor, object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(result: ImageCapture.OutputFileResults) {
                        scope.launch { onCaptured(outFile.absolutePath) }
                    }


                    override fun onError(exception: ImageCaptureException) {
                        // surface? for now ignore
                    }
                })
            },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Filled.CameraAlt, contentDescription = null) },
        )
    }
}

@Composable
private fun ResultStage(
    state: SolveState,
    onRetake: () -> Unit,
    onSolve: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(NeoVedicTokens.SpaceLg),
        verticalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceLg),
    ) {
        item {
            NeoVedicCard(modifier = Modifier.fillMaxWidth(), showCornerMarkers = true) {
                Column {
                    Text("Captured problem", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(NeoVedicTokens.SpaceSm))
                    Text(
                        state.capturedImagePath ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(NeoVedicTokens.SpaceSm))
                    Row(horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm)) {
                        NeoVedicButton("Retake", style = NeoVedicButtonStyle.Outline, onClick = onRetake, leadingIcon = { Icon(Icons.Filled.Refresh, null) })
                        NeoVedicButton(
                            if (state.answer == null) "Solve" else "Re-solve",
                            onClick = onSolve,
                            loading = state.pending,
                        )
                    }
                }
            }
        }

        if (state.pending) {
            item { Text("Reading the question and solving…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }

        state.errorMessage?.let { msg ->
            item {
                NeoVedicCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("Couldn't solve", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                        Text(msg, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        state.answer?.let { ans ->
            item {
                NeoVedicCard(modifier = Modifier.fillMaxWidth(), showCornerMarkers = true) {
                    Column {
                        Text("Solution", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(NeoVedicTokens.SpaceSm))
                        Text(ans, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

