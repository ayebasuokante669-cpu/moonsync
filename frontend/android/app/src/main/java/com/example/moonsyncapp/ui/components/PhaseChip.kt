package com.example.moonsyncapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moonsyncapp.data.model.CyclePhase
import com.example.moonsyncapp.ui.theme.MoonSyncTheme
import com.example.moonsyncapp.ui.theme.MoonTheme
import com.example.moonsyncapp.ui.theme.tokens.toColorTokens

/**
 * Non-interactive phase label chip — used to tag community posts,
 * articles, and timeline entries with a cycle phase.
 *
 * Unlike [MoonChip], this is display-only (no selection state, no click).
 * Use [MoonChip] with a phase color whenever selection is needed.
 *
 * @param phase    The cycle phase this chip represents.
 * @param label    Override text; defaults to the phase's short display name.
 * @param modifier Modifier for the outer container.
 */
@Composable
fun PhaseChip(
    phase: CyclePhase,
    modifier: Modifier = Modifier,
    label: String = phase.displayName,
) {
    val tokens  = phase.toColorTokens()
    val radius  = MoonTheme.radius
    val spacing = MoonTheme.spacing
    val shape   = RoundedCornerShape(radius.full)

    // Dot size
    val dotSize = 6.dp

    Row(
        modifier = modifier
            .background(color = tokens.fill, shape = shape)
            .border(width = 1.dp, color = tokens.border, shape = shape)
            .padding(horizontal = spacing.sm, vertical = spacing.xs)
            .semantics { contentDescription = "${phase.displayName} phase" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        // Phase colour dot
        Box(
            modifier = Modifier
                .size(dotSize)
                .background(color = tokens.border, shape = RoundedCornerShape(dotSize / 2)),
        )
        Text(
            text = label,
            style = MoonTheme.typography.labelSmall,
            color = tokens.text,
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "PhaseChip — all phases", showBackground = true)
@Composable
private fun PhaseChipPreview() {
    MoonSyncTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CyclePhase.entries.forEach { phase ->
                PhaseChip(phase = phase)
            }

            // Short-label variant
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CyclePhase.entries.forEach { phase ->
                    PhaseChip(
                        phase = phase,
                        label = phase.name.take(3), // MEN / FOL / OVU / LUT
                    )
                }
            }
        }
    }
}
