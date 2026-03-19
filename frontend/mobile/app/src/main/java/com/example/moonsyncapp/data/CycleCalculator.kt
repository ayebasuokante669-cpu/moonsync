package com.example.moonsyncapp.data

import com.example.moonsyncapp.data.model.CyclePhase
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Pure calculation logic for menstrual cycle tracking.
 *
 * This object contains no Android dependencies and is fully unit-testable.
 * All functions are pure (same inputs always produce same outputs).
 *
 * Medical assumptions:
 * - Luteal phase is consistently 14 days (ovulation occurs 14 days before next period)
 * - Ovulation window spans 3 days (day before, day of, day after)
 * - Follicular phase is variable (causes cycle length differences between individuals)
 *
 * Used by:
 * - CycleViewModel (app UI)
 * - Widget (home screen display)
 * - Background workers (notifications)
 * - Any future features requiring cycle calculations
 */
object CycleCalculator {

    /**
     * Standard luteal phase duration in days.
     * Medical consensus: luteal phase is remarkably consistent at 14 days.
     */
    private const val LUTEAL_PHASE_DAYS = 14

    /**
     * Ovulation window duration in days.
     * Centered on ovulation day (day before, day of, day after).
     */
    private const val OVULATION_WINDOW_DAYS = 3

    /**
     * Maximum age of last period data before predictions are considered stale.
     * Set to 2 full cycles to allow for one missed log while maintaining accuracy.
     */
    const val DATA_FRESHNESS_THRESHOLD_CYCLES = 2

    /**
     * Calculate which day of the cycle the user is currently on.
     *
     * @param lastPeriodStart The date the user's last period began
     * @param today Current date (injectable for testing)
     * @param cycleLength Expected cycle length in days
     * @return Cycle day (1-based, wraps after cycleLength)
     *
     * Examples:
     * - Last period: Jan 1, Today: Jan 10 → Day 10
     * - Last period: Jan 1, Cycle: 28, Today: Jan 30 → Day 2 (new cycle)
     */
    fun calculateCycleDay(
        lastPeriodStart: LocalDate,
        today: LocalDate = LocalDate.now(),
        cycleLength: Int
    ): Int {
        val daysSinceLastPeriod = ChronoUnit.DAYS.between(lastPeriodStart, today).toInt()

        // Handle if user hasn't updated their period in a while
        // Wrap around based on cycle length
        val cycleDay = (daysSinceLastPeriod % cycleLength) + 1

        return cycleDay.coerceIn(1, cycleLength)
    }

    /**
     * Determine which phase of the cycle the user is currently in.
     *
     * Phase breakdown:
     * - Menstrual: Days 1 to [periodDuration]
     * - Follicular: After period ends until ovulation window
     * - Ovulation: 3 days centered on ovulation (14 days before next period)
     * - Luteal: After ovulation until next period
     *
     * @param cycleDay Current day of cycle (1-based)
     * @param periodDuration How many days period typically lasts
     * @param cycleLength Total cycle length
     * @return Current CyclePhase
     */
    fun determinePhase(
        cycleDay: Int,
        periodDuration: Int,
        cycleLength: Int
    ): CyclePhase {
        // Ovulation occurs 14 days before next period (standard luteal phase)
        val ovulationDay = cycleLength - LUTEAL_PHASE_DAYS

        return when {
            // Currently menstruating
            cycleDay <= periodDuration -> CyclePhase.MENSTRUAL

            // Between period end and ovulation window
            cycleDay < ovulationDay - 1 -> CyclePhase.FOLLICULAR

            // 3-day ovulation window (day before, day of, day after)
            cycleDay in (ovulationDay - 1)..(ovulationDay + 1) -> CyclePhase.OVULATION

            // After ovulation until next period
            else -> CyclePhase.LUTEAL
        }
    }

    /**
     * Calculate how many days until the next period is expected.
     *
     * @param cycleDay Current day of cycle
     * @param cycleLength Total cycle length
     * @return Days until next period (0 if today is day 1)
     */
    fun daysUntilNextPeriod(cycleDay: Int, cycleLength: Int): Int {
        return if (cycleDay == 1) {
            0 // Period is today
        } else {
            cycleLength - cycleDay + 1
        }
    }

    /**
     * Calculate how many days until ovulation is expected.
     *
     * @param cycleDay Current day of cycle
     * @param cycleLength Total cycle length
     * @return Days until ovulation (0 if in ovulation window)
     */
    fun daysUntilOvulation(cycleDay: Int, cycleLength: Int): Int {
        val ovulationDay = cycleLength - LUTEAL_PHASE_DAYS

        return when {
            // Before ovulation window
            cycleDay < ovulationDay - 1 -> ovulationDay - cycleDay

            // In ovulation window
            cycleDay in (ovulationDay - 1)..(ovulationDay + 1) -> 0

            // Past ovulation this cycle, calculate for next cycle
            else -> {
                val daysUntilNextCycle = cycleLength - cycleDay + 1
                daysUntilNextCycle + ovulationDay - 1
            }
        }
    }

