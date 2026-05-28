package com.example.moonsyncapp.ui.theme.tokens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moonsyncapp.data.model.CyclePhase
import com.example.moonsyncapp.ui.theme.MoonSyncTheme
import com.example.moonsyncapp.ui.theme.MoonTheme

// ---------------------------------------------------------------------------
// Token swatch preview — shows every design token visually.
// Open in Android Studio → Split view to inspect all tokens at a glance.
// ---------------------------------------------------------------------------

@Preview(
    name = "Design Token Swatches",
    showBackground = true,
    backgroundColor = 0xFFF9EDF2,
    widthDp = 400,
    heightDp = 1200,
)
@Composable
fun TokenSwatchPreview() {
    MoonSyncTheme {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(MoonTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(MoonTheme.spacing.lg),
        ) {
            item { SwatchSectionHeader("SPACING") }
            item { SpacingSwatches() }

            item { SwatchSectionHeader("BORDER RADIUS") }
            item { RadiusSwatches() }

            item { SwatchSectionHeader("ELEVATION") }
            item { ElevationSwatches() }

            item { SwatchSectionHeader("ANIMATION DURATION") }
            item { DurationSwatches() }

            item { SwatchSectionHeader("PHASE PALETTES") }
            item { PhasePaletteSwatches() }
        }
    }
}

// ---------------------------------------------------------------------------
// Section header
// ---------------------------------------------------------------------------

@Composable
private fun SwatchSectionHeader(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = MoonTheme.spacing.xs),
    )
}

// ---------------------------------------------------------------------------
// Spacing swatches — horizontal bars scaled to token value
// ---------------------------------------------------------------------------

@Composable
private fun SpacingSwatches() {
    val spacing = MoonTheme.spacing
    val swatches = listOf(
        "xs  =  4 dp" to spacing.xs,
        "sm  =  8 dp" to spacing.sm,
        "md  = 16 dp" to spacing.md,
        "lg  = 24 dp" to spacing.lg,
        "xl  = 32 dp" to spacing.xl,
        "xxl = 48 dp" to spacing.xxl,
    )
    Column(verticalArrangement = Arrangement.spacedBy(MoonTheme.spacing.xs)) {
        swatches.forEach { (label, size) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(size)
                        .height(MoonTheme.spacing.sm)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(MoonTheme.radius.sm),
                        ),
                )
                Spacer(Modifier.width(MoonTheme.spacing.sm))
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Radius swatches — boxes showing each corner radius
// ---------------------------------------------------------------------------

@Composable
private fun RadiusSwatches() {
    val radius = MoonTheme.radius
    val swatches = listOf(
        "sm  =  8 dp" to radius.sm,
        "md  = 12 dp" to radius.md,
        "lg  = 16 dp" to radius.lg,
        "xl  = 24 dp" to radius.xl,
        "full = pill"  to radius.full,
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(MoonTheme.spacing.sm),
        modifier = Modifier.fillMaxWidth(),
    ) {
        swatches.forEach { (label, cornerRadius) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(cornerRadius),
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(cornerRadius),
                        ),
                )
                Spacer(Modifier.height(MoonTheme.spacing.xs))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Elevation swatches — cards at each elevation level
// ---------------------------------------------------------------------------

@Composable
private fun ElevationSwatches() {
    val elev = MoonTheme.elevation
    val swatches = listOf(
        "none\n0 dp"    to elev.none,
        "low\n1 dp"     to elev.low,
        "medium\n4 dp"  to elev.medium,
        "high\n8 dp"    to elev.high,
        "overlay\n12dp" to elev.overlay,
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(MoonTheme.spacing.sm),
        modifier = Modifier.fillMaxWidth(),
    ) {
        swatches.forEach { (label, elevation) ->
            Card(
                modifier = Modifier.weight(1f),
                elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Duration swatches — labels only (animation values can't be shown statically)
// ---------------------------------------------------------------------------

@Composable
private fun DurationSwatches() {
    val dur = MoonTheme.duration
    val swatches = listOf(
        "fast"     to "${dur.fast} ms",
        "standard" to "${dur.standard} ms",
        "slow"     to "${dur.slow} ms",
        "phase"    to "${dur.phase} ms",
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(MoonTheme.spacing.sm),
        modifier = Modifier.fillMaxWidth(),
    ) {
        swatches.forEach { (name, value) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(MoonTheme.radius.sm),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 9.sp,
                    )
                }
                Spacer(Modifier.height(MoonTheme.spacing.xs))
                Text(name, style = MaterialTheme.typography.labelSmall, fontSize = 8.sp)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Phase palette swatches — all 5 color roles for each phase
// ---------------------------------------------------------------------------

@Composable
private fun PhasePaletteSwatches() {
    Column(verticalArrangement = Arrangement.spacedBy(MoonTheme.spacing.md)) {
        CyclePhase.entries.forEach { phase ->
            PhasePaletteRow(phase)
        }
    }
}

@Composable
private fun PhasePaletteRow(phase: CyclePhase) {
    val tokens = phase.toColorTokens()
    val swatches = listOf(
        "fill"     to tokens.fill,
        "border"   to tokens.border,
        "progress" to tokens.progress,
        "text"     to tokens.text,
        "onFill"   to tokens.onFill,
        "bgTint"   to tokens.bgTint.copy(alpha = 1f), // show at full alpha in swatch
    )

    Column {
        // Phase label row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = tokens.fill,
                    shape = RoundedCornerShape(
                        topStart = MoonTheme.radius.sm,
                        topEnd = MoonTheme.radius.sm,
                    ),
                )
                .padding(horizontal = MoonTheme.spacing.sm, vertical = MoonTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = phase.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = tokens.text,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = phase.description,
                style = MaterialTheme.typography.labelSmall,
                color = tokens.onFill,
            )
        }

        // Color chip row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = tokens.border,
                    shape = RoundedCornerShape(
                        bottomStart = MoonTheme.radius.sm,
                        bottomEnd = MoonTheme.radius.sm,
                    ),
                )
                .padding(MoonTheme.spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(MoonTheme.spacing.xs),
        ) {
            swatches.forEach { (label, color) ->
                ColorSwatch(label, color, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ColorSwatch(label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clip(RoundedCornerShape(MoonTheme.radius.xs))
                .background(color)
                .border(
                    width = 0.5.dp,
                    color = Color.Black.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(MoonTheme.radius.xs),
                ),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 7.sp,
        )
    }
}
