package com.sikai.learn.core.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

fun Modifier.neoVedicCornerMarkers(color: Color = VedicGold): Modifier = drawBehind {
    val l = NeoVedicTokens.marker.toPx()
    val s = 1.5.dp.toPx()
    drawLine(color, Offset.Zero, Offset(l, 0f), s, cap = StrokeCap.Square)
    drawLine(color, Offset.Zero, Offset(0f, l), s, cap = StrokeCap.Square)
    drawLine(color, Offset(size.width, 0f), Offset(size.width - l, 0f), s, cap = StrokeCap.Square)
    drawLine(color, Offset(size.width, 0f), Offset(size.width, l), s, cap = StrokeCap.Square)
    drawLine(color, Offset(0f, size.height), Offset(l, size.height), s, cap = StrokeCap.Square)
    drawLine(color, Offset(0f, size.height), Offset(0f, size.height - l), s, cap = StrokeCap.Square)
    drawLine(color, Offset(size.width, size.height), Offset(size.width - l, size.height), s, cap = StrokeCap.Square)
    drawLine(color, Offset(size.width, size.height), Offset(size.width, size.height - l), s, cap = StrokeCap.Square)
}

@Composable
fun NeoVedicCard(modifier: Modifier = Modifier, emphasized: Boolean = false, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val p = LocalNeoVedicPalette.current
    val base = modifier
        .background(p.surface, RoundedCornerShape(NeoVedicTokens.corner))
        .border(NeoVedicTokens.border, if (emphasized) p.borderStrong else p.border, RoundedCornerShape(NeoVedicTokens.corner))
        .then(if (emphasized) Modifier.neoVedicCornerMarkers(p.accent) else Modifier)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(NeoVedicTokens.space4)
    Column(base, verticalArrangement = Arrangement.spacedBy(NeoVedicTokens.space3), content = content)
}

@Composable
fun NeoVedicButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, secondary: Boolean = false, icon: ImageVector? = null, onClick: () -> Unit) {
    val p = LocalNeoVedicPalette.current
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = NeoVedicTokens.touch),
        shape = RoundedCornerShape(NeoVedicTokens.corner),
        colors = ButtonDefaults.buttonColors(containerColor = if (secondary) p.surfaceHover else p.primary, contentColor = if (secondary) p.text else Vellum, disabledContainerColor = p.surfaceHover)
    ) {
        if (icon != null) Icon(icon, null, Modifier.size(18.dp))
        if (icon != null) Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun NeoVedicTextField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier, singleLine: Boolean = true, keyboardOptions: KeyboardOptions = KeyboardOptions.Default, visualTransformation: VisualTransformation = VisualTransformation.None) {
    val p = LocalNeoVedicPalette.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label) },
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(NeoVedicTokens.corner),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = p.accent, unfocusedBorderColor = p.border, focusedContainerColor = p.surface, unfocusedContainerColor = p.surface, focusedTextColor = p.text, unfocusedTextColor = p.text)
    )
}

@Composable
fun NeoVedicPageHeader(title: String, subtitle: String? = null, action: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineMedium, color = LocalNeoVedicPalette.current.text)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = LocalNeoVedicPalette.current.textMuted) }
        }
        action?.invoke()
    }
}

@Composable
fun NeoVedicStatusPill(text: String, modifier: Modifier = Modifier, tone: Color = LocalNeoVedicPalette.current.accent) {
    Text(text, modifier.background(tone.copy(alpha = 0.14f), RoundedCornerShape(NeoVedicTokens.corner)).border(1.dp, tone.copy(alpha = 0.5f), RoundedCornerShape(NeoVedicTokens.corner)).padding(horizontal = 8.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall, color = tone, fontWeight = FontWeight.Bold)
}

@Composable
fun NeoVedicEmptyState(title: String, body: String, icon: ImageVector = Icons.Outlined.School, action: (@Composable () -> Unit)? = null) {
    NeoVedicCard(Modifier.fillMaxWidth(), emphasized = true) {
        Icon(icon, null, tint = LocalNeoVedicPalette.current.accent, modifier = Modifier.size(32.dp))
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = LocalNeoVedicPalette.current.textMuted)
        action?.invoke()
    }
}

