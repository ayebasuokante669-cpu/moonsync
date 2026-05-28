package com.example.moonsyncapp.ui.components

import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moonsyncapp.data.model.CyclePhase
import com.example.moonsyncapp.ui.theme.MoonSyncTheme
import com.example.moonsyncapp.ui.theme.MoonTheme
import com.example.moonsyncapp.ui.theme.tokens.toColorTokens

/**
 * MoonSync signature phase ring.
 *
 * Renders a circular 360° progress track with a phase-coloured filled arc,
 * the cycle day number centred inside, and the phase name below the ring.
 *
 * The arc animates from 0 → [progress] on first composition with a 600 ms
 * ease-out curve. If the system animator scale is 0 (i.e. "Disable animations"
 * is on in developer settings), the arc appears instantly.
 *
 * @param phase          Current cycle phase — drives the arc and track colours.
 * @param progress       Arc fill fraction 0f..1f (0 = empty, 1 = full circle).
 * @param day            Current cycle day displayed in the centre.
 * @param size           Outer diameter of the ring. Defaults to 200 dp.
 * @param remainingDays  Days remaining in this phase; used in content description.
 * @param modifier       Modifier for the outer [Box].
 */
@Composable
fun PhaseRing(
    phase: CyclePhase,
    progress: Float,
    day: Int,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    remainingDays: Int = 0,
) {
    val tokens   = phase.toColorTokens()
    val duration = MoonTheme.duration

    // -------------------------------------------------------------------------
    // Reduced-motion detection: read system animator duration scale.
    // A scale of 0f means the user has disabled animations.
    // -------------------------------------------------------------------------
    val context = LocalContext.current
    val reducedMotion = remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }

    // -------------------------------------------------------------------------
    // Mount animation: start at 0, animate to target progress.
    // We use a separate mutable state so the animation fires once on first
    // composition rather than starting mid-value.
    // -------------------------------------------------------------------------
    var animTarget by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(progress) { animTarget = progress.coerceIn(0f, 1f) }

    val animatedProgress by animateFloatAsState(
        targetValue = animTarget,
        animationSpec = if (reducedMotion) {
            snap()
        } else {
            tween(durationMillis = duration.slow, easing = FastOutSlowInEasing)
        },
        label = "PhaseRingProgress",
    )

    // -------------------------------------------------------------------------
    // Canvas geometry — computed at render time via BoxWithConstraints or
    // using the size param directly.
    // -------------------------------------------------------------------------
    val trackColor  = tokens.fill
    val arcColor    = tokens.border
    val strokeWidth = size * 0.075f   // ~7.5% of diameter — visually balanced

    val description = "Day $day. ${phase.displayName}. $remainingDays days remaining."

    Column(
        modifier = modifier.wrapContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MoonTheme.spacing.xs),
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .semantics { contentDescription = description },
            contentAlignment = Alignment.Center,
        ) {
            // Canvas: track + animated arc
            Canvas(modifier = Modifier.size(size)) {
                val strokePx = strokeWidth.toPx()
                val inset    = strokePx / 2f
                val arcSize  = androidx.compose.ui.geometry.Size(
                    this.size.width - strokePx,
                    this.size.height - strokePx,
                )
                val topLeft  = androidx.compose.ui.geometry.Offset(inset, inset)

                // Full 360° track (muted fill colour)
                drawArc(
                    color      = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter  = false,
                    topLeft    = topLeft,
                    size       = arcSize,
                    style      = Stroke(width = strokePx, cap = StrokeCap.Round),
                )

                // Progress arc (phase border colour, clockwise from 12 o'clock)
                if (animatedProgress > 0f) {
                    drawArc(
                        color      = arcColor,
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter  = false,
                        topLeft    = topLeft,
                        size       = arcSize,
                        style      = Stroke(width = strokePx, cap = StrokeCap.Round),
                    )
                }
            }

            // Day number — centred inside the ring
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "$day",
                    style = MoonTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize   = (size.value * 0.22f).sp,
                        lineHeight = (size.value * 0.22f).sp,
                    ),
                    color = tokens.text,
                )
                Text(
                    text = "Day",
                    style = MoonTheme.typography.labelMedium.copy(
                        fontSize = (size.value * 0.07f).sp,
                    ),
                    color = tokens.onFill,
                )
            }
        }

        // Phase name — displayed below the ring
        Text(
            text = phase.displayName,
            style = MoonTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = tokens.text,
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "PhaseRing — all phases", showBackground = true)
@Composable
private fun PhaseRingAllPhasesPreview() {
    MoonSyncTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PhaseRing(
                phase = CyclePhase.MENSTRUAL,
                progress = 0.6f,
                day = 3,
                size = 90.dp,
                remainingDays = 2,
            )
            PhaseRing(
                phase = CyclePhase.FOLLICULAR,
                progress = 0.4f,
                day = 9,
                size = 90.dp,
                remainingDays = 5,
            )
            PhaseRing(
                phase = CyclePhase.OVULATION,
                progress = 0.85f,
                day = 15,
                size = 90.dp,
                remainingDays = 1,
            )
            PhaseRing(
                phase = CyclePhase.LUTEAL,
                progress = 0.3f,
                day = 20,
                size = 90.dp,
                remainingDays = 8,
            )
        }
    }
}

@Preview(name = "PhaseRing — large hero size", showBackground = true)
@Composable
private fun PhaseRingHeroPreview() {
    MoonSyncTheme {
        Box(
            modifier = Modifier.padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            PhaseRing(
                phase = CyclePhase.FOLLICULAR,
                progress = 0.55f,
                day = 14,
                size = 240.dp,
                remainingDays = 4,
            )
        }
    }
}

@Preview(name = "PhaseRing — edge cases", showBackground = true)
@Composable
private fun PhaseRingEdgeCasesPreview() {
    MoonSyncTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Empty ring
            PhaseRing(
                phase = CyclePhase.MENSTRUAL,
                progress = 0f,
                day = 1,
                size = 100.dp,
            )
            // Full ring
            PhaseRing(
                phase = CyclePhase.LUTEAL,
                progress = 1f,
                day = 28,
                size = 100.dp,
            )
        }
    }
}
