package com.example.moonsyncapp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import com.example.moonsyncapp.ui.theme.tokens.DurationTokens
import com.example.moonsyncapp.ui.theme.tokens.ElevationTokens
import com.example.moonsyncapp.ui.theme.tokens.LocalMoonDuration
import com.example.moonsyncapp.ui.theme.tokens.LocalMoonElevation
import com.example.moonsyncapp.ui.theme.tokens.LocalMoonRadius
import com.example.moonsyncapp.ui.theme.tokens.LocalMoonSpacing
import com.example.moonsyncapp.ui.theme.tokens.RadiusTokens
import com.example.moonsyncapp.ui.theme.tokens.SpacingTokens

/**
 * Single access point for the MoonSync design token system.
 *
 * All composables read tokens through this object — never through raw
 * CompositionLocals or hardcoded literal values.
 *
 * Usage:
 *   val spacing  = MoonTheme.spacing          // SpacingTokens
 *   val radius   = MoonTheme.radius           // RadiusTokens
 *   val elev     = MoonTheme.elevation        // ElevationTokens
 *   val duration = MoonTheme.duration         // DurationTokens
 *   val colors   = MoonTheme.colors           // Material 3 ColorScheme
 *   val type     = MoonTheme.typography       // Material 3 Typography
 *
 *   // Phase-specific colours:
 *   val phaseColors = CyclePhase.MENSTRUAL.toColorTokens()
 */
object MoonTheme {

    /** 8 dp-grid spacing scale. */
    val spacing: SpacingTokens
        @Composable get() = LocalMoonSpacing.current

    /** Corner-radius scale. */
    val radius: RadiusTokens
        @Composable get() = LocalMoonRadius.current

    /** Shadow / tonal-elevation scale. */
    val elevation: ElevationTokens
        @Composable get() = LocalMoonElevation.current

    /** Animation duration scale (ms). */
    val duration: DurationTokens
        @Composable get() = LocalMoonDuration.current

    // -------------------------------------------------------------------------
    // Material 3 delegates — keeps all theme access through one import.
    // -------------------------------------------------------------------------

    /** Material 3 colour scheme (light or dark based on current theme). */
    val colors: ColorScheme
        @Composable get() = MaterialTheme.colorScheme

    /** Material 3 type scale. */
    val typography: Typography
        @Composable get() = MaterialTheme.typography
}
