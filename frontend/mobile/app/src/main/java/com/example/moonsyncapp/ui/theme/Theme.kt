package com.example.moonsyncapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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

// Custom colors accessible via LocalCustomColors
data class CustomColors(
    val progressActive: Color,
    val progressInactive: Color,
    val imageContainer: Color
)

val LightCustomColors = CustomColors(
    progressActive = ProgressActiveLight,
    progressInactive = ProgressInactiveLight,
    imageContainer = ImageContainerLight
)

val DarkCustomColors = CustomColors(
    progressActive = ProgressActiveDark,
    progressInactive = ProgressInactiveDark,
    imageContainer = ImageContainerDark
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Helper to get custom colors
@Composable
fun customColors(): CustomColors {
    return if (isSystemInDarkTheme()) DarkCustomColors else LightCustomColors
}