package com.example.moonsyncapp.data

import com.example.moonsyncapp.data.model.CyclePhase
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for CycleCalculator.
 *
 * These tests validate core cycle calculation logic without Android dependencies.
 * Coverage areas:
 * - Cycle day calculation and wrapping
 * - Phase determination for all phases
 * - Countdown calculations (period, ovulation)
 * - Progress calculation
 * - Data freshness validation
 */
class CycleCalculatorTest {

    // ==================== CYCLE DAY CALCULATION ====================

    @Test
    fun `calculateCycleDay - same day as last period is Day 1`() {
        val lastPeriod = LocalDate.of(2025, 1, 15)
        val today = LocalDate.of(2025, 1, 15)

        val result = CycleCalculator.calculateCycleDay(
            lastPeriodStart = lastPeriod,
            today = today,
            cycleLength = 28
        )

        assertEquals(1, result)
    }

    @Test
    fun `calculateCycleDay - 10 days after last period is Day 11`() {
        val lastPeriod = LocalDate.of(2025, 1, 1)
        val today = LocalDate.of(2025, 1, 11)

        val result = CycleCalculator.calculateCycleDay(
            lastPeriodStart = lastPeriod,
            today = today,
            cycleLength = 28
        )

        assertEquals(11, result)
    }

    @Test
    fun `calculateCycleDay - wraps correctly after full cycle`() {
        val lastPeriod = LocalDate.of(2025, 1, 1)
        val today = LocalDate.of(2025, 1, 30) // 29 days later

        val result = CycleCalculator.calculateCycleDay(
            lastPeriodStart = lastPeriod,
            today = today,
            cycleLength = 28
        )

        assertEquals(2, result) // Day 2 of new cycle
    }

    @Test
    fun `calculateCycleDay - handles 35-day cycle correctly`() {
        val lastPeriod = LocalDate.of(2025, 1, 1)
        val today = LocalDate.of(2025, 1, 21)

        val result = CycleCalculator.calculateCycleDay(
            lastPeriodStart = lastPeriod,
            today = today,
            cycleLength = 35
        )

        assertEquals(21, result)
    }

    // ==================== PHASE DETERMINATION ====================

    @Test
    fun `determinePhase - day 1 is menstrual`() {
        val phase = CycleCalculator.determinePhase(
            cycleDay = 1,
            periodDuration = 5,
            cycleLength = 28
        )

        assertEquals(CyclePhase.MENSTRUAL, phase)
    }

    @Test
    fun `determinePhase - last day of period is still menstrual`() {
        val phase = CycleCalculator.determinePhase(
            cycleDay = 5,
            periodDuration = 5,
            cycleLength = 28
        )

        assertEquals(CyclePhase.MENSTRUAL, phase)
    }

    @Test
    fun `determinePhase - day after period is follicular`() {
        val phase = CycleCalculator.determinePhase(
            cycleDay = 6,
            periodDuration = 5,
            cycleLength = 28
        )

        assertEquals(CyclePhase.FOLLICULAR, phase)
    }

    @Test
    fun `determinePhase - day 14 of 28-day cycle is ovulation`() {
        val phase = CycleCalculator.determinePhase(
            cycleDay = 14,
            periodDuration = 5,
            cycleLength = 28
        )

        assertEquals(CyclePhase.OVULATION, phase)
    }

    @Test
    fun `determinePhase - day before ovulation is ovulation window`() {
        // For 28-day cycle, ovulation day is 14 (28-14), window is 13-15
        val phase = CycleCalculator.determinePhase(
            cycleDay = 13,
            periodDuration = 5,
            cycleLength = 28
        )

        assertEquals(CyclePhase.OVULATION, phase)
    }

    @Test
    fun `determinePhase - day after ovulation window is luteal`() {
        val phase = CycleCalculator.determinePhase(
            cycleDay = 16,
            periodDuration = 5,
            cycleLength = 28
        )

        assertEquals(CyclePhase.LUTEAL, phase)
    }

    @Test
    fun `determinePhase - last day of cycle is luteal`() {
        val phase = CycleCalculator.determinePhase(
            cycleDay = 28,
            periodDuration = 5,
            cycleLength = 28
        )

        assertEquals(CyclePhase.LUTEAL, phase)
    }

    @Test
    fun `determinePhase - 35-day cycle has correct ovulation day`() {
        // Ovulation should be day 21 (35-14)
        val phase = CycleCalculator.determinePhase(
            cycleDay = 21,
            periodDuration = 5,
            cycleLength = 35
        )

        assertEquals(CyclePhase.OVULATION, phase)
    }

    // ==================== DAYS UNTIL CALCULATIONS ====================

    @Test
    fun `daysUntilNextPeriod - on day 1 returns 0`() {
        val result = CycleCalculator.daysUntilNextPeriod(
            cycleDay = 1,
            cycleLength = 28
        )

        assertEquals(0, result)
    }

    @Test
    fun `daysUntilNextPeriod - on day 20 of 28-day cycle returns 9`() {
        val result = CycleCalculator.daysUntilNextPeriod(
            cycleDay = 20,
            cycleLength = 28
        )

        assertEquals(9, result)
    }

    @Test
    fun `daysUntilOvulation - day 10 of 28-day cycle returns 4`() {
        // Ovulation day is 14 (28-14), so from day 10 it's 4 days
        val result = CycleCalculator.daysUntilOvulation(
            cycleDay = 10,
            cycleLength = 28
        )

        assertEquals(4, result)
    }

    @Test
    fun `daysUntilOvulation - during ovulation window returns 0`() {
        val result = CycleCalculator.daysUntilOvulation(
            cycleDay = 14,
            cycleLength = 28
        )

        assertEquals(0, result)
    }

