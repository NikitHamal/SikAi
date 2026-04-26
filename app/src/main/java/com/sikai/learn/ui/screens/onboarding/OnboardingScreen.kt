package com.sikai.learn.ui.screens.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sikai.learn.ui.components.HairlineDivider
import com.sikai.learn.ui.components.NeoVedicButton
import com.sikai.learn.ui.components.NeoVedicButtonStyle
import com.sikai.learn.ui.components.NeoVedicCard
import com.sikai.learn.ui.components.neoVedicCornerMarkers
import com.sikai.learn.ui.theme.NeoVedicTokens

@Composable
fun OnboardingScreen(
    vm: OnboardingViewModel = hiltViewModel(),
    onComplete: () -> Unit,
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(state.finished) { if (state.finished) onComplete() }

    Column(modifier = Modifier.fillMaxSize().padding(NeoVedicTokens.SpaceLg)) {
        BrandMark()
        Spacer(Modifier.height(NeoVedicTokens.SpaceXl))

        when (state.step) {
            0 -> WelcomeStep(onNext = vm::nextStep)
            1 -> ClassStep(state, vm::setClass, vm::nextStep, vm::prevStep)
            2 -> LanguageStep(state, vm::setLanguage, vm::nextStep, vm::prevStep)
            3 -> SubjectsStep(state, vm::toggleSubject, vm::nextStep, vm::prevStep)
            else -> ExamDateStep(state, vm::setExamDate, vm::finish, vm::prevStep, state.saving)
        }
    }
}