    /**
     * Calculate progress through the current phase (0.0 to 1.0).
     *
     * Used for visual progress indicators in UI and widgets.
     *
     * @param cycleDay Current day of cycle
     * @param phase Current phase
     * @param periodDuration Period length
     * @param cycleLength Total cycle length
     * @return Progress as float between 0.0 and 1.0
     */
    fun phaseProgress(
        cycleDay: Int,
        phase: CyclePhase,
        periodDuration: Int,
        cycleLength: Int
    ): Float {
        val ovulationDay = cycleLength - LUTEAL_PHASE_DAYS

        val (phaseStart, phaseEnd) = when (phase) {
            CyclePhase.MENSTRUAL -> 1 to periodDuration
            CyclePhase.FOLLICULAR -> (periodDuration + 1) to (ovulationDay - 2)
            CyclePhase.OVULATION -> (ovulationDay - 1) to (ovulationDay + 1)
            CyclePhase.LUTEAL -> (ovulationDay + 2) to cycleLength
        }

        val phaseDuration = phaseEnd - phaseStart + 1
        val dayInPhase = cycleDay - phaseStart + 1

        return (dayInPhase.toFloat() / phaseDuration.toFloat()).coerceIn(0f, 1f)
    }

    /**
     * Calculate the expected date of the next period.
     *
     * @param lastPeriodStart Last period start date
     * @param cycleLength Total cycle length
     * @param today Current date (injectable for testing)
     * @return Expected next period date
     */
    fun calculateNextPeriodDate(
        lastPeriodStart: LocalDate,
        cycleLength: Int,
        today: LocalDate = LocalDate.now()
    ): LocalDate {
        var nextPeriodDate = lastPeriodStart.plusDays(cycleLength.toLong())

        // If calculated date is in the past, advance to next cycle
        while (nextPeriodDate.isBefore(today) || nextPeriodDate.isEqual(today)) {
            nextPeriodDate = nextPeriodDate.plusDays(cycleLength.toLong())
        }

        return nextPeriodDate
    }

    /**
     * Calculate the expected ovulation date.
     *
     * @param lastPeriodStart Last period start date
     * @param cycleLength Total cycle length
     * @param today Current date (injectable for testing)
     * @return Expected ovulation date
     */
    fun calculateOvulationDate(
        lastPeriodStart: LocalDate,
        cycleLength: Int,
        today: LocalDate = LocalDate.now()
    ): LocalDate {
        val ovulationDayOffset = cycleLength - LUTEAL_PHASE_DAYS
        var ovulationDate = lastPeriodStart.plusDays(ovulationDayOffset.toLong())

        // If calculated date is in the past, advance to next cycle
        while (ovulationDate.isBefore(today)) {
            ovulationDate = ovulationDate.plusDays(cycleLength.toLong())
        }

        return ovulationDate
    }

    /**
     * Get the number of days remaining in the current phase.
     *
     * @param cycleDay Current day of cycle
     * @param phase Current phase
     * @param periodDuration Period length
     * @param cycleLength Total cycle length
     * @return Days remaining in phase (0 if last day of phase)
     */
    fun daysRemainingInPhase(
        cycleDay: Int,
        phase: CyclePhase,
        periodDuration: Int,
        cycleLength: Int
    ): Int {
        val ovulationDay = cycleLength - LUTEAL_PHASE_DAYS

        val phaseEnd = when (phase) {
            CyclePhase.MENSTRUAL -> periodDuration
            CyclePhase.FOLLICULAR -> ovulationDay - 2
            CyclePhase.OVULATION -> ovulationDay + 1
            CyclePhase.LUTEAL -> cycleLength
        }

        return (phaseEnd - cycleDay).coerceAtLeast(0)
    }

    /**
     * Get the total duration of a phase in days.
     *
     * @param phase The phase to query
     * @param periodDuration Period length
     * @param cycleLength Total cycle length
     * @return Total days in phase
     */
    fun phaseDuration(
        phase: CyclePhase,
        periodDuration: Int,
        cycleLength: Int
    ): Int {
        val ovulationDay = cycleLength - LUTEAL_PHASE_DAYS

        return when (phase) {
            CyclePhase.MENSTRUAL -> periodDuration
            CyclePhase.FOLLICULAR -> ovulationDay - periodDuration - 2
            CyclePhase.OVULATION -> OVULATION_WINDOW_DAYS
            CyclePhase.LUTEAL -> cycleLength - ovulationDay - 1
        }
    }

    /**
     * Determine if cycle data is fresh enough for reliable predictions.
     *
     * Data is considered stale after 2 full cycle lengths have passed since
     * the last logged period. This allows for one missed cycle while still
     * maintaining reasonable accuracy.
     *
     * Use case: Show "update needed" prompt in widget/app when data is stale.
     *
     * @param lastPeriodStart Last logged period start date
     * @param cycleLength User's expected cycle length
     * @param today Current date (injectable for testing)
     * @return true if data is fresh (within 2 cycles), false if stale
     *
     * Examples:
     * - Last period: 10 days ago, Cycle: 28 → fresh (true)
     * - Last period: 60 days ago, Cycle: 28 → stale (false)
     */
    fun isDataFresh(
        lastPeriodStart: LocalDate,
        cycleLength: Int,
        today: LocalDate = LocalDate.now()
    ): Boolean {
        val daysSinceLastPeriod = ChronoUnit.DAYS.between(lastPeriodStart, today)
        val freshnessThreshold = cycleLength * DATA_FRESHNESS_THRESHOLD_CYCLES
        return daysSinceLastPeriod <= freshnessThreshold
    }
}