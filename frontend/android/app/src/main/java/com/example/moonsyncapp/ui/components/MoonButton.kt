package com.example.moonsyncapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.moonsyncapp.data.model.CyclePhase
import com.example.moonsyncapp.ui.theme.MoonSyncTheme
import com.example.moonsyncapp.ui.theme.MoonTheme
import com.example.moonsyncapp.ui.theme.tokens.toColorTokens

// ---------------------------------------------------------------------------
// Enums
// ---------------------------------------------------------------------------

enum class MoonButtonVariant {
    /** Solid filled — primary action per screen. */
    Primary,
    /** Outlined — secondary action, equal importance to ghost. */
    Secondary,
    /** Text-only with transparent background — tertiary/inline action. */
    Ghost,
    /** Solid filled with destructive (error) palette — destructive actions. */
    Destructive,
}

enum class MoonButtonSize {
    Small,   // 32 dp height, labelSmall
    Medium,  // 44 dp height, labelLarge  (default)
    Large,   // 56 dp height, bodyMedium
}

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

/**
 * MoonSync primary button component.
 *
 * @param onClick    Action fired on tap.
 * @param label      Button label text.
 * @param modifier   Modifier passed to the outer surface.
 * @param variant    Visual style — Primary, Secondary, Ghost, or Destructive.
 * @param size       Height tier — Small, Medium, or Large.
 * @param phaseColor Optional: tints Primary variant with the given cycle phase.
 * @param loading    When true, replaces label + icon with a spinner; disables tap.
 * @param icon       Optional leading icon.
 * @param enabled    When false, renders at disabled alpha and blocks interaction.
 */
@Composable
fun MoonButton(
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    variant: MoonButtonVariant = MoonButtonVariant.Primary,
    size: MoonButtonSize = MoonButtonSize.Medium,
    phaseColor: CyclePhase? = null,
    loading: Boolean = false,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val radius  = MoonTheme.radius
    val spacing = MoonTheme.spacing
    val colors  = MoonTheme.colors

    val shape = RoundedCornerShape(radius.full)
    val isInteractive = enabled && !loading

    // Heights
    val height: Dp = when (size) {
        MoonButtonSize.Small  -> 32.dp
        MoonButtonSize.Medium -> 44.dp
        MoonButtonSize.Large  -> 56.dp
    }

    // Horizontal padding
    val hPad: Dp = when (size) {
        MoonButtonSize.Small  -> spacing.md
        MoonButtonSize.Medium -> spacing.lg
        MoonButtonSize.Large  -> spacing.xl
    }

    // Icon size
    val iconSize: Dp = when (size) {
        MoonButtonSize.Small  -> 14.dp
        MoonButtonSize.Medium -> 18.dp
        MoonButtonSize.Large  -> 22.dp
    }

    // Text style
    val textStyle = when (size) {
        MoonButtonSize.Small  -> MoonTheme.typography.labelSmall
        MoonButtonSize.Medium -> MoonTheme.typography.labelLarge
        MoonButtonSize.Large  -> MoonTheme.typography.bodyMedium
    }

    // Phase override (Primary only)
    val phaseTokens = phaseColor?.toColorTokens()

    // Resolve colours per variant
    val containerColor: Color
    val contentColor: Color
    val borderColor: Color?

    when (variant) {
        MoonButtonVariant.Primary -> {
            containerColor = phaseTokens?.border ?: colors.primary
            contentColor   = if (phaseTokens != null) Color.White else colors.onPrimary
            borderColor    = null
        }
        MoonButtonVariant.Secondary -> {
            containerColor = Color.Transparent
            contentColor   = phaseTokens?.text ?: colors.primary
            borderColor    = phaseTokens?.border ?: colors.primary
        }
        MoonButtonVariant.Ghost -> {
            containerColor = Color.Transparent
            contentColor   = phaseTokens?.text ?: colors.primary
            borderColor    = null
        }
        MoonButtonVariant.Destructive -> {
            containerColor = colors.error
            contentColor   = colors.onError
            borderColor    = null
        }
    }

    val buttonColors = ButtonDefaults.buttonColors(
        containerColor         = containerColor,
        contentColor           = contentColor,
        disabledContainerColor = containerColor.copy(alpha = 0.38f),
        disabledContentColor   = contentColor.copy(alpha = 0.38f),
    )

    Button(
        onClick = onClick,
        enabled = isInteractive,
        shape = shape,
        colors = buttonColors,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation  = if (variant == MoonButtonVariant.Primary) MoonTheme.elevation.low else 0.dp,
            pressedElevation  = 0.dp,
        ),
        contentPadding = PaddingValues(horizontal = hPad),
        modifier = modifier
            .height(height)
            .then(
                if (borderColor != null)
                    Modifier.border(width = 1.5.dp, color = if (isInteractive) borderColor else borderColor.copy(alpha = 0.38f), shape = shape)
                else Modifier
            )
            .semantics { role = Role.Button },
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(iconSize),
                strokeWidth = 2.dp,
                color = contentColor,
            )
        } else {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                )
                Spacer(Modifier.width(spacing.xs))
            }
            Text(text = label, style = textStyle)
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "MoonButton — variants", showBackground = true)
@Composable
private fun MoonButtonVariantsPreview() {
    MoonSyncTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(
                MoonButtonVariant.Primary,
                MoonButtonVariant.Secondary,
                MoonButtonVariant.Ghost,
                MoonButtonVariant.Destructive,
            ).forEach { variant ->
                MoonButton(
                    onClick = {},
                    label = variant.name,
                    variant = variant,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(name = "MoonButton — sizes", showBackground = true)
@Composable
private fun MoonButtonSizesPreview() {
    MoonSyncTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MoonButtonSize.entries.forEach { size ->
                MoonButton(onClick = {}, label = size.name, size = size)
            }
        }
    }
}

@Preview(name = "MoonButton — phase + loading", showBackground = true)
@Composable
private fun MoonButtonPhasePreview() {
    MoonSyncTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CyclePhase.entries.forEach { phase ->
                MoonButton(
                    onClick = {},
                    label = phase.displayName,
                    phaseColor = phase,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            MoonButton(
                onClick = {},
                label = "Saving…",
                loading = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