@Composable
private fun BrandMark() {
    Column {
        Text(
            "SIKAI",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            "AI learning for Nepal",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(NeoVedicTokens.SpaceSm))
        Box(
            modifier = Modifier
                .height(2.dp)
                .fillMaxWidth(0.25f)
                .background(MaterialTheme.colorScheme.secondary),
        )
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    NeoVedicCard(modifier = Modifier.fillMaxWidth().neoVedicCornerMarkers(), showCornerMarkers = false) {
        Column(verticalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceMd)) {
            Text(
                "Welcome",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "SikAi works fully offline once content is downloaded. AI tutoring uses free providers by default — you can add your own keys later. No login required.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HairlineDivider()
            Bullet("Designed for Class 8, SEE (Class 10), and NEB (Class 12)")
            Bullet("Free Qwen AI by default — no signup")
            Bullet("Past papers, MCQs, and offline study packs")
            Bullet("Snap & solve — point your camera at a problem")
            Spacer(Modifier.height(NeoVedicTokens.SpaceSm))
            NeoVedicButton(
                "Get started",
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { androidx.compose.material3.Icon(Icons.AutoMirrored.Filled.ArrowForward, null) },
            )
        }
    }
}

@Composable
private fun ClassStep(
    state: OnboardingState,
    onSelectClass: (Int) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
) {
    StepShell(
        title = "Choose your class",
        description = "Select the class you are studying. We will load the right syllabus and past papers.",
        onPrev = onPrev,
        onNext = onNext,
        nextEnabled = true,
    ) {
        listOf(8, 10, 12).forEach { c ->
            ClassChoice(
                level = c,
                selected = state.classLevel == c,
                onSelect = { onSelectClass(c) },
            )
        }
    }
}

@Composable
private fun ClassChoice(level: Int, selected: Boolean, onSelect: () -> Unit) {
    val board = when (level) { 8 -> "Lower Secondary"; 10 -> "SEE board"; else -> "NEB board" }
    val border = if (selected) BorderStroke(NeoVedicTokens.StrokeStrong, MaterialTheme.colorScheme.secondary)
    else BorderStroke(NeoVedicTokens.StrokeHairline, MaterialTheme.colorScheme.outlineVariant)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(NeoVedicTokens.ShapeSharp)
            .border(border, NeoVedicTokens.ShapeSharp)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onSelect)
            .padding(NeoVedicTokens.SpaceLg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceLg),
    ) {
        Text(
            "$level",
            style = MaterialTheme.typography.headlineMedium,
            color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text("Class $level", style = MaterialTheme.typography.titleMedium)
            Text(board, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LanguageStep(
    state: OnboardingState,
    onPick: (String) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
) {
    StepShell(
        title = "Choose your language",
        description = "AI tutoring works in both English and Nepali. You can always change this in Settings.",
        onPrev = onPrev,
        onNext = onNext,
        nextEnabled = true,
    ) {
        listOf("en" to "English", "ne" to "नेपाली (Nepali-ready)").forEach { (code, label) ->
            val selected = state.language == code
            val border = if (selected) BorderStroke(NeoVedicTokens.StrokeStrong, MaterialTheme.colorScheme.secondary)
            else BorderStroke(NeoVedicTokens.StrokeHairline, MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(NeoVedicTokens.ShapeSharp)
                    .border(border, NeoVedicTokens.ShapeSharp)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onPick(code) }
                    .padding(NeoVedicTokens.SpaceLg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(label, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun SubjectsStep(
    state: OnboardingState,
    onToggle: (String) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
) {
    StepShell(
        title = "Pick your subjects",
        description = "Choose what to focus on. You can always change this later.",
        onPrev = onPrev,
        onNext = onNext,
        nextEnabled = state.subjects.isNotEmpty(),
    ) {
        state.availableSubjects.forEach { subject ->
            val selected = state.subjects.contains(subject.name)
            val border = if (selected) BorderStroke(NeoVedicTokens.StrokeStrong, MaterialTheme.colorScheme.secondary)
            else BorderStroke(NeoVedicTokens.StrokeHairline, MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(NeoVedicTokens.ShapeSharp)
                    .border(border, NeoVedicTokens.ShapeSharp)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onToggle(subject.name) }
                    .padding(NeoVedicTokens.SpaceLg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceLg),
            ) {
                SubjectGlyph(subject.symbol)
                Text(subject.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(
                    if (selected) "ADDED" else "ADD",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SubjectGlyph(text: String) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(BorderStroke(NeoVedicTokens.StrokeHairline, MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(2.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
    }
}

@Composable
private fun ExamDateStep(
    state: OnboardingState,
    onSetDate: (Long?) -> Unit,
    onFinish: () -> Unit,
    onPrev: () -> Unit,
    saving: Boolean,
) {
    StepShell(
        title = "Exam date (optional)",
        description = "If you have a target exam date we'll build a study plan around it. You can skip this and set it later.",
        onPrev = onPrev,
        onNext = onFinish,
        nextLabel = "Start using SikAi",
        nextLoading = saving,
        nextEnabled = !saving,
    ) {
        Text(
            "Examples: SEE 2082 → set the date your school posts.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        // Simple offset chips (today + N days)
        Row(horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm)) {
            listOf("30d" to 30, "90d" to 90, "180d" to 180).forEach { (label, days) ->
                val ts = System.currentTimeMillis() + days * 24L * 60 * 60 * 1000
                val selected = state.examDate?.let { kotlin.math.abs(it - ts) < 86_400_000 } == true
                val border = if (selected) BorderStroke(NeoVedicTokens.StrokeStrong, MaterialTheme.colorScheme.secondary)
                else BorderStroke(NeoVedicTokens.StrokeHairline, MaterialTheme.colorScheme.outlineVariant)
                Box(
                    modifier = Modifier
                        .clip(NeoVedicTokens.ShapeSharp)
                        .border(border, NeoVedicTokens.ShapeSharp)
                        .clickable { onSetDate(ts) }
                        .padding(horizontal = NeoVedicTokens.SpaceLg, vertical = NeoVedicTokens.SpaceMd),
                ) { Text("In $label") }
            }
            Box(
                modifier = Modifier
                    .clip(NeoVedicTokens.ShapeSharp)
                    .border(BorderStroke(NeoVedicTokens.StrokeHairline, MaterialTheme.colorScheme.outlineVariant), NeoVedicTokens.ShapeSharp)
                    .clickable { onSetDate(null) }
                    .padding(horizontal = NeoVedicTokens.SpaceLg, vertical = NeoVedicTokens.SpaceMd),
            ) { Text("Skip") }
        }
        Spacer(Modifier.height(NeoVedicTokens.SpaceMd))
        Text(
            "API keys for Gemini / OpenRouter / NVIDIA / DeepSeek can be added in Settings → AI Providers. Free Qwen and DeepInfra are configured by default.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StepShell(
    title: String,
    description: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    nextEnabled: Boolean = true,
    nextLoading: Boolean = false,
    nextLabel: String = "Continue",
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceLg)) {
        Column {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm),
        ) {
            item { Box(modifier = Modifier.fillMaxWidth()) { Column(verticalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm)) { content() } } }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceMd)) {
            NeoVedicButton("Back", onClick = onPrev, style = NeoVedicButtonStyle.Outline, modifier = Modifier.weight(1f))
            NeoVedicButton(nextLabel, onClick = onNext, enabled = nextEnabled, loading = nextLoading, modifier = Modifier.weight(2f))
        }
    }
}

@Composable
private fun Bullet(text: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm)) {
        Text("◆", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Start)
    }
}

