package com.example.moonsyncapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moonsyncapp.ui.theme.MoonSyncTheme
import com.example.moonsyncapp.ui.theme.MoonTheme

/**
 * MoonSync quick-log chip.
 *
 * Tap toggles selected state with a spring scale animation and haptic feedback.
 * Designed for the logging screen's 40-item grid.
 *
 * @param selected  Whether this chip is currently selected.
 * @param label     Chip text label.
 * @param icon      Optional leading icon.
 * @param onSelect  Callback fired on every tap (caller manages selection state).
 * @param modifier  Modifier passed to the chip container.
 */
@Composable
fun MoonChip(
    selected: Boolean,
    label: String,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val radius  = MoonTheme.radius
    val spacing = MoonTheme.spacing
    val colors  = MoonTheme.colors

    val haptic = LocalHapticFeedback.current
    val shape  = RoundedCornerShape(radius.full)

    // Spring scale: nudge up on select, back to 1 on deselect
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.07f else 1.0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 400f),
        label = "ChipScale",
    )

    val containerColor = if (selected) colors.primaryContainer else colors.surface
    val contentColor   = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant
    val borderColor    = if (selected) colors.primary else colors.outlineVariant

    Row(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(color = containerColor, shape = shape)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .clickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSelect()
                },
            )
            .padding(horizontal = spacing.sm, vertical = spacing.xs)
            .semantics {
                role = Role.Checkbox
                this.selected = selected
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp),
            )
        }
        Text(
            text = label,
            style = MoonTheme.typography.labelMedium,
            color = contentColor,
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "MoonChip — states", showBackground = true)
@Composable
private fun MoonChipPreview() {
    MoonSyncTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            var selectedA by remember { mutableStateOf(false) }
            var selectedB by remember { mutableStateOf(true) }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MoonChip(
                    selected = selectedA,
                    label = "Energetic",
                    onSelect = { selectedA = !selectedA },
                )
                MoonChip(
                    selected = selectedB,
                    label = "Cramps",
                    onSelect = { selectedB = !selectedB },
                )
                MoonChip(
                    selected = false,
                    label = "Anxious",
                    onSelect = {},
                )
            }
        }
    }
}
