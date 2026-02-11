package com.example.moonsyncapp.widget

import com.example.moonsyncapp.data.model.CyclePhase
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Represents the complete state needed to render MoonSync widgets.
 *
 * This is a UI-ready data class — all calculations are done before populating this.
 * The widget simply reads and displays; no computation happens in the widget itself.
 *
 * Designed for:
 * - Serialization to DataStore (widget runs in separate process)
 * - All three widget sizes (small, medium, large)
 * - Privacy-aware display (detailed vs. minimal mode)
 * - Stale data handling (prompts user to update)
 *
 * Privacy model:
 * - Default (showDetailedInfo = false): Emoji only, abstract countdown labels
 * - Detailed (showDetailedInfo = true): Full phase names, explicit countdowns
 *
 * Data flow:
 * SettingsManager → CycleCalculator → WidgetState → DataStore → Widget
 */
@Serializable
data class WidgetState(
    // ==================== CORE CYCLE DATA ====================

    /**
     * Current day of the menstrual cycle (1-based).
     * Primary display element across all widget sizes.
     * Value of 0 indicates stale/invalid data.
     */
    val cycleDay: Int,

    /**
     * Total length of user's cycle in days.
     * Used for "Day X of Y" display on medium/large widgets.
     */
    val cycleLength: Int,

    /**
     * Current cycle phase as string for serialization.
     * Use [phase] property to get the enum value.
     *
     * Values: "MENSTRUAL", "FOLLICULAR", "OVULATION", "LUTEAL"
     */
    val phaseKey: String,

    /**
     * Progress through current phase (0.0 to 1.0).
     * Used for progress bar on large widget.
     */
    val phaseProgress: Float,

    // ==================== COUNTDOWN DATA ====================

    /**
     * Days until next period is expected.
     * 0 means period is today/starting.
     */
    val daysUntilPeriod: Int,

    /**
     * Days until ovulation is expected.
     * 0 means currently in ovulation window.
     */
    val daysUntilOvulation: Int,

    /**
     * Next period date as ISO string (yyyy-MM-dd).
     * Use [nextPeriodDate] property to get LocalDate.
     */
    val nextPeriodDateString: String,

    /**
     * Ovulation date as ISO string (yyyy-MM-dd).
     * Use [ovulationDate] property to get LocalDate.
     */
    val ovulationDateString: String,

    // ==================== DISPLAY METADATA ====================

    /**
     * Whether cycle data is fresh enough for reliable predictions.
     * When false, widget shows "Update needed" prompt instead of predictions.
     *
     * Freshness is determined by [CycleCalculator.isDataFresh].
     */
    val isDataFresh: Boolean,

    /**
     * User preference: show detailed phase info or privacy-minimal mode.
     *
     * When false (default):
     * - Phase shown as emoji only (no text label)
     * - Countdown uses abstract terms ("cycle reset" not "period")
     *
     * When true:
     * - Full phase names shown ("Follicular")
     * - Explicit countdown terms ("until period", "until ovulation")
     */
    val showDetailedInfo: Boolean = false,

    /**
     * Timestamp when this state was last calculated (epoch millis).
     * Used for debugging and potential "last updated" display.
     */
    val lastUpdatedMillis: Long = System.currentTimeMillis()
) {

    // ==================== COMPUTED PROPERTIES: PHASE ====================

    /**
     * Current cycle phase as enum.
     * Converts [phaseKey] string back to [CyclePhase].
     */
    val phase: CyclePhase
        get() = try {
            CyclePhase.valueOf(phaseKey)
        } catch (e: IllegalArgumentException) {
            CyclePhase.FOLLICULAR // Safe fallback
        }

    /**
     * Phase emoji for visual display.
     * Privacy-safe — emoji is abstract, not explicit.
     */
    val phaseEmoji: String
        get() = when (phase) {
            CyclePhase.MENSTRUAL -> "🌸"
            CyclePhase.FOLLICULAR -> "🌱"
            CyclePhase.OVULATION -> "✨"
            CyclePhase.LUTEAL -> "🌙"
        }

    /**
     * Full phase name for detailed mode.
     */
    val phaseLabel: String
        get() = when (phase) {
            CyclePhase.MENSTRUAL -> "Menstrual"
            CyclePhase.FOLLICULAR -> "Follicular"
            CyclePhase.OVULATION -> "Ovulation"
            CyclePhase.LUTEAL -> "Luteal"
        }

    /**
     * Display-ready phase label respecting privacy setting.
     *
     * Privacy mode: emoji only (e.g., "🌱")
     * Detailed mode: emoji + name (e.g., "🌱 Follicular")
     */
    val displayPhaseLabel: String
        get() = if (showDetailedInfo) {
            "$phaseEmoji $phaseLabel"
        } else {
            phaseEmoji
        }

    /**
     * Phase advice text for large widget.
     * Only shown when [showDetailedInfo] is true.
     */
    val phaseAdvice: String
        get() = when (phase) {
            CyclePhase.MENSTRUAL -> "Rest and nurture yourself"
            CyclePhase.FOLLICULAR -> "Energy is rising"
            CyclePhase.OVULATION -> "Peak energy window"
            CyclePhase.LUTEAL -> "Prepare for next cycle"
        }

    // ==================== COMPUTED PROPERTIES: DATES ====================

    /**
     * Next period date as LocalDate.
     * Parses [nextPeriodDateString] from ISO format.
     */
    val nextPeriodDate: LocalDate
        get() = try {
            LocalDate.parse(nextPeriodDateString, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            LocalDate.now().plusDays(14) // Safe fallback
        }

    /**
     * Ovulation date as LocalDate.
     * Parses [ovulationDateString] from ISO format.
     */
    val ovulationDate: LocalDate
        get() = try {
            LocalDate.parse(ovulationDateString, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            LocalDate.now().plusDays(7) // Safe fallback
        }

    // ==================== COMPUTED PROPERTIES: COUNTDOWN ====================

    /**
     * Determines which countdown to show as primary.
     *
     * Privacy mode: Always shows period countdown with abstract terms
     * Detailed mode: Shows whichever event is closer (ovulation or period)
     *
     * This respects privacy by avoiding "ovulation" terminology in private mode,
     * while providing contextually relevant information in detailed mode.
     */
    val primaryCountdown: CountdownInfo
        get() {
            // Privacy mode: always show period with abstract terminology
            if (!showDetailedInfo) {
                return CountdownInfo(
                    days = daysUntilPeriod,
                    label = "until cycle reset",
                    emoji = "🌸",
                    isPrivacyMode = true
                )
            }

            // Detailed mode: show whichever event is closer
            return if (daysUntilOvulation in 1 until daysUntilPeriod) {
                CountdownInfo(
                    days = daysUntilOvulation,
                    label = "until ovulation",
                    emoji = "✨",
                    isPrivacyMode = false
                )
            } else {
                CountdownInfo(
                    days = daysUntilPeriod,
                    label = "until period",
                    emoji = "🌸",
                    isPrivacyMode = false
                )
            }
        }

    // ==================== COMPUTED PROPERTIES: DISPLAY STRINGS ====================

    /**
     * Formatted "Day X of Y" string for medium/large widgets.
     */
    val cycleDayDisplay: String
        get() = "Day $cycleDay of $cycleLength"

    /**
     * Short cycle day display for small widget.
     */
    val cycleDayShort: String
        get() = "Day $cycleDay"

    /**
     * Hero cycle day number as string.
     * Just the number for large display.
     */
    val cycleDayNumber: String
        get() = cycleDay.toString()

    // ==================== COMPUTED PROPERTIES: ACCESSIBILITY ====================

    /**
     * Accessibility label for screen readers.
     * Privacy-aware — doesn't mention phase in privacy mode.
     */
    val accessibilityLabel: String
        get() = if (showDetailedInfo) {
            "Day $cycleDay of $cycleLength day cycle, $phaseLabel phase"
        } else {
            "Day $cycleDay of your cycle"
        }

    // ==================== COMPUTED PROPERTIES: STALE STATE ====================

    /**
     * Stale state prompt title.
     * Friendly, action-oriented language.
     */
    val stalePromptTitle: String
        get() = "Log your period"

    /**
     * Stale state prompt subtitle.
     * Explains the benefit of updating.
     */
    val stalePromptSubtitle: String
        get() = "to update predictions"

    /**
     * Stale state emoji.
     * Maintains brand personality even in error state.
     */
    val stalePromptEmoji: String
        get() = "🌙"

    /**
     * Full stale prompt for larger widgets.
     */
    val stalePromptFull: String
        get() = "Log your period to update predictions"

    /**
     * Short stale prompt for small widget.
     */
    val stalePromptShort: String
        get() = "Update"

    // ==================== COMPANION: FACTORY METHODS ====================

    companion object {

        /**
         * Creates a default/empty state for initial widget render.
         * Used when no data is available yet or on first install.
         *
         * This state triggers the stale prompt UI.
         */
        fun empty(): WidgetState = WidgetState(
            cycleDay = 0,
            cycleLength = 28,
            phaseKey = CyclePhase.FOLLICULAR.name,
            phaseProgress = 0f,
            daysUntilPeriod = 0,
            daysUntilOvulation = 0,
            nextPeriodDateString = "",
            ovulationDateString = "",
            isDataFresh = false,
            showDetailedInfo = false
        )

        /**
         * Creates a stale data state that prompts user to update.
         * Used when [CycleCalculator.isDataFresh] returns false.
         *
         * Widget displays friendly prompt instead of unreliable predictions.
         */
        fun stale(): WidgetState = empty()

        /**
         * Validates if a state represents valid, displayable data.
         *
         * @param state The state to validate
         * @return true if state can be displayed normally, false if stale prompt needed
         */
        fun isDisplayable(state: WidgetState): Boolean {
            return state.isDataFresh && state.cycleDay > 0
        }
    }
}

/**
 * Helper data class for countdown display.
 * Bundles days, label, and emoji for consistent rendering.
 *
 * Supports both privacy and detailed modes with appropriate formatting.
 */
@Serializable
data class CountdownInfo(
    /**
     * Number of days until the event.
     */
    val days: Int,

    /**
     * Label describing the event.
     * Privacy mode: "until cycle reset"
     * Detailed mode: "until period" or "until ovulation"
     */
    val label: String,

    /**
     * Emoji representing the event.
     */
    val emoji: String,

    /**
     * Whether this countdown was generated in privacy mode.
     * Affects display formatting choices.
     */
    val isPrivacyMode: Boolean = false
) {
    /**
     * Formatted countdown string.
     * Examples:
     * - "4 days until ovulation"
     * - "1 day until period"
     * - "Today!"
     */
    val displayText: String
        get() = when (days) {
            0 -> "Today!"
            1 -> "1 day $label"
            else -> "$days days $label"
        }

    /**
     * Short countdown for compact display.
     * Examples: "4 days", "1 day", "Today"
     */
    val shortText: String
        get() = when (days) {
            0 -> "Today"
            1 -> "1 day"
            else -> "$days days"
        }

    /**
     * Medium-length countdown with emoji.
     * Examples: "✨ 4 days", "🌸 Today"
     */
    val mediumText: String
        get() = when (days) {
            0 -> "$emoji Today"
            1 -> "$emoji 1 day"
            else -> "$emoji $days days"
        }

    /**
     * Full countdown with emoji and label.
     * Examples: "✨ 4 days until ovulation"
     */
    val fullText: String
        get() = "$emoji $displayText"

    /**
     * Accessibility description for screen readers.
     */
    val accessibilityText: String
        get() = when (days) {
            0 -> if (isPrivacyMode) "Cycle event today" else displayText
            else -> if (isPrivacyMode) "$days days until next cycle event" else displayText
        }
}