//package com.example.moonsyncapp.ui.screens.home
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.moonsyncapp.data.model.CycleData
//import com.example.moonsyncapp.data.model.CyclePhase
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import java.time.LocalDate
//import java.time.LocalTime
//
//class CycleViewModel : ViewModel() {
//
//    private val _cycleData = MutableStateFlow(getSampleCycleData())
//    val cycleData: StateFlow<CycleData> = _cycleData.asStateFlow()
//
//    private val _isRefreshing = MutableStateFlow(false)
//    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
//
//    private val _isLoading = MutableStateFlow(true)
//    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
//
//    private val _hasUnreadNotifications = MutableStateFlow(true)
//    val hasUnreadNotifications: StateFlow<Boolean> = _hasUnreadNotifications.asStateFlow()
//
//    private val _cycleStreak = MutableStateFlow(5) // Months tracked
//    val cycleStreak: StateFlow<Int> = _cycleStreak.asStateFlow()
//
//    init {
//        // Simulate initial data loading
//        viewModelScope.launch {
//            delay(1500) // Simulate network delay
//            _isLoading.value = false
//        }
//    }
//
//    fun refresh() {
//        viewModelScope.launch {
//            _isRefreshing.value = true
//            delay(1200)
//            _cycleData.value = getSampleCycleData()
//            _isRefreshing.value = false
//        }
//    }
//
//    fun markNotificationsRead() {
//        _hasUnreadNotifications.value = false
//    }
//
//    fun getGreeting(): String {
//        val hour = LocalTime.now().hour
//        return when {
//            hour < 12 -> "Good morning"
//            hour < 17 -> "Good afternoon"
//            else -> "Good evening"
//        }
//    }
//
//    fun getGreetingEmoji(): String {
//        val hour = LocalTime.now().hour
//        return when {
//            hour < 12 -> "☀️"
//            hour < 17 -> "🌤️"
//            else -> "🌙"
//        }
//    }
//
//    fun getNextEventInfo(cycleData: CycleData): NextEventInfo {
//        return when (cycleData.currentPhase) {
//            CyclePhase.MENSTRUAL -> NextEventInfo(
//                title = "Ovulation",
//                countdown = "In ${cycleData.daysUntilOvulation} days",
//                date = cycleData.getOvulationFormatted(),
//                subtitle = "Fertility window approaching",
//                progress = 1f - (cycleData.daysUntilOvulation.toFloat() / 14f)
//            )
//            CyclePhase.FOLLICULAR -> NextEventInfo(
//                title = "Ovulation",
//                countdown = "In ${cycleData.daysUntilOvulation} days",
//                date = cycleData.getOvulationFormatted(),
//                subtitle = "Fertility window approaching",
//                progress = 1f - (cycleData.daysUntilOvulation.toFloat() / 14f)
//            )
//            CyclePhase.OVULATION -> NextEventInfo(
//                title = "Next Period",
//                countdown = "In ${cycleData.daysUntilNextPeriod} days",
//                date = cycleData.getNextPeriodFormatted(),
//                subtitle = "Luteal phase begins soon",
//                progress = 1f - (cycleData.daysUntilNextPeriod.toFloat() / 14f)
//            )
//            CyclePhase.LUTEAL -> NextEventInfo(
//                title = "Next Period",
//                countdown = "In ${cycleData.daysUntilNextPeriod} days",
//                date = cycleData.getNextPeriodFormatted(),
//                subtitle = "Prepare for your cycle",
//                progress = 1f - (cycleData.daysUntilNextPeriod.toFloat() / 14f)
//            )
//        }
//    }
//
//    private fun getSampleCycleData(): CycleData {
//        return CycleData(
//            userName = "Ada",
//            currentPhase = CyclePhase.FOLLICULAR,
//            cycleDay = 10,
//            cycleLength = 28,
//            daysUntilNextPeriod = 18,
//            daysUntilOvulation = 4,
//            periodDaysRemaining = null,
//            phaseProgress = 0.625f,
//            phaseDaysRemaining = 4,
//            phaseTotalDays = 9,
//            nextPeriodDate = LocalDate.now().plusDays(18),
//            ovulationDate = LocalDate.now().plusDays(4)
//        )
//    }
//}
//
//data class NextEventInfo(
//    val title: String,
//    val countdown: String,
//    val date: String,
//    val subtitle: String,
//    val progress: Float
//)
package com.example.moonsyncapp.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moonsyncapp.data.ApiClient
import com.example.moonsyncapp.data.LoggingDataStore
import com.example.moonsyncapp.data.model.CycleData
import com.example.moonsyncapp.data.model.CyclePhase
import com.example.moonsyncapp.data.model.LoggingStreak
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class CycleViewModel(
    private val loggingDataStore: LoggingDataStore
) : ViewModel() {

    private val _cycleData = MutableStateFlow(CycleData(
        userName = "",
        currentPhase = CyclePhase.FOLLICULAR,
        cycleDay = 10,
        cycleLength = 28,
        daysUntilNextPeriod = 18,
        daysUntilOvulation = 4,
        periodDaysRemaining = null,
        phaseProgress = 0.625f,
        phaseDaysRemaining = 4,
        phaseTotalDays = 9,
        nextPeriodDate = java.time.LocalDate.now().plusDays(18),
        ovulationDate = java.time.LocalDate.now().plusDays(4)
    ))
    val cycleData: StateFlow<CycleData> = _cycleData.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasUnreadNotifications = MutableStateFlow(true)
    val hasUnreadNotifications: StateFlow<Boolean> = _hasUnreadNotifications.asStateFlow()

    private val _cycleStreak = MutableStateFlow(0)
    val cycleStreak: StateFlow<Int> = _cycleStreak.asStateFlow()

    init {
        fetchCycleData()
        loadStreak()
        checkNotifications()
    }

    private fun checkNotifications() {
        viewModelScope.launch {
            try {
                val result = ApiClient.get("/notifications/ping")
                result.onSuccess { json ->
                    val hasUnread = try {
                        org.json.JSONObject(json).optBoolean("has_unread", true)
                    } catch (_: Exception) { true }
                    _hasUnreadNotifications.value = hasUnread
                }
            } catch (_: Exception) {
                // Keep default (true) so badge shows until confirmed otherwise
            }
        }
    }

    private fun fetchCycleData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) _isRefreshing.value = true else _isLoading.value = true
            try {
                val result = ApiClient.get("/cycle/preview")
                result.onSuccess { json ->
                    parseCyclePreview(json)?.let { _cycleData.value = it }
                }
                // On failure, sample data already in state — keep it
            } catch (_: Exception) {
                // Keep sample data
            } finally {
                if (isRefresh) _isRefreshing.value = false else _isLoading.value = false
            }
        }
    }

    private fun parseCyclePreview(json: String): CycleData? = try {
        val obj = JSONObject(json)
        val iso = DateTimeFormatter.ISO_LOCAL_DATE
        val phaseStr = obj.optString("current_phase", "follicular").uppercase()
        val phase = try { CyclePhase.valueOf(phaseStr) } catch (_: Exception) { CyclePhase.FOLLICULAR }
        val nextPeriod = obj.optString("next_period_date").let {
            if (it.isNotEmpty()) runCatching { LocalDate.parse(it, iso) }.getOrNull() else null
        } ?: LocalDate.now().plusDays(obj.optInt("days_until_next_period", 18).toLong())
        val ovulation = obj.optString("ovulation_date").let {
            if (it.isNotEmpty()) runCatching { LocalDate.parse(it, iso) }.getOrNull() else null
        } ?: LocalDate.now().plusDays(obj.optInt("days_until_ovulation", 4).toLong())

        CycleData(
            userName = obj.optString("user_name", "").ifEmpty { _cycleData.value.userName },
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
    } catch (_: Exception) { null }

    private fun loadStreak() {
        viewModelScope.launch {
            try {
                val savedStreak = loggingDataStore.getStreak()
                if (savedStreak != null) {
                    val today = LocalDate.now()
                    val daysSinceLastLog = if (savedStreak.lastLogDate != null) {
                        ChronoUnit.DAYS.between(savedStreak.lastLogDate, today)
                    } else {
                        Long.MAX_VALUE
                    }

                    // Show current streak if active (logged today or yesterday)
                    val displayStreak = when {
                        daysSinceLastLog <= 1L -> savedStreak.currentStreak
                        else -> 0 // Streak broken
                    }

                    _cycleStreak.value = displayStreak
                }
            } catch (e: Exception) {
                _cycleStreak.value = 0
            }
        }
    }

    fun refresh() {
        fetchCycleData(isRefresh = true)
        loadStreak()
        checkNotifications()
    }

    fun markNotificationsRead() {
        _hasUnreadNotifications.value = false
    }

    fun getGreeting(): String {
        val hour = LocalTime.now().hour
        return when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    fun getGreetingEmoji(): String {
        val hour = LocalTime.now().hour
        return when {
            hour < 12 -> "☀️"
            hour < 17 -> "🌤️"
            else -> "🌙"
        }
    }

    fun getNextEventInfo(cycleData: CycleData): NextEventInfo {
        return when (cycleData.currentPhase) {
            CyclePhase.MENSTRUAL -> NextEventInfo(
                title = "Ovulation",
                countdown = "In ${cycleData.daysUntilOvulation} days",
                date = cycleData.getOvulationFormatted(),
                subtitle = "Fertility window approaching",
                progress = 1f - (cycleData.daysUntilOvulation.toFloat() / 14f)
            )
            CyclePhase.FOLLICULAR -> NextEventInfo(
                title = "Ovulation",
                countdown = "In ${cycleData.daysUntilOvulation} days",
                date = cycleData.getOvulationFormatted(),
                subtitle = "Fertility window approaching",
                progress = 1f - (cycleData.daysUntilOvulation.toFloat() / 14f)
            )
            CyclePhase.OVULATION -> NextEventInfo(
                title = "Next Period",
                countdown = "In ${cycleData.daysUntilNextPeriod} days",
                date = cycleData.getNextPeriodFormatted(),
                subtitle = "Luteal phase begins soon",
                progress = 1f - (cycleData.daysUntilNextPeriod.toFloat() / 14f)
            )
            CyclePhase.LUTEAL -> NextEventInfo(
                title = "Next Period",
                countdown = "In ${cycleData.daysUntilNextPeriod} days",
                date = cycleData.getNextPeriodFormatted(),
                subtitle = "Prepare for your cycle",
                progress = 1f - (cycleData.daysUntilNextPeriod.toFloat() / 14f)
            )
        }
    }

}

data class NextEventInfo(
    val title: String,
    val countdown: String,
    val date: String,
    val subtitle: String,
    val progress: Float
)

/**
 * Factory for CycleViewModel — provides LoggingDataStore dependency.
 */
class CycleViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CycleViewModel::class.java)) {
            return CycleViewModel(LoggingDataStore(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}