    @Test
    fun `daysUntilOvulation - after ovulation calculates for next cycle`() {
        // Day 20 of 28-day cycle, ovulation passed
        // Days until next cycle: 28-20+1 = 9
        // Plus ovulation day offset: 9 + 14 - 1 = 22
        val result = CycleCalculator.daysUntilOvulation(
            cycleDay = 20,
            cycleLength = 28
        )

        assertEquals(22, result)
    }

    // ==================== PROGRESS CALCULATION ====================

    @Test
    fun `phaseProgress - first day of menstrual phase is near 0`() {
        val progress = CycleCalculator.phaseProgress(
            cycleDay = 1,
            phase = CyclePhase.MENSTRUAL,
            periodDuration = 5,
            cycleLength = 28
        )

        // First day of 5-day period: 1/5 = 0.2
        assertEquals(0.2f, progress, 0.01f)
    }

    @Test
    fun `phaseProgress - last day of phase approaches 1`() {
        val progress = CycleCalculator.phaseProgress(
            cycleDay = 5,
            phase = CyclePhase.MENSTRUAL,
            periodDuration = 5,
            cycleLength = 28
        )

        // Last day of 5-day period: 5/5 = 1.0
        assertEquals(1.0f, progress, 0.01f)
    }

    // ==================== DATE CALCULATIONS ====================

    @Test
    fun `calculateNextPeriodDate - returns future date`() {
        val lastPeriod = LocalDate.of(2025, 1, 1)
        val today = LocalDate.of(2025, 1, 15)

        val result = CycleCalculator.calculateNextPeriodDate(
            lastPeriodStart = lastPeriod,
            cycleLength = 28,
            today = today
        )

        // Next period should be Jan 29 (1 + 28)
        assertEquals(LocalDate.of(2025, 1, 29), result)
    }

    @Test
    fun `calculateNextPeriodDate - skips past dates`() {
        val lastPeriod = LocalDate.of(2024, 12, 1)
        val today = LocalDate.of(2025, 2, 1)

        val result = CycleCalculator.calculateNextPeriodDate(
            lastPeriodStart = lastPeriod,
            cycleLength = 28,
            today = today
        )

        // Should advance to next future period, not return a past date
        assertTrue(result.isAfter(today))
    }

    @Test
    fun `calculateOvulationDate - returns correct date for standard cycle`() {
        val lastPeriod = LocalDate.of(2025, 1, 1)
        val today = LocalDate.of(2025, 1, 5)

        val result = CycleCalculator.calculateOvulationDate(
            lastPeriodStart = lastPeriod,
            cycleLength = 28,
            today = today
        )

        // Ovulation on day 14: Jan 1 + 14 = Jan 15
        assertEquals(LocalDate.of(2025, 1, 15), result)
    }

    // ==================== PHASE DURATION ====================

    @Test
    fun `phaseDuration - menstrual phase equals period duration`() {
        val duration = CycleCalculator.phaseDuration(
            phase = CyclePhase.MENSTRUAL,
            periodDuration = 5,
            cycleLength = 28
        )

        assertEquals(5, duration)
    }

    @Test
    fun `phaseDuration - ovulation phase is always 3 days`() {
        val duration = CycleCalculator.phaseDuration(
            phase = CyclePhase.OVULATION,
            periodDuration = 5,
            cycleLength = 28
        )

        assertEquals(3, duration)
    }

    // ==================== DATA FRESHNESS ====================

    @Test
    fun `isDataFresh - 10 days old is fresh`() {
        val lastPeriod = LocalDate.now().minusDays(10)
        val result = CycleCalculator.isDataFresh(
            lastPeriodStart = lastPeriod,
            cycleLength = 28
        )

        assertTrue(result)
    }

    @Test
    fun `isDataFresh - exactly 2 cycles old is still fresh`() {
        val lastPeriod = LocalDate.now().minusDays(56) // Exactly 2 * 28
        val result = CycleCalculator.isDataFresh(
            lastPeriodStart = lastPeriod,
            cycleLength = 28
        )

        assertTrue(result)
    }

    @Test
    fun `isDataFresh - over 2 cycles old is stale`() {
        val lastPeriod = LocalDate.now().minusDays(60) // More than 2 * 28
        val result = CycleCalculator.isDataFresh(
            lastPeriodStart = lastPeriod,
            cycleLength = 28
        )

        assertFalse(result)
    }

    @Test
    fun `isDataFresh - 180 days old is definitely stale`() {
        val lastPeriod = LocalDate.now().minusDays(180)
        val result = CycleCalculator.isDataFresh(
            lastPeriodStart = lastPeriod,
            cycleLength = 28
        )

        assertFalse(result)
    }

    @Test
    fun `isDataFresh - handles 35-day cycle threshold correctly`() {
        // 2 cycles of 35 days = 70 days
        val lastPeriod = LocalDate.now().minusDays(71)
        val result = CycleCalculator.isDataFresh(
            lastPeriodStart = lastPeriod,
            cycleLength = 35
        )

        assertFalse(result) // 71 days is stale for 35-day cycle
    }

    @Test
    fun `daysRemainingInPhase - returns correct value for menstrual`() {
        val result = CycleCalculator.daysRemainingInPhase(
            cycleDay = 3,
            phase = CyclePhase.MENSTRUAL,
            periodDuration = 5,
            cycleLength = 28
        )

        assertEquals(2, result) // Days 4 and 5 remaining
    }

    @Test
    fun `daysRemainingInPhase - returns 0 on last day of phase`() {
        val result = CycleCalculator.daysRemainingInPhase(
            cycleDay = 5,
            phase = CyclePhase.MENSTRUAL,
            periodDuration = 5,
            cycleLength = 28
        )

        assertEquals(0, result)
    }
}