@Composable
fun NeoVedicSectionTitle(text: String, modifier: Modifier = Modifier) {
    val markerColor = LocalNeoVedicPalette.current.accent
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(7.dp)) { drawCircle(markerColor) }
        Spacer(Modifier.width(8.dp))
        Text(text.uppercase(), style = MaterialTheme.typography.labelLarge, color = LocalNeoVedicPalette.current.textMuted)
    }
}

@Composable
fun NeoVedicLearningCard(title: String, body: String, meta: String, icon: ImageVector = Icons.Outlined.AutoStories, onClick: () -> Unit = {}) {
    NeoVedicCard(Modifier.fillMaxWidth(), emphasized = true, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = LocalNeoVedicPalette.current.accent)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(body, style = MaterialTheme.typography.bodyMedium, color = LocalNeoVedicPalette.current.textMuted)
            }
            NeoVedicStatusPill(meta)
        }
    }
}

@Composable
fun NeoVedicDownloadCard(title: String, subtitle: String, status: String, onDownload: () -> Unit) {
    NeoVedicCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Download, null, tint = LocalNeoVedicPalette.current.accent)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = LocalNeoVedicPalette.current.textMuted)
            }
            NeoVedicButton(status, secondary = status != "Download", onClick = onDownload)
        }
    }
}

@Composable
fun NeoVedicAiAnswerCard(answer: String, provider: String, onCopy: () -> Unit = {}, onSave: () -> Unit = {}) {
    NeoVedicCard(Modifier.fillMaxWidth(), emphasized = true) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            NeoVedicStatusPill("AI · $provider")
            Row { TextButton(onClick = onCopy) { Text("Copy") }; TextButton(onClick = onSave) { Text("Save") } }
        }
        MarkdownText(answer)
    }
}

@Composable
fun MarkdownText(markdown: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        markdown.lines().filter { it.isNotBlank() }.forEach { raw ->
            val line = raw.trim()
            when {
                line.startsWith("###") -> Text(line.removePrefix("###").trim(), style = MaterialTheme.typography.titleMedium)
                line.startsWith("##") -> Text(line.removePrefix("##").trim(), style = MaterialTheme.typography.titleLarge)
                line.startsWith("#") -> Text(line.removePrefix("#").trim(), style = MaterialTheme.typography.headlineMedium)
                line.startsWith("-") || line.startsWith("*") -> Text("• " + line.drop(1).trim(), style = MaterialTheme.typography.bodyMedium)
                else -> Text(line.replace("**", ""), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun ScreenScaffold(title: String, subtitle: String? = null, action: (@Composable () -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(LocalNeoVedicPalette.current.background).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        NeoVedicPageHeader(title, subtitle, action)
        content()
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
fun NeoVedicBottomNav(items: List<Pair<String, ImageVector>>, selected: Int, onSelect: (Int) -> Unit) {
    NavigationBar(containerColor = LocalNeoVedicPalette.current.surface, tonalElevation = 0.dp, modifier = Modifier.border(1.dp, LocalNeoVedicPalette.current.border)) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(selected = selected == index, onClick = { onSelect(index) }, icon = { Icon(item.second, null) }, label = { Text(item.first) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = LocalNeoVedicPalette.current.primary, selectedTextColor = LocalNeoVedicPalette.current.primary, indicatorColor = LocalNeoVedicPalette.current.accent.copy(alpha = .18f)))
        }
    }
}

@Preview
@Composable
private fun ComponentsPreview() {
    NeoVedicTheme { NeoVedicCard(Modifier.padding(16.dp), emphasized = true) { Text("SikAi"); NeoVedicStatusPill("SEE Ready") } }
}
