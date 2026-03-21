//package com.example.moonsyncapp.ui.theme
//
//import android.app.Activity
//import android.os.Build
//import androidx.compose.foundation.isSystemInDarkTheme
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.darkColorScheme
//import androidx.compose.material3.lightColorScheme
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.SideEffect
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.toArgb
//import androidx.compose.ui.platform.LocalView
//import androidx.core.view.WindowCompat
//
//private val LightColorScheme = lightColorScheme(
//    primary = MoonSyncPrimary,
//    onPrimary = Color.White,
//    secondary = MoonSyncSecondary,
//    onSecondary = Color.White,
//    tertiary = MoonSyncTertiary,
//    background = BackgroundLight,
//    onBackground = OnBackgroundLight,
//    surface = SurfaceLight,
//    onSurface = OnSurfaceLight,
//    surfaceVariant = ImageContainerLight,
//    onSurfaceVariant = OnSurfaceVariantLight,
//    outline = ProgressInactiveLight
//)
//
//private val DarkColorScheme = darkColorScheme(
//    primary = MoonSyncTertiary,
//    onPrimary = Color.Black,
//    secondary = MoonSyncSecondary,
//    onSecondary = Color.Black,
//    tertiary = MoonSyncPrimary,
//    background = BackgroundDark,
//    onBackground = OnBackgroundDark,
//    surface = SurfaceDark,
//    onSurface = OnSurfaceDark,
//    surfaceVariant = ImageContainerDark,
//    onSurfaceVariant = OnSurfaceVariantDark,
//    outline = ProgressInactiveDark
//)
//
//// Custom colors accessible via LocalCustomColors
//data class CustomColors(
//    val progressActive: Color,
//    val progressInactive: Color,
//    val imageContainer: Color
//)
//
//val LightCustomColors = CustomColors(
//    progressActive = ProgressActiveLight,
//    progressInactive = ProgressInactiveLight,
//    imageContainer = ImageContainerLight
//)
//
//val DarkCustomColors = CustomColors(
//    progressActive = ProgressActiveDark,
//    progressInactive = ProgressInactiveDark,
//    imageContainer = ImageContainerDark
//)
//
//@Composable
//fun MoonSyncTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
//    content: @Composable () -> Unit
//) {
//    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
//
//    val view = LocalView.current
//    if (!view.isInEditMode) {
//        SideEffect {
//            val window = (view.context as Activity).window
//            window.statusBarColor = colorScheme.background.toArgb()
//            window.navigationBarColor = colorScheme.background.toArgb()
//            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
//            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
//        }
//    }
//
//    MaterialTheme(
//        colorScheme = colorScheme,
//        typography = Typography,
//        content = content
//    )
//}
//
//// Helper to get custom colors
//@Composable
//fun customColors(): CustomColors {
//    return if (isSystemInDarkTheme()) DarkCustomColors else LightCustomColors
//}

package com.example.moonsyncapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ============================================
// Resolved dark theme indicator
// Fixes: customColors() ignoring manual toggle
// ============================================
val LocalIsDarkTheme = compositionLocalOf { false }

private val LightColorScheme = lightColorScheme(
    primary = MoonSyncPrimary,
    onPrimary = Color.White,
    secondary = MoonSyncSecondary,
    onSecondary = Color.White,
    tertiary = MoonSyncTertiary,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = ImageContainerLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = ProgressInactiveLight
)

private val DarkColorScheme = darkColorScheme(
    primary = MoonSyncTertiary,
    onPrimary = Color.Black,
    secondary = MoonSyncSecondary,
    onSecondary = Color.Black,
    tertiary = MoonSyncPrimary,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = ImageContainerDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = ProgressInactiveDark
)

// ============================================
// Custom colors (theme-aware)
// ============================================
data class CustomColors(
    val progressActive: Color,
    val progressInactive: Color,
    val imageContainer: Color,
    // Skeleton shimmer colors
    val skeletonBase: Color,
    val skeletonHighlight: Color,
    // Hexagon text colors
    val hexagonTextPrimary: Color,
    val hexagonTextSecondary: Color,
    // Card colors
    val adviceCardBg: Color,
    val adviceCardGlass: Color,
    val softPink: Color,
    // Community header gradient
    val communityGradientStart: Color,
    val communityGradientEnd: Color,
    // Community text on pastel
    val textOnPastel: Color,
    val secondaryTextOnPastel: Color
)

val LightCustomColors = CustomColors(
    progressActive = ProgressActiveLight,
    progressInactive = ProgressInactiveLight,
    imageContainer = ImageContainerLight,
    skeletonBase = Color(0xFFE8DDE6),          // Warm shimmer base
    skeletonHighlight = Color(0xFFF5EEF3),     // Warm shimmer highlight
    hexagonTextPrimary = Color(0xFF2D2D2D),    // Dark text on pastel fills
    hexagonTextSecondary = Color(0xFF666666),   // Secondary text on pastel fills
    adviceCardBg = Color(0xFFF8F4FC),          // Light purple card bg
    adviceCardGlass = Color(0xCCFFFFFF),       // Glass overlay
    softPink = SoftPink,
    communityGradientStart = Color(0xFF7B5EA7),
    communityGradientEnd = Color(0xFF9575CD),
    textOnPastel = Color(0xFF2D2D2D),
    secondaryTextOnPastel = Color(0xFF666666)
)

val DarkCustomColors = CustomColors(
    progressActive = ProgressActiveDark,
    progressInactive = ProgressInactiveDark,
    imageContainer = ImageContainerDark,
    skeletonBase = Color(0xFF2A2533),          // Dark warm shimmer base
    skeletonHighlight = Color(0xFF342D3A),     // Dark warm shimmer highlight
    hexagonTextPrimary = Color(0xFFF5F5F5),    // Light text on dark fills
    hexagonTextSecondary = Color(0xFFBBBBBB),  // Secondary text on dark fills
    adviceCardBg = Color(0xFF2A2533),          // Dark purple card bg
    adviceCardGlass = Color(0x33FFFFFF),       // Subtle glass on dark
    softPink = Color(0xFF3D2A35),             // Muted pink in dark mode
    communityGradientStart = Color(0xFF5A3D7A),
    communityGradientEnd = Color(0xFF7B5EA7),
    textOnPastel = Color(0xFFF5F5F5),
    secondaryTextOnPastel = Color(0xFFBBBBBB)
)

@Composable
fun MoonSyncTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// ============================================
// Helper to get custom colors — now respects
// manual theme toggle, not just system theme
// ============================================
@Composable
fun customColors(): CustomColors {
    return if (LocalIsDarkTheme.current) DarkCustomColors else LightCustomColors
}