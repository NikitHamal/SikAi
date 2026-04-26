package com.sikai.learn.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.sikai.learn.ui.theme.NeoVedicTokens

@Composable
fun NeoVedicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    isError: Boolean = false,
    supportingText: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        isError = isError,
        label = if (label != null) { { Text(label, style = MaterialTheme.typography.labelMedium) } } else null,
        placeholder = if (placeholder != null) { { Text(placeholder) } } else null,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        shape = NeoVedicTokens.ShapeSharp,
        supportingText = if (supportingText != null) { { Text(supportingText, style = MaterialTheme.typography.bodySmall) } } else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
        ),
    )
}
