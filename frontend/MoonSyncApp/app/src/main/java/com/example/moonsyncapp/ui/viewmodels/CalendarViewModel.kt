package com.example.moonsyncapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.moonsyncapp.data.model.CycleData
import com.example.moonsyncapp.data.model.CyclePhase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

/**
 * Additional calendar-specific display data
 * Complements CycleData with date sets for calendar rendering
 */
data class CalendarDisplayData(
    val periodDates: Set<LocalDate>,
    val predictedPeriodDates: Set<LocalDate>,
    val fertileDates: Set<LocalDate>,
    val ovulationDate: LocalDate
)

/**
 * CalendarViewModel
 *
 * Frontend: Uses mock data for now
 * Backend team: Replace getMockCycleData() with real data from Repository
 */
class CalendarViewModel : ViewModel() {

    // ========================================
    // MOCK DATA - Backend replaces this section
    // ========================================

    private fun getMockCycleData(): CycleData {
        val today = LocalDate.now()
        val nextPeriod = today.plusDays(8)
        val ovulation = today.plusDays(1)

        return CycleData(
            userName = "User",
            currentPhase = CyclePhase.FOLLICULAR,
            cycleDay = 12,
            cycleLength = 28,
            daysUntilNextPeriod = 8,
            daysUntilOvulation = 1,
            periodDaysRemaining = null,
            phaseProgress = 0.6f,
            phaseDaysRemaining = 2,
            phaseTotalDays = 5,
            nextPeriodDate = nextPeriod,
            ovulationDate = ovulation
        )
    }

    private fun getMockCalendarDisplayData(cycleData: CycleData): CalendarDisplayData {
        val today = LocalDate.now()

        // Last period (example: 12 days ago, lasted 5 days)
        val lastPeriodStart = today.minusDays(cycleData.cycleDay.toLong() - 1)
        val periodDuration = 5
        val periodDates = generateDateRange(lastPeriodStart, periodDuration)

        // Predicted next period
        val predictedPeriodDates = generateDateRange(cycleData.nextPeriodDate, periodDuration)

        // Fertile window (5 days before ovulation + 1 day after)
        val fertileStart = cycleData.ovulationDate.minusDays(5)
        val fertileDates = generateDateRange(fertileStart, 7)

        return CalendarDisplayData(
            periodDates = periodDates,
            predictedPeriodDates = predictedPeriodDates,
            fertileDates = fertileDates,
            ovulationDate = cycleData.ovulationDate
        )
    }

    private fun generateDateRange(start: LocalDate, days: Int): Set<LocalDate> {
        return generateSequence(start) { it.plusDays(1) }
            .take(days)
            .toSet()
    }

    // ========================================
    // STATE
    // ========================================

    private val _cycleData = MutableStateFlow(getMockCycleData())
    val cycleData: StateFlow<CycleData> = _cycleData.asStateFlow()

    private val _calendarDisplayData = MutableStateFlow(getMockCalendarDisplayData(_cycleData.value))
    val calendarDisplayData: StateFlow<CalendarDisplayData> = _calendarDisplayData.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    // ========================================
    // ACTIONS
    // ========================================

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun clearSelectedDate() {
        _selectedDate.value = null
    }

    /**
     * Called when user logs a period day
     * Backend team: Save to database, then recalculate
     */
    fun logPeriodDay(date: LocalDate) {
        // TODO: Backend implements saving to database
    }

    /**
     * Refresh data from backend
     * Backend team: Fetch from Repository
     */
    fun refreshData() {
        _cycleData.value = getMockCycleData()
        _calendarDisplayData.value = getMockCalendarDisplayData(_cycleData.value)
    }

    // ========================================
    // HELPER FUNCTIONS FOR UI
    // ========================================

    fun isDateInPeriod(date: LocalDate): Boolean {
        return date in _calendarDisplayData.value.periodDates
    }

    fun isDatePredictedPeriod(date: LocalDate): Boolean {
        return date in _calendarDisplayData.value.predictedPeriodDates
    }

    fun isDateFertile(date: LocalDate): Boolean {
        return date in _calendarDisplayData.value.fertileDates
    }

    fun isDateOvulation(date: LocalDate): Boolean {
        return date == _calendarDisplayData.value.ovulationDate
    }
}