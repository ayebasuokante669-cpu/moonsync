package com.example.moonsyncapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moonsyncapp.data.model.CycleData
import com.example.moonsyncapp.data.model.CyclePhase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class CycleViewModel : ViewModel() {

    private val _cycleData = MutableStateFlow(getSampleCycleData())
    val cycleData: StateFlow<CycleData> = _cycleData.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasUnreadNotifications = MutableStateFlow(true)
    val hasUnreadNotifications: StateFlow<Boolean> = _hasUnreadNotifications.asStateFlow()

    private val _cycleStreak = MutableStateFlow(5) // Months tracked
    val cycleStreak: StateFlow<Int> = _cycleStreak.asStateFlow()

    init {
        // Simulate initial data loading
        viewModelScope.launch {
            delay(1500) // Simulate network delay
            _isLoading.value = false
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(1200)
            _cycleData.value = getSampleCycleData()
            _isRefreshing.value = false
        }
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

    private fun getSampleCycleData(): CycleData {
        return CycleData(
            userName = "Ada",
            currentPhase = CyclePhase.FOLLICULAR,
            cycleDay = 10,
            cycleLength = 28,
            daysUntilNextPeriod = 18,
            daysUntilOvulation = 4,
            periodDaysRemaining = null,
            phaseProgress = 0.625f,
            phaseDaysRemaining = 4,
            phaseTotalDays = 9,
            nextPeriodDate = LocalDate.now().plusDays(18),
            ovulationDate = LocalDate.now().plusDays(4)
        )
    }
}

data class NextEventInfo(
    val title: String,
    val countdown: String,
    val date: String,
    val subtitle: String,
    val progress: Float
)