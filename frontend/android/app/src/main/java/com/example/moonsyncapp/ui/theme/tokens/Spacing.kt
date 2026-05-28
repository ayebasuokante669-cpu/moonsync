package com.example.moonsyncapp.ui.theme.tokens

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * MoonSync spacing scale — 8 dp base grid.
 *
 * xs  =  4 dp   (tight insets, icon padding)
 * sm  =  8 dp   (chip inner padding, icon gaps)
 * md  = 16 dp   (standard content padding)
 * lg  = 24 dp   (section gaps)
 * xl  = 32 dp   (card padding, between-section whitespace)
 * xxl = 48 dp   (hero sections, screen margins on large displays)
 */
data class SpacingTokens(
    val xs: Dp  =  4.dp,
    val sm: Dp  =  8.dp,
    val md: Dp  = 16.dp,
    val lg: Dp  = 24.dp,
    val xl: Dp  = 32.dp,
    val xxl: Dp = 48.dp,
)

val LocalMoonSpacing = staticCompositionLocalOf { SpacingTokens() }
