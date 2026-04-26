package com.sikai.learn.presentation.solve

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Text
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.sikai.learn.core.design.*
import com.sikai.learn.domain.ai.AiAttachment
import java.io.File
import androidx.compose.ui.unit.dp

@Composable
fun SolveScreen(nav: NavHostController, vm: SolveViewModel = hiltViewModel()) {
    val state by vm.state
    val context = LocalContext.current
    var subject by remember { mutableStateOf("Mathematics") }
    var showCamera by remember { mutableStateOf(false) }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let { vm.setAttachment(context.attachmentFromUri(it)) } }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { vm.setAttachment(context.attachmentFromUri(it)) } }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> showCamera = granted }
    ScreenScaffold("Snap & Solve", "Send images or PDFs directly to multimodal AI — no OCR path.") {
        NeoVedicTextField(subject, { subject = it }, "Subject", Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NeoVedicButton("Gallery", Modifier.weight(1f), secondary = true, icon = Icons.Outlined.AttachFile) { imagePicker.launch("image/*") }
            NeoVedicButton("File/PDF", Modifier.weight(1f), secondary = true, icon = Icons.Outlined.UploadFile) { filePicker.launch(arrayOf("application/pdf", "image/*")) }
        }
        NeoVedicButton("CameraX capture", Modifier.fillMaxWidth(), secondary = true, icon = Icons.Outlined.PhotoCamera) { permission.launch(Manifest.permission.CAMERA) }
        if (showCamera) CameraCaptureCard { uri -> vm.setAttachment(context.attachmentFromUri(uri)); showCamera = false }
        state.attachmentName?.let { NeoVedicCard(Modifier.fillMaxWidth(), emphasized = true) { Text("Selected: $it"); Text("Provider fallback will use file upload, PDF, or vision capability only.") } }
        NeoVedicButton(if (state.loading) "Solving…" else "Solve step by step", Modifier.fillMaxWidth(), enabled = state.attachmentName != null && !state.loading) { vm.solve(subject) }
        if (state.loading) NeoVedicCard(Modifier.fillMaxWidth(), emphasized = true) { Text("Drawing a Vedic grid around the question…"); LinearProgressIndicator(Modifier.fillMaxWidth()) }
        state.error?.let { NeoVedicEmptyState("Could not solve", it) { NeoVedicButton("Provider settings") { nav.navigate("settings") } } }
        state.answer?.let { NeoVedicAiAnswerCard(it.text, it.providerName, onSave = vm::saveSolved) }
    }
}

@Composable
private fun CameraCaptureCard(onCaptured: (Uri) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller = remember { LifecycleCameraController(context).apply { cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA; setEnabledUseCases(CameraController.IMAGE_CAPTURE) } }
    LaunchedEffect(controller) { controller.bindToLifecycle(lifecycleOwner) }
    NeoVedicCard(Modifier.fillMaxWidth(), emphasized = true) {
        AndroidView(factory = { PreviewView(it).apply { this.controller = controller } }, modifier = Modifier.fillMaxWidth().height(280.dp))
        NeoVedicButton("Capture question", Modifier.fillMaxWidth()) {
            val file = File(context.cacheDir, "sikai-capture-${System.currentTimeMillis()}.jpg")
            val options = ImageCapture.OutputFileOptions.Builder(file).build()
            controller.takePicture(options, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) { onCaptured(Uri.fromFile(file)) }
                override fun onError(exception: ImageCaptureException) {}
            })
        }
    }
}

private fun Context.attachmentFromUri(uri: Uri): AiAttachment {
    val mime = contentResolver.getType(uri) ?: if (uri.toString().endsWith(".pdf")) "application/pdf" else "image/jpeg"
    val name = uri.lastPathSegment?.substringAfterLast('/') ?: "sikai-attachment"
    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
    return AiAttachment(uri.toString(), mime, name, Base64.encodeToString(bytes, Base64.NO_WRAP))
}
