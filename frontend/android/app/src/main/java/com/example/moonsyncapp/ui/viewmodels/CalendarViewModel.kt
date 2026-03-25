package com.example.moonsyncapp.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moonsyncapp.data.ApiClient
import com.example.moonsyncapp.data.model.CycleData
import com.example.moonsyncapp.data.model.CyclePhase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

class CalendarViewModel : ViewModel() {

    // ========================================
    // STATE
    // ========================================

    private val _cycleData = MutableStateFlow(getDefaultCycleData())
    val cycleData: StateFlow<CycleData> = _cycleData.asStateFlow()

    private val _calendarDisplayData = MutableStateFlow(buildDisplayData(_cycleData.value))
    val calendarDisplayData: StateFlow<CalendarDisplayData> = _calendarDisplayData.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchCalendarData()
    }

    // ========================================
    // API
    // ========================================

    private fun fetchCalendarData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = ApiClient.get("/calendar/preview")
                result.onSuccess { json ->
                    parseCalendarPreview(json)?.let { (cycleData, displayData) ->
                        _cycleData.value = cycleData
                        _calendarDisplayData.value = displayData
                    }
                }
                // On failure, keep existing data
            } catch (_: Exception) {
                // Keep default data
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun parseCalendarPreview(json: String): Pair<CycleData, CalendarDisplayData>? = try {
        val obj = JSONObject(json)
        val iso = DateTimeFormatter.ISO_LOCAL_DATE
        val phaseStr = obj.optString("current_phase", "follicular").uppercase()
        val phase = try { CyclePhase.valueOf(phaseStr) } catch (_: Exception) { CyclePhase.FOLLICULAR }

        val nextPeriod = obj.optString("next_period_date").let {
            if (it.isNotEmpty()) runCatching { LocalDate.parse(it, iso) }.getOrNull() else null
        } ?: LocalDate.now().plusDays(obj.optInt("days_until_next_period", 8).toLong())

        val ovulation = obj.optString("ovulation_date").let {
            if (it.isNotEmpty()) runCatching { LocalDate.parse(it, iso) }.getOrNull() else null
        } ?: LocalDate.now().plusDays(obj.optInt("days_until_ovulation", 1).toLong())

        val cycleData = CycleData(
            userName = obj.optString("user_name", _cycleData.value.userName),
            currentPhase = phase,
            cycleDay = obj.optInt("cycle_day", _cycleData.value.cycleDay),
            cycleLength = obj.optInt("cycle_length", _cycleData.value.cycleLength),
            daysUntilNextPeriod = obj.optInt("days_until_next_period", _cycleData.value.daysUntilNextPeriod),
            daysUntilOvulation = obj.optInt("days_until_ovulation", _cycleData.value.daysUntilOvulation),
            periodDaysRemaining = obj.optInt("period_days_remaining", -1).takeIf { it >= 0 },
            phaseProgress = obj.optDouble("phase_progress", _cycleData.value.phaseProgress.toDouble()).toFloat(),
            phaseDaysRemaining = obj.optInt("phase_days_remaining", _cycleData.value.phaseDaysRemaining),
            phaseTotalDays = obj.optInt("phase_total_days", _cycleData.value.phaseTotalDays),
            nextPeriodDate = nextPeriod,
            ovulationDate = ovulation
        )

        // Parse date arrays if provided, otherwise compute from cycle data
        val periodDates = parseDateArray(obj.optJSONArray("period_dates"))
            ?: generateDateRange(
                LocalDate.now().minusDays(cycleData.cycleDay.toLong() - 1), 5
            )
        val predictedDates = parseDateArray(obj.optJSONArray("predicted_period_dates"))
            ?: generateDateRange(cycleData.nextPeriodDate, 5)
        val fertileDates = parseDateArray(obj.optJSONArray("fertile_dates"))
            ?: generateDateRange(cycleData.ovulationDate.minusDays(5), 7)

        val displayData = CalendarDisplayData(
            periodDates = periodDates,
            predictedPeriodDates = predictedDates,
            fertileDates = fertileDates,
            ovulationDate = cycleData.ovulationDate
        )

        Pair(cycleData, displayData)
    } catch (_: Exception) { null }

    private fun parseDateArray(arr: JSONArray?): Set<LocalDate>? {
        arr ?: return null
        if (arr.length() == 0) return null
        val iso = DateTimeFormatter.ISO_LOCAL_DATE
        return (0 until arr.length()).mapNotNull { i ->
            runCatching { LocalDate.parse(arr.getString(i), iso) }.getOrNull()
        }.toSet()
    }

    // ========================================
    // ACTIONS
    // ========================================

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun clearSelectedDate() {
        _selectedDate.value = null
    }

    fun logPeriodDay(date: LocalDate) {
        // TODO: Backend endpoint for period day logging
    }

    fun refreshData() {
        fetchCalendarData(isRefresh = true)
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

    // ========================================
    // PRIVATE HELPERS
    // ========================================

    private fun generateDateRange(start: LocalDate, days: Int): Set<LocalDate> {
        return generateSequence(start) { it.plusDays(1) }
            .take(days)
            .toSet()
    }
}

private fun getDefaultCycleData(): CycleData {
    val today = LocalDate.now()
    return CycleData(
        userName = "",
        currentPhase = CyclePhase.FOLLICULAR,
        cycleDay = 12,
        cycleLength = 28,
        daysUntilNextPeriod = 8,
        daysUntilOvulation = 1,
        periodDaysRemaining = null,
        phaseProgress = 0.6f,
        phaseDaysRemaining = 2,
        phaseTotalDays = 5,
        nextPeriodDate = today.plusDays(8),
        ovulationDate = today.plusDays(1)
    )
}

private fun buildDisplayData(cycleData: CycleData): CalendarDisplayData {
    val today = LocalDate.now()
    val lastPeriodStart = today.minusDays(cycleData.cycleDay.toLong() - 1)
    val periodDuration = 5
    val periodDates = generateSequence(lastPeriodStart) { it.plusDays(1) }.take(periodDuration).toSet()
    val predictedPeriodDates = generateSequence(cycleData.nextPeriodDate) { it.plusDays(1) }.take(periodDuration).toSet()
    val fertileStart = cycleData.ovulationDate.minusDays(5)
    val fertileDates = generateSequence(fertileStart) { it.plusDays(1) }.take(7).toSet()
    return CalendarDisplayData(
        periodDates = periodDates,
        predictedPeriodDates = predictedPeriodDates,
        fertileDates = fertileDates,
        ovulationDate = cycleData.ovulationDate
    )
}

class CalendarViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            return CalendarViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
