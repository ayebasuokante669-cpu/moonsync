package com.example.moonsyncapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * SafeScreen - Handles all device cutouts and system bars
 *
 * Use this as the root container for ALL screens to ensure:
 * - Status bar padding
 * - Navigation bar padding
 * - Notch/punch-hole camera avoidance
 * - Dynamic Island support
 * - Curved edge handling
 */
@Composable
fun SafeScreen(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
        content = content
    )
}

/**
 * SafeScreenWithoutBottomPadding - For screens with custom bottom navigation
 *
 * Use when you have a floating bottom nav that handles its own padding
 */
@Composable
fun SafeScreenWithoutBottomPadding(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
        content = content
    )
}