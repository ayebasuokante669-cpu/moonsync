package com.example.moonsyncapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moonsyncapp.data.model.CyclePhase
import com.example.moonsyncapp.ui.theme.MoonSyncTheme
import com.example.moonsyncapp.ui.theme.MoonTheme
import com.example.moonsyncapp.ui.theme.tokens.toColorTokens

// ---------------------------------------------------------------------------
// Enum
// ---------------------------------------------------------------------------

enum class MoonBadgeType {
    /** Phase name badge — tinted with the phase palette. Needs [phase] argument. */
    Phase,
    /** Streak count badge — flame emoji + count, warm amber tint. */
    Streak,
    /** Medical-professional verified badge — green check. */
    Verified,
    /** Wisdom/gamification level badge — neutral surface with label. */
    Level,
}

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

/**
 * MoonSync informational badge — non-interactive label pill.
 *
 * @param type    Badge semantic type; drives colour palette.
 * @param value   Text displayed inside the badge (e.g. "7", "Blooming", "Verified").
 * @param phase   Required when [type] == [MoonBadgeType.Phase].
 * @param modifier Modifier for the outer container.
 */
@Composable
fun MoonBadge(
    type: MoonBadgeType,
    value: String,
    modifier: Modifier = Modifier,
    phase: CyclePhase? = null,
) {
    val radius  = MoonTheme.radius
    val spacing = MoonTheme.spacing
    val colors  = MoonTheme.colors
    val shape   = RoundedCornerShape(radius.full)

    // Resolve palette
    val (containerColor, contentColor, borderColor) = when (type) {
        MoonBadgeType.Phase -> {
            val pc = phase?.toColorTokens()
            Triple(
                pc?.fill ?: colors.surfaceVariant,
                pc?.text ?: colors.onSurfaceVariant,
                pc?.border ?: colors.outline,
            )
        }
        MoonBadgeType.Streak -> Triple(
            Color(0xFFFFF3E0),   // Warm amber container
            Color(0xFFE65100),   // Deep amber text
            Color(0xFFFFA726),   // Amber border
        )
        MoonBadgeType.Verified -> Triple(
            Color(0xFFE8F5E9),   // Sage container
            Color(0xFF388E3C),   // Sage text
            Color(0xFF66BB6A),   // Sage border
        )
        MoonBadgeType.Level -> Triple(
            colors.surfaceVariant,
            colors.onSurfaceVariant,
            colors.outlineVariant,
        )
    }

    // Emoji prefix per type
    val prefix = when (type) {
        MoonBadgeType.Streak   -> "🔥 "
        MoonBadgeType.Verified -> "✓ "
        else                   -> ""
    }

    Row(
        modifier = modifier
            .background(color = containerColor, shape = shape)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .padding(horizontal = spacing.sm, vertical = spacing.xs)
            .semantics {
                contentDescription = when (type) {
                    MoonBadgeType.Phase    -> "${phase?.displayName} phase"
                    MoonBadgeType.Streak   -> "Streak: $value days"
                    MoonBadgeType.Verified -> "Verified professional"
                    MoonBadgeType.Level    -> "Level: $value"
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (type == MoonBadgeType.Verified) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(12.dp),
            )
            Spacer(Modifier.width(spacing.xs))
        }
        Text(
            text = "$prefix$value",
            style = MoonTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = contentColor,
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "MoonBadge — all types", showBackground = true)
@Composable
private fun MoonBadgePreview() {
    MoonSyncTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Phase badges
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CyclePhase.entries.forEach { phase ->
                    MoonBadge(type = MoonBadgeType.Phase, value = phase.displayName, phase = phase)
                }
            }
            // Streak
            MoonBadge(type = MoonBadgeType.Streak, value = "14 days")
            // Verified
            MoonBadge(type = MoonBadgeType.Verified, value = "Verified")
            // Level
            listOf("Seedling", "Sprout", "Blooming", "Wise Tree").forEach { level ->
                MoonBadge(type = MoonBadgeType.Level, value = level)
            }
        }
    }
}
