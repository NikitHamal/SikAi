package com.sikai.learn.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.sikai.learn.ui.theme.SikAi

@Composable
fun SikAiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    helper: String? = null,
    isError: Boolean = false,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    masked: Boolean = false,
) {
    val colors = SikAi.colors
    val tokens = SikAi.tokens
    val type = SikAi.type

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label.uppercase(),
                style = type.sectionTitle,
                color = colors.onSurfaceMuted
            )
            Spacer(Modifier.height(6.dp))
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (masked) PasswordVisualTransformation() else VisualTransformation.None,
            textStyle = type.bodyLarge.copy(color = colors.onSurface),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.primary),
            modifier = Modifier
                .fillMaxWidth()
                .clip(tokens.cornerSharp)
                .background(colors.surfaceMuted)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            decorationBox = { inner ->
                if (value.isEmpty() && placeholder != null) {
                    Text(
                        text = placeholder,
                        style = type.bodyLarge,
                        color = colors.onSurfaceMuted
                    )
                }
                inner()
            }
        )
        if (helper != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = helper,
                style = type.caption,
                color = if (isError) colors.danger else colors.onSurfaceMuted
            )
        }
    }
}
