package com.example.moonsyncapp.widget.theme

import androidx.compose.ui.graphics.Color
import com.example.moonsyncapp.data.model.CyclePhase

/**
 * Widget theme colors adapted from MoonSync's design system.
 *
 * Glance widgets use simple Color values resolved at render time.
 * Light/dark mode is handled by providing both variants
 * and selecting at render time based on system theme.
 */
object WidgetTheme {

    // ==================== SURFACE COLORS ====================

    object Light {
        val Background = Color(0xFFFFFFFF)
        val Surface = Color(0xFFF8F8F8)
        val TextPrimary = Color(0xFF1A1A1A)
        val TextSecondary = Color(0xFF666666)
        val Divider = Color(0xFFE0E0E0)
        val Primary = Color(0xFF6B4C8A)
        val Secondary = Color(0xFFE91E63)
    }

    object Dark {
        val Background = Color(0xFF1E1E1E)
        val Surface = Color(0xFF2A2A2A)
        val TextPrimary = Color(0xFFF5F5F5)
        val TextSecondary = Color(0xFFAAAAAA)
        val Divider = Color(0xFF3D3D3D)
        val Primary = Color(0xFF9C7AC1)
        val Secondary = Color(0xFFE91E63)
    }

    // Text on phase-colored backgrounds (always dark - pastels are light)
    val TextOnPhaseBackground = Color(0xFF2D2D2D)
    val TextOnPhaseBackgroundSecondary = Color(0xFF555555)

    // Button
    val ButtonText = Color.White

    // ==================== PHASE COLORS ====================

    fun phaseBackgroundColor(phase: CyclePhase, isDarkMode: Boolean): Color {
        return when (phase) {
            CyclePhase.MENSTRUAL -> if (isDarkMode) Color(0xFF3D2832) else Color(0xFFFCE4EC)
            CyclePhase.FOLLICULAR -> if (isDarkMode) Color(0xFF263328) else Color(0xFFE8F5E9)
            CyclePhase.OVULATION -> if (isDarkMode) Color(0xFF3D3326) else Color(0xFFFFF3E0)
            CyclePhase.LUTEAL -> if (isDarkMode) Color(0xFF2D2633) else Color(0xFFF3E5F5)
        }
    }

    fun phaseAccentColor(phase: CyclePhase, isDarkMode: Boolean): Color {
        return when (phase) {
            CyclePhase.MENSTRUAL -> if (isDarkMode) Color(0xFFEC407A) else Color(0xFFEC407A)
            CyclePhase.FOLLICULAR -> if (isDarkMode) Color(0xFF81C784) else Color(0xFF66BB6A)
            CyclePhase.OVULATION -> if (isDarkMode) Color(0xFFFFB74D) else Color(0xFFFFA726)
            CyclePhase.LUTEAL -> if (isDarkMode) Color(0xFFBA68C8) else Color(0xFFAB47BC)
        }
    }

    fun phaseProgressColor(phase: CyclePhase): Color {
        return when (phase) {
            CyclePhase.MENSTRUAL -> Color(0xFFF48FB1)
            CyclePhase.FOLLICULAR -> Color(0xFFA5D6A7)
            CyclePhase.OVULATION -> Color(0xFFFFCC80)
            CyclePhase.LUTEAL -> Color(0xFFCE93D8)
        }
    }

    fun phaseProgressTrackColor(phase: CyclePhase, isDarkMode: Boolean): Color {
        return when (phase) {
            CyclePhase.MENSTRUAL -> if (isDarkMode) Color(0xFF3D2832) else Color(0xFFFCE4EC)
            CyclePhase.FOLLICULAR -> if (isDarkMode) Color(0xFF263328) else Color(0xFFE8F5E9)
            CyclePhase.OVULATION -> if (isDarkMode) Color(0xFF3D3326) else Color(0xFFFFF3E0)
            CyclePhase.LUTEAL -> if (isDarkMode) Color(0xFF2D2633) else Color(0xFFF3E5F5)
        }
    }

    // ==================== STALE STATE ====================

    fun staleBackground(isDarkMode: Boolean): Color {
        return if (isDarkMode) Dark.Surface else Light.Surface
    }

    fun staleAccent(isDarkMode: Boolean): Color {
        return if (isDarkMode) Dark.Primary else Light.Primary
    }

    fun staleText(isDarkMode: Boolean): Color {
        return if (isDarkMode) Dark.TextSecondary else Light.TextSecondary
    }
}

/**
 * Resolved color scheme for a specific phase and theme mode.
 * Created at render time when dark mode state is known.
 */
data class PhaseColorScheme(
    val background: Color,
    val accent: Color,
    val progress: Color,
    val progressTrack: Color,
    val textPrimary: Color,
    val textSecondary: Color
) {
    companion object {
        fun forPhase(phase: CyclePhase, isDarkMode: Boolean): PhaseColorScheme {
            return PhaseColorScheme(
                background = WidgetTheme.phaseBackgroundColor(phase, isDarkMode),
                accent = WidgetTheme.phaseAccentColor(phase, isDarkMode),
                progress = WidgetTheme.phaseProgressColor(phase),
                progressTrack = WidgetTheme.phaseProgressTrackColor(phase, isDarkMode),
                textPrimary = WidgetTheme.TextOnPhaseBackground,
                textSecondary = WidgetTheme.TextOnPhaseBackgroundSecondary
            )
        }

        fun stale(isDarkMode: Boolean): PhaseColorScheme {
            return PhaseColorScheme(
                background = WidgetTheme.staleBackground(isDarkMode),
                accent = WidgetTheme.staleAccent(isDarkMode),
                progress = if (isDarkMode) WidgetTheme.Dark.Primary else WidgetTheme.Light.Primary,
                progressTrack = if (isDarkMode) WidgetTheme.Dark.Divider else WidgetTheme.Light.Divider,
                textPrimary = if (isDarkMode) WidgetTheme.Dark.TextPrimary else WidgetTheme.Light.TextPrimary,
                textSecondary = if (isDarkMode) WidgetTheme.Dark.TextSecondary else WidgetTheme.Light.TextSecondary
            )
        }
    }
}

/**
 * Widget dimension constants.
 */
object WidgetDimensions {
    const val TEXT_SIZE_HERO = 36
    const val TEXT_SIZE_LARGE = 24
    const val TEXT_SIZE_MEDIUM = 16
    const val TEXT_SIZE_BODY = 14
    const val TEXT_SIZE_SMALL = 12
    const val TEXT_SIZE_TINY = 10

    const val SPACING_XS = 4
    const val SPACING_SM = 8
    const val SPACING_MD = 12
    const val SPACING_LG = 16
    const val SPACING_XL = 20
    const val SPACING_XXL = 24

    const val RADIUS_SM = 8
    const val RADIUS_MD = 12
    const val RADIUS_LG = 16
    const val RADIUS_XL = 20

    const val PROGRESS_HEIGHT = 6
    const val PROGRESS_RADIUS = 3

    const val WIDGET_PADDING_SMALL = 12
    const val WIDGET_PADDING_MEDIUM = 16
    const val WIDGET_PADDING_LARGE = 16
    const val ACCENT_BAR_WIDTH = 4
}