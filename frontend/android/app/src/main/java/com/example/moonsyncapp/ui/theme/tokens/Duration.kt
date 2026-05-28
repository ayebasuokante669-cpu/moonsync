package com.example.moonsyncapp.ui.theme.tokens

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * MoonSync animation duration scale (milliseconds).
 *
 * fast     =  150 ms  (micro-interactions: chip select, button press)
 * standard =  300 ms  (screen transitions, card expand/collapse)
 * slow     =  600 ms  (hero entrances: PhaseRing arc draw, staggered reveals)
 * phase    = 1500 ms  (background tint crossfade on phase change)
 *
 * NOTE: All animation consumers should also check the system animator scale.
 *       Compose's animateFloatAsState / tween respects the global animator scale,
 *       so these values work automatically with "Disable animations" accessibility setting.
 */
data class DurationTokens(
    val fast:     Int =  150,
    val standard: Int =  300,
    val slow:     Int =  600,
    val phase:    Int = 1500,
)

val LocalMoonDuration = staticCompositionLocalOf { DurationTokens() }
