package com.example.moonsyncapp.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

enum class MoonCardElevation {
    /** No shadow — flat, background-level surface. */
    None,
    /** 1 dp tonal lift — list items, subtle separation. */
    Low,
    /** 4 dp tonal lift — content cards, chips. */
    Medium,
    /** 8 dp tonal lift — floating panels, raised cards. */
    High,
}

enum class MoonCardBorder {
    /** No visible border stroke. */
    None,
    /** 1.5 dp border using the supplied [phase]'s border color. */
    Phase,
    /** 1 dp muted outline border. */
    Subtle,
}

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

/**
 * MoonSync surface card.
 *
 * Wraps Material 3's [Card] with token-driven elevation and border options.
 * Use [MoonCardBorder.Phase] together with a [phase] argument to draw a
 * phase-coloured accent border — e.g. for the advice card on the home screen.
 *
 * @param modifier        Modifier for the outer card.
 * @param elevation       Shadow / tonal elevation tier.
 * @param border          Border style.
 * @param phase           Required when [border] == [MoonCardBorder.Phase].
 * @param onClick         Optional click handler; makes the card interactive.
 * @param content         Card body content.
 */
@Composable
fun MoonCard(
    modifier: Modifier = Modifier,
    elevation: MoonCardElevation = MoonCardElevation.Low,
    border: MoonCardBorder = MoonCardBorder.None,
    phase: CyclePhase? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val elevTokens = MoonTheme.elevation
    val radius     = MoonTheme.radius
    val colors     = MoonTheme.colors
    val shape      = RoundedCornerShape(radius.lg)

    val dp: Dp = when (elevation) {
        MoonCardElevation.None   -> elevTokens.none
        MoonCardElevation.Low    -> elevTokens.low
        MoonCardElevation.Medium -> elevTokens.medium
        MoonCardElevation.High   -> elevTokens.high
    }

    val borderMod: Modifier = when (border) {
        MoonCardBorder.None   -> Modifier
        MoonCardBorder.Subtle -> Modifier.border(
            width = 1.dp,
            color = colors.outlineVariant,
            shape = shape,
        )
        MoonCardBorder.Phase  -> {
            val phaseColors = phase?.toColorTokens()
            if (phaseColors != null) {
                Modifier.border(
                    width = 1.5.dp,
                    color = phaseColors.border,
                    shape = shape,
                )
            } else Modifier
        }
    }

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.then(borderMod),
            shape = shape,
            elevation = CardDefaults.cardElevation(defaultElevation = dp),
            content = content,
        )
    } else {
        Card(
            modifier = modifier.then(borderMod),
            shape = shape,
            elevation = CardDefaults.cardElevation(defaultElevation = dp),
            content = content,
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "MoonCard — elevation & border variants", showBackground = true)
@Composable
private fun MoonCardPreview() {
    MoonSyncTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MoonCardElevation.entries.forEach { elev ->
                MoonCard(elevation = elev, modifier = Modifier.fillMaxWidth()) {
                    Box(Modifier.padding(16.dp)) {
                        androidx.compose.material3.Text(
                            "Elevation: ${elev.name}",
                            style = MoonTheme.typography.labelLarge,
                        )
                    }
                }
            }

            MoonCard(
                elevation = MoonCardElevation.Low,
                border = MoonCardBorder.Subtle,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(Modifier.padding(16.dp)) {
                    androidx.compose.material3.Text("Subtle border", style = MoonTheme.typography.labelLarge)
                }
            }

            CyclePhase.entries.forEach { phase ->
                MoonCard(
                    elevation = MoonCardElevation.None,
                    border = MoonCardBorder.Phase,
                    phase = phase,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        androidx.compose.material3.Text(
                            "${phase.displayName} phase card",
                            style = MoonTheme.typography.labelLarge,
                            color = phase.toColorTokens().text,
                        )
                    }
                }
            }
        }
    }
}
