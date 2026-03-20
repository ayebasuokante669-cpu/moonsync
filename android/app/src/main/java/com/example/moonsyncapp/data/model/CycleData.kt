package com.example.moonsyncapp.data.model

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class CycleData(
    val userName: String,
    val currentPhase: CyclePhase,
    val cycleDay: Int,
    val cycleLength: Int,
    val daysUntilNextPeriod: Int,
    val daysUntilOvulation: Int,
    val periodDaysRemaining: Int?,
    val phaseProgress: Float,
    val phaseDaysRemaining: Int,
    val phaseTotalDays: Int,
    val nextPeriodDate: LocalDate,
    val ovulationDate: LocalDate
) {
    fun getNextPeriodFormatted(): String {
        return nextPeriodDate.format(DateTimeFormatter.ofPattern("MMM d"))
    }

    fun getOvulationFormatted(): String {
        return ovulationDate.format(DateTimeFormatter.ofPattern("MMM d"))
    }
}

enum class CyclePhase(
    val displayName: String,
    val description: String
) {
    MENSTRUAL(
        displayName = "Menstrual Phase",
        description = "Rest and nurture yourself"
    ),
    FOLLICULAR(
        displayName = "Follicular Phase",
        description = "Energy is rising"
    ),
    OVULATION(
        displayName = "Ovulation Phase",
        description = "Peak fertility window"
    ),
    LUTEAL(
        displayName = "Luteal Phase",
        description = "Preparing for next cycle"
    )
}

// Phase color theming
object PhaseColors {
    // Menstrual - Warm Rose
    val MenstrualFill = Color(0xFFFCE4EC)
    val MenstrualBorder = Color(0xFFEC407A)
    val MenstrualProgress = Color(0xFFF48FB1)

    // Follicular - Fresh Sage
    val FollicularFill = Color(0xFFE8F5E9)
    val FollicularBorder = Color(0xFF66BB6A)
    val FollicularProgress = Color(0xFFA5D6A7)

    // Ovulation - Warm Amber
    val OvulationFill = Color(0xFFFFF3E0)
    val OvulationBorder = Color(0xFFFFA726)
    val OvulationProgress = Color(0xFFFFCC80)

    // Luteal - Calm Lavender
    val LutealFill = Color(0xFFF3E5F5)
    val LutealBorder = Color(0xFFAB47BC)
    val LutealProgress = Color(0xFFCE93D8)

    // ADD: Background tints (very subtle, ~6% opacity)
    val MenstrualBgTint = Color(0x0FEC407A)
    val FollicularBgTint = Color(0x0F66BB6A)
    val OvulationBgTint = Color(0x0FFFA726)
    val LutealBgTint = Color(0x0FAB47BC)

    fun getFillColor(phase: CyclePhase): Color = when (phase) {
        CyclePhase.MENSTRUAL -> MenstrualFill
        CyclePhase.FOLLICULAR -> FollicularFill
        CyclePhase.OVULATION -> OvulationFill
        CyclePhase.LUTEAL -> LutealFill
    }

    fun getBorderColor(phase: CyclePhase): Color = when (phase) {
        CyclePhase.MENSTRUAL -> MenstrualBorder
        CyclePhase.FOLLICULAR -> FollicularBorder
        CyclePhase.OVULATION -> OvulationBorder
        CyclePhase.LUTEAL -> LutealBorder
    }

    fun getProgressColor(phase: CyclePhase): Color = when (phase) {
        CyclePhase.MENSTRUAL -> MenstrualProgress
        CyclePhase.FOLLICULAR -> FollicularProgress
        CyclePhase.OVULATION -> OvulationProgress
        CyclePhase.LUTEAL -> LutealProgress
    }

    // ADD: Background tint getter
    fun getBackgroundTint(phase: CyclePhase): Color = when (phase) {
        CyclePhase.MENSTRUAL -> MenstrualBgTint
        CyclePhase.FOLLICULAR -> FollicularBgTint
        CyclePhase.OVULATION -> OvulationBgTint
        CyclePhase.LUTEAL -> LutealBgTint
    }
}