package com.example.moonsyncapp.ui.screens.setup

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moonsyncapp.data.OnboardingManager
import com.example.moonsyncapp.data.settings.SettingsManager
import com.example.moonsyncapp.widget.WidgetRefreshHelper
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Shared ViewModel for setup flow.
 * Persists user inputs to SettingsManager as they progress.
 */
class SetupViewModel(
    private val settingsManager: SettingsManager,
    private val onboardingManager: OnboardingManager,
    private val context: Context
) : ViewModel() {

    fun savePeriodStartDate(date: LocalDate) {
        viewModelScope.launch {
            settingsManager.saveLastPeriodStartDate(date)
        }
    }

    fun saveCycleLength(length: Int) {
        viewModelScope.launch {
            settingsManager.saveCycleLength(length)
        }
    }

    fun savePeriodDuration(duration: Int) {
        viewModelScope.launch {
            settingsManager.savePeriodDuration(duration)
        }
    }

    fun completeSetup() {
        viewModelScope.launch {
            // Mark setup as complete
            onboardingManager.completeSetup()

            // Refresh widget with new data
            WidgetRefreshHelper.refreshNow(context)
        }
    }
}