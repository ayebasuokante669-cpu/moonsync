package com.example.moonsyncapp.ui.screens.setup

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.moonsyncapp.data.OnboardingManager
import com.example.moonsyncapp.data.settings.SettingsManager

class SetupViewModelFactory(
    private val settingsManager: SettingsManager,
    private val onboardingManager: OnboardingManager,
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SetupViewModel::class.java)) {
            return SetupViewModel(settingsManager, onboardingManager, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}