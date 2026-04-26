package com.sikai.learn.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sikai.learn.ui.theme.NeoVedicTokens

enum class NeoVedicButtonStyle { Primary, Outline, Text, Gold }

@Composable
fun NeoVedicButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: NeoVedicButtonStyle = NeoVedicButtonStyle.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    val effectiveEnabled = enabled && !loading
    val height = Modifier.heightIn(min = NeoVedicTokens.MinTouchTarget)
    val padding = PaddingValues(horizontal = NeoVedicTokens.SpaceLg, vertical = NeoVedicTokens.SpaceMd)

    when (style) {
        NeoVedicButtonStyle.Primary -> Button(
            onClick = onClick,
            modifier = modifier.then(height),
            enabled = effectiveEnabled,
            shape = NeoVedicTokens.ShapeSharp,
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            contentPadding = padding,
        ) {
            ButtonContent(text, loading, leadingIcon)
        }
        NeoVedicButtonStyle.Outline -> OutlinedButton(
            onClick = onClick,
            modifier = modifier.then(height),
            enabled = effectiveEnabled,
            shape = NeoVedicTokens.ShapeSharp,
            border = BorderStroke(NeoVedicTokens.StrokeHairline, MaterialTheme.colorScheme.outline),
            contentPadding = padding,
        ) {
            ButtonContent(text, loading, leadingIcon)
        }
        NeoVedicButtonStyle.Text -> TextButton(
            onClick = onClick,
            modifier = modifier.then(height),
            enabled = effectiveEnabled,
            shape = NeoVedicTokens.ShapeSharp,
            contentPadding = padding,
        ) {
            ButtonContent(text, loading, leadingIcon)
        }
        NeoVedicButtonStyle.Gold -> Button(
            onClick = onClick,
            modifier = modifier.then(height),
            enabled = effectiveEnabled,
            shape = NeoVedicTokens.ShapeSharp,
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
            ),
            contentPadding = padding,
        ) {
            ButtonContent(text, loading, leadingIcon)
        }
    }
}

@Composable
private fun ButtonContent(text: String, loading: Boolean, leadingIcon: @Composable (() -> Unit)?) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(NeoVedicTokens.SpaceSm)) {
        if (loading) {
            CircularProgressIndicator(
                strokeWidth = 1.5.dp,
                modifier = Modifier.size(16.dp),
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else if (leadingIcon != null) {
            leadingIcon()
        }
        Text(text, style = MaterialTheme.typography.titleSmall)
    }
}
