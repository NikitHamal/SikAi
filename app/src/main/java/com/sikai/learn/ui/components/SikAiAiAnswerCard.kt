package com.sikai.learn.ui.components

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.sikai.learn.ui.theme.SikAi
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin

@Composable
fun SikAiAiAnswerCard(
    markdown: String,
    providerLabel: String,
    modelLabel: String?,
    isStreaming: Boolean,
    modifier: Modifier = Modifier,
    onCopy: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null,
    onRetry: (() -> Unit)? = null,
) {
    val colors = SikAi.colors
    SikAiCard(modifier = modifier.fillMaxWidth(), emphasized = true) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SikAiStatusPill(text = providerLabel, accent = colors.accent)
                    if (modelLabel != null) {
                        Spacer(Modifier.width(6.dp))
                        SikAiStatusPill(text = modelLabel)
                    }
                }
                if (isStreaming) {
                    Text(
                        text = "STREAMING",
                        style = SikAi.type.label,
                        color = colors.accent
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            MarkdownText(markdown = markdown, color = colors.onSurface)

            if (onCopy != null || onSave != null || onRetry != null) {
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onCopy != null) ActionLabel("COPY", onCopy)
                    if (onSave != null) ActionLabel("SAVE", onSave)
                    if (onRetry != null) ActionLabel("RETRY", onRetry)
                }
            }
        }
    }
}

@Composable
private fun ActionLabel(text: String, onClick: () -> Unit) {
    Text(
        modifier = Modifier
            .padding(end = 16.dp)
            .clickable(onClick = onClick),
        text = text,
        style = SikAi.type.label,
        color = SikAi.colors.onSurfaceMuted
    )
}

@Composable
private fun MarkdownText(markdown: String, color: Color, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val markwon = remember(context) { buildMarkwon(context) }
    val argb = color.toArgb()
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            android.widget.TextView(ctx).apply {
                setTextColor(argb)
                textSize = 15f
                setLineSpacing(0f, 1.25f)
            }
        },
        update = { tv ->
            tv.setTextColor(argb)
            markwon.setMarkdown(tv, markdown)
        }
    )
}

private fun buildMarkwon(context: Context): Markwon = Markwon.builder(context)
    .usePlugin(HtmlPlugin.create())
    .usePlugin(StrikethroughPlugin.create())
    .usePlugin(TablePlugin.create(context))
    .build()
