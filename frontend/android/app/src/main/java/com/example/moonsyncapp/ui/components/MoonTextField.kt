package com.example.moonsyncapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moonsyncapp.ui.theme.MoonSyncTheme
import com.example.moonsyncapp.ui.theme.MoonTheme

// ---------------------------------------------------------------------------
// Enum
// ---------------------------------------------------------------------------

enum class MoonTextFieldVariant {
    /** Standard outlined box — use for most form fields. */
    Outlined,
    /** Filled surface — use in forms embedded in colored cards. */
    Filled,
    /** No visible border or fill — use inline in content (search, journal). */
    Ghost,
}

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

/**
 * MoonSync text field wrapping Material 3's OutlinedTextField / TextField.
 *
 * @param value           Current text value.
 * @param onValueChange   Callback on text change.
 * @param label           Floating label shown above the field when focused.
 * @param modifier        Modifier passed to the outer container.
 * @param variant         Visual style — Outlined, Filled, or Ghost.
 * @param placeholder     Placeholder shown when field is empty.
 * @param error           Error message; non-null shows red state + message below.
 * @param supportingText  Helper text shown below field when no error is active.
 * @param trailingIcon    Composable rendered at the trailing end.
 * @param leadingIcon     Composable rendered at the leading end.
 * @param singleLine      When true, prevents line breaks (default false).
 * @param maxLines        Max visible lines (default Int.MAX_VALUE).
 * @param visualTransformation Transformation applied to displayed text (e.g. password).
 * @param keyboardOptions  Keyboard configuration.
 * @param keyboardActions  Keyboard action callbacks.
 * @param enabled         Whether the field accepts input.
 */
@Composable
fun MoonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    variant: MoonTextFieldVariant = MoonTextFieldVariant.Outlined,
    placeholder: String? = null,
    error: String? = null,
    supportingText: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    enabled: Boolean = true,
) {
    val radius = MoonTheme.radius
    val colors = MoonTheme.colors
    val isError = error != null
    val shape = RoundedCornerShape(radius.md)

    val fieldColors = when (variant) {
        MoonTextFieldVariant.Outlined -> OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = colors.primary,
            unfocusedBorderColor = colors.outline,
            errorBorderColor     = colors.error,
            focusedLabelColor    = colors.primary,
            errorLabelColor      = colors.error,
        )
        MoonTextFieldVariant.Filled -> TextFieldDefaults.colors(
            focusedContainerColor   = colors.surfaceVariant,
            unfocusedContainerColor = colors.surfaceVariant,
            focusedIndicatorColor   = colors.primary,
            unfocusedIndicatorColor = Color.Transparent,
            errorIndicatorColor     = colors.error,
        )
        MoonTextFieldVariant.Ghost -> OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            errorBorderColor     = colors.error,
            focusedContainerColor   = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
        )
    }

    Column(modifier = modifier) {
        when (variant) {
            MoonTextFieldVariant.Outlined, MoonTextFieldVariant.Ghost -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = label?.let { { Text(it) } },
                    placeholder = placeholder?.let { { Text(it, color = colors.onSurfaceVariant) } },
                    trailingIcon = trailingIcon,
                    leadingIcon = leadingIcon,
                    isError = isError,
                    singleLine = singleLine,
                    maxLines = maxLines,
                    visualTransformation = visualTransformation,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    enabled = enabled,
                    shape = shape,
                    colors = fieldColors,
                )
            }
            MoonTextFieldVariant.Filled -> {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = label?.let { { Text(it) } },
                    placeholder = placeholder?.let { { Text(it, color = colors.onSurfaceVariant) } },
                    trailingIcon = trailingIcon,
                    leadingIcon = leadingIcon,
                    isError = isError,
                    singleLine = singleLine,
                    maxLines = maxLines,
                    visualTransformation = visualTransformation,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    enabled = enabled,
                    shape = shape,
                    colors = fieldColors,
                )
            }
        }

        // Error or supporting text
        val subText = if (isError) error else supportingText
        if (subText != null) {
            Text(
                text = subText,
                style = MoonTheme.typography.labelSmall,
                color = if (isError) colors.error else colors.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = MoonTheme.spacing.md,
                    top = MoonTheme.spacing.xs,
                ),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "MoonTextField — variants", showBackground = true)
@Composable
private fun MoonTextFieldPreview() {
    MoonSyncTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            var text by remember { mutableStateOf("") }
            var filled by remember { mutableStateOf("Some text") }

            MoonTextField(
                value = text,
                onValueChange = { text = it },
                label = "Outlined",
                placeholder = "Enter something…",
                variant = MoonTextFieldVariant.Outlined,
            )
            MoonTextField(
                value = filled,
                onValueChange = { filled = it },
                label = "Filled",
                variant = MoonTextFieldVariant.Filled,
            )
            MoonTextField(
                value = "Ghost input",
                onValueChange = {},
                placeholder = "Ghost input…",
                variant = MoonTextFieldVariant.Ghost,
            )
            MoonTextField(
                value = "Wrong value",
                onValueChange = {},
                label = "With error",
                error = "This field is required",
                variant = MoonTextFieldVariant.Outlined,
            )
            MoonTextField(
                value = "With hint",
                onValueChange = {},
                label = "Supporting text",
                supportingText = "Helper text below the field",
                variant = MoonTextFieldVariant.Outlined,
            )
        }
    }
}
