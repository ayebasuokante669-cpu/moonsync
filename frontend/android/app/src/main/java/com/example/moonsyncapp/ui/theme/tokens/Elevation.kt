package com.example.moonsyncapp.ui.theme.tokens

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * MoonSync elevation / shadow scale — maps to Material 3 tonal elevation.
 *
 * none    =  0 dp  (flat surfaces, backgrounds)
 * low     =  1 dp  (subtle lift, list items)
 * medium  =  4 dp  (cards, chips)
 * high    =  8 dp  (FAB, raised panels)
 * overlay = 12 dp  (bottom sheets, modals, dialogs)
 */
data class ElevationTokens(
    val none:    Dp =  0.dp,
    val low:     Dp =  1.dp,
    val medium:  Dp =  4.dp,
    val high:    Dp =  8.dp,
    val overlay: Dp = 12.dp,
)

val LocalMoonElevation = staticCompositionLocalOf { ElevationTokens() }
