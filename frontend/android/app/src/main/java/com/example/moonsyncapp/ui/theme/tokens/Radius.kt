package com.example.moonsyncapp.ui.theme.tokens

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * MoonSync corner-radius scale.
 *
 * sm   =    8 dp  (chips, small surfaces, input fields)
 * md   =   12 dp  (cards, bottom-sheet header)
 * lg   =   16 dp  (large cards, dialogs)
 * xl   =   24 dp  (hero cards, phase containers)
 * full = 9999 dp  (pills, badges, fully rounded buttons)
 */
data class RadiusTokens(
    val sm:   Dp =    8.dp,
    val md:   Dp =   12.dp,
    val lg:   Dp =   16.dp,
    val xl:   Dp =   24.dp,
    val full: Dp = 9999.dp,
)

val LocalMoonRadius = staticCompositionLocalOf { RadiusTokens() }
