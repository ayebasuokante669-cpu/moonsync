package com.example.moonsyncapp.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.moonsyncapp.data.auth.AuthManager
import com.example.moonsyncapp.data.settings.SettingsManager
import com.example.moonsyncapp.notifications.NotificationHelper
import com.example.moonsyncapp.ui.theme.ThemeManager

class SettingsViewModelFactory(
    private val context: Context,
    private val themeManager: ThemeManager?,           // ← Nullable
    private val authManager: AuthManager?,             // ← Nullable
    private val settingsManager: SettingsManager?,     // ← Nullable
    private val notificationHelper: NotificationHelper? // ← Nullable
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(
                themeManager = themeManager,
                authManager = authManager,
                settingsManager = settingsManager,
                notificationHelper = notificationHelper,
                context = context
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}