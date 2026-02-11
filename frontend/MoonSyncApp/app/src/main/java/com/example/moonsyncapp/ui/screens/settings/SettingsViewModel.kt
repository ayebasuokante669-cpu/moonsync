package com.example.moonsyncapp.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moonsyncapp.data.auth.AuthManager
import com.example.moonsyncapp.data.model.*
import com.example.moonsyncapp.data.settings.SettingsManager
import com.example.moonsyncapp.notifications.NotificationHelper
import com.example.moonsyncapp.ui.theme.ThemeManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import com.example.moonsyncapp.widget.WidgetRefreshHelper

// Navigation events
sealed class SettingsNavigationEvent {
    object NavigateToLogin : SettingsNavigationEvent()
    object NavigateToSetup : SettingsNavigationEvent()
    data class OpenUrl(val url: String) : SettingsNavigationEvent()
    data class OpenEmail(val email: String, val subject: String) : SettingsNavigationEvent()
    data class ShowMessage(val message: String) : SettingsNavigationEvent()
}

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val showEditNameDialog: Boolean = false,
    val editNameInput: String = "",
    val showPhotoOptions: Boolean = false,
    val showResetDataDialog: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val showDeleteAccountDialog: Boolean = false,
    val showPeriodTimePicker: Boolean = false,
    val showOvulationTimePicker: Boolean = false,
    val showPeriodEndTimePicker: Boolean = false,
    val showMedicationTimePicker: Boolean = false,
    val showDailyLogTimePicker: Boolean = false,
    // Cycle settings dialogs
    val showCycleLengthDialog: Boolean = false,
    val showPeriodDurationDialog: Boolean = false,
    val showLastPeriodDatePicker: Boolean = false,
    val editCycleLengthInput: Int = 28,
    val editPeriodDurationInput: Int = 5,
    val isLoading: Boolean = false,
    // Widget
    val showLockScreenInfo: Boolean = false
)

class SettingsViewModel(
    private val themeManager: ThemeManager? = null,
    private val authManager: AuthManager? = null,
    private val settingsManager: SettingsManager? = null,
    private val notificationHelper: NotificationHelper? = null,
    private val context: Context? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // Navigation events
    private val _navigationEvent = MutableSharedFlow<SettingsNavigationEvent>()
    val navigationEvent: SharedFlow<SettingsNavigationEvent> = _navigationEvent.asSharedFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        // Load theme preferences
        viewModelScope.launch {
            themeManager?.themeFlow?.collect { prefs ->
                _uiState.update { state ->
                    state.copy(
                        settings = state.settings.copy(
                            appearance = AppearanceSettings(
                                useDarkMode = prefs.useDarkMode,
                                useSystemTheme = prefs.useSystemTheme
                            )
                        )
                    )
                }
            }
        }

        // Load other settings
        viewModelScope.launch {
            settingsManager?.settings?.collect { userSettings ->
                _uiState.update { state ->
                    state.copy(
                        settings = state.settings.copy(
                            profile = state.settings.profile.copy(
                                name = userSettings.userName,
                                photoUri = userSettings.profilePhotoPath
                            ),
                            cycleSettings = CycleSettings(
                                cycleLength = userSettings.cycleLength,
                                periodDuration = userSettings.periodDuration,
                                lastPeriodStartDate = userSettings.lastPeriodStartDate
                            ),
                            notifications = NotificationSettings(
                                periodReminderEnabled = userSettings.periodReminderEnabled,
                                periodReminderTime = userSettings.periodReminderTime,
                                periodReminderDaysBefore = userSettings.periodReminderDaysBefore,
                                ovulationReminderEnabled = userSettings.ovulationReminderEnabled,
                                ovulationReminderTime = userSettings.ovulationReminderTime,
                                periodEndReminderEnabled = userSettings.periodEndReminderEnabled,
                                periodEndReminderTime = userSettings.periodEndReminderTime,
                                medicationReminderEnabled = userSettings.medicationReminderEnabled,
                                medicationReminderTime = userSettings.medicationReminderTime,
                                dailyLogReminderEnabled = userSettings.dailyLogReminderEnabled,
                                dailyLogReminderTime = userSettings.dailyLogReminderTime
                            ),
                            widget = WidgetSettings(
                                showDetailedInfo = userSettings.widgetShowDetailedInfo
                            )
                        )
                    )
                }
            }
        }

        // Load user email from AuthManager
        viewModelScope.launch {
            authManager?.currentUser?.collect { user ->
                user?.let {
                    _uiState.update { state ->
                        state.copy(
                            settings = state.settings.copy(
                                profile = state.settings.profile.copy(
                                    email = it.email
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    // ==================== PROFILE ====================

    fun showEditNameDialog() {
        _uiState.update {
            it.copy(
                showEditNameDialog = true,
                editNameInput = it.settings.profile.name
            )
        }
    }

    fun hideEditNameDialog() {
        _uiState.update { it.copy(showEditNameDialog = false, editNameInput = "") }
    }

    fun updateNameInput(name: String) {
        _uiState.update { it.copy(editNameInput = name) }
    }

    fun saveName() {
        val newName = _uiState.value.editNameInput.trim()
        if (newName.isNotBlank()) {
            viewModelScope.launch {
                settingsManager?.saveUserName(newName)
                _uiState.update { state ->
                    state.copy(
                        settings = state.settings.copy(
                            profile = state.settings.profile.copy(name = newName)
                        ),
                        showEditNameDialog = false,
                        editNameInput = ""
                    )
                }
            }
        }
    }

    fun showPhotoOptions() {
        _uiState.update { it.copy(showPhotoOptions = true) }
    }

    fun hidePhotoOptions() {
        _uiState.update { it.copy(showPhotoOptions = false) }
    }

    fun updateProfilePhoto(uri: Uri?) {
        viewModelScope.launch {
            if (uri != null && context != null) {
                // Copy to internal storage for persistence
                val savedPath = saveImageToInternalStorage(uri)
                settingsManager?.saveProfilePhotoPath(savedPath)

                _uiState.update { state ->
                    state.copy(
                        settings = state.settings.copy(
                            profile = state.settings.profile.copy(photoUri = savedPath)
                        ),
                        showPhotoOptions = false
                    )
                }
            }
        }
    }

    private suspend fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            context?.let { ctx ->
                val inputStream = ctx.contentResolver.openInputStream(uri)
                val file = File(ctx.filesDir, "profile_photo.jpg")
                file.outputStream().use { outputStream ->
                    inputStream?.copyTo(outputStream)
                }
                file.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun removeProfilePhoto() {
        viewModelScope.launch {
            // Delete file from storage
            context?.let { ctx ->
                val file = File(ctx.filesDir, "profile_photo.jpg")
                if (file.exists()) {
                    file.delete()
                }
            }

            settingsManager?.saveProfilePhotoPath(null)

            _uiState.update { state ->
                state.copy(
                    settings = state.settings.copy(
                        profile = state.settings.profile.copy(photoUri = null)
                    ),
                    showPhotoOptions = false
                )
            }
        }
    }

    // ==================== APPEARANCE ====================

    fun toggleDarkMode(enabled: Boolean) {
        _uiState.update { state ->
            state.copy(
                settings = state.settings.copy(
                    appearance = state.settings.appearance.copy(useDarkMode = enabled)
                )
            )
        }
        viewModelScope.launch {
            themeManager?.setDarkMode(enabled)
        }
    }

    fun toggleSystemTheme(enabled: Boolean) {
        _uiState.update { state ->
            state.copy(
                settings = state.settings.copy(
                    appearance = state.settings.appearance.copy(useSystemTheme = enabled)
                )
            )
        }
        viewModelScope.launch {
            themeManager?.setSystemTheme(enabled)
        }
    }

    // ==================== NOTIFICATIONS ====================

    fun togglePeriodReminder(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager?.savePeriodReminder(enabled)
            _uiState.update { state ->
                state.copy(
                    settings = state.settings.copy(
                        notifications = state.settings.notifications.copy(
                            periodReminderEnabled = enabled
                        )
                    )
                )
            }

            // Schedule or cancel notification
            if (enabled) {
                val settings = _uiState.value.settings
                val nextPeriodDate = calculateNextPeriodDate(
                    settings.cycleSettings.lastPeriodStartDate,
                    settings.cycleSettings.cycleLength
                )
                notificationHelper?.schedulePeriodReminder(
                    nextPeriodDate = nextPeriodDate,
                    daysBefore = settings.notifications.periodReminderDaysBefore,
                    time = settings.notifications.periodReminderTime
                )
            } else {
                notificationHelper?.cancelNotification(NotificationHelper.REQUEST_PERIOD)
            }
        }
    }

    private fun calculateNextPeriodDate(lastPeriodStart: LocalDate, cycleLength: Int): LocalDate {
        var nextDate = lastPeriodStart.plusDays(cycleLength.toLong())
        while (nextDate.isBefore(LocalDate.now()) || nextDate.isEqual(LocalDate.now())) {
            nextDate = nextDate.plusDays(cycleLength.toLong())
        }
        return nextDate
    }

    fun showPeriodTimePicker() {
        _uiState.update { it.copy(showPeriodTimePicker = true) }
    }

    fun hidePeriodTimePicker() {
        _uiState.update { it.copy(showPeriodTimePicker = false) }
    }

    fun setPeriodReminderTime(time: LocalTime) {
        viewModelScope.launch {
            settingsManager?.savePeriodReminder(
                enabled = _uiState.value.settings.notifications.periodReminderEnabled,
                time = time
            )
            _uiState.update { state ->
                state.copy(
                    settings = state.settings.copy(
                        notifications = state.settings.notifications.copy(
                            periodReminderTime = time
                        )
                    ),
                    showPeriodTimePicker = false
                )
            }

            // Reschedule notification with new time
            if (_uiState.value.settings.notifications.periodReminderEnabled) {
                togglePeriodReminder(true)
            }
        }
    }

    // Similar implementations for other notifications...
    fun toggleOvulationReminder(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager?.saveOvulationReminder(enabled)
            _uiState.update { state ->
                state.copy(
                    settings = state.settings.copy(
                        notifications = state.settings.notifications.copy(
                            ovulationReminderEnabled = enabled
                        )
                    )
                )
            }
        }
    }

    fun showOvulationTimePicker() {
        _uiState.update { it.copy(showOvulationTimePicker = true) }
    }

    fun hideOvulationTimePicker() {
        _uiState.update { it.copy(showOvulationTimePicker = false) }
    }

    fun setOvulationReminderTime(time: LocalTime) {
        viewModelScope.launch {
            settingsManager?.saveOvulationReminder(
                enabled = _uiState.value.settings.notifications.ovulationReminderEnabled,
                time = time
            )
            _uiState.update { state ->
                state.copy(
                    settings = state.settings.copy(
                        notifications = state.settings.notifications.copy(
                            ovulationReminderTime = time
                        )
                    ),
                    showOvulationTimePicker = false
                )
            }
        }
    }

    fun togglePeriodEndReminder(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager?.savePeriodEndReminder(enabled)
            _uiState.update { state ->
                state.copy(
                    settings = state.settings.copy(
                        notifications = state.settings.notifications.copy(
                            periodEndReminderEnabled = enabled
                        )
                    )
                )
            }
        }
    }

    fun showPeriodEndTimePicker() {
        _uiState.update { it.copy(showPeriodEndTimePicker = true) }
    }

    fun hidePeriodEndTimePicker() {
        _uiState.update { it.copy(showPeriodEndTimePicker = false) }
    }

    fun setPeriodEndReminderTime(time: LocalTime) {
        viewModelScope.launch {
            settingsManager?.savePeriodEndReminder(
                enabled = _uiState.value.settings.notifications.periodEndReminderEnabled,
                time = time
            )
            _uiState.update { state ->
                state.copy(
                    settings = state.settings.copy(
                        notifications = state.settings.notifications.copy(
                            periodEndReminderTime = time
                        )
                    ),
                    showPeriodEndTimePicker = false
                )
            }
        }
    }

    fun toggleMedicationReminder(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager?.saveMedicationReminder(enabled)
            _uiState.update { state ->
                state.copy(
                    settings = state.settings.copy(
                        notifications = state.settings.notifications.copy(
                            medicationReminderEnabled = enabled
                        )
                    )
                )
            }

            // Schedule daily medication reminder
            notificationHelper?.scheduleMedicationReminder(
                time = _uiState.value.settings.notifications.medicationReminderTime,
                enabled = enabled
            )
        }
    }

    fun showMedicationTimePicker() {
        _uiState.update { it.copy(showMedicationTimePicker = true) }
    }

    fun hideMedicationTimePicker() {
        _uiState.update { it.copy(showMedicationTimePicker = false) }
    }

    fun setMedicationReminderTime(time: LocalTime) {
        viewModelScope.launch {
            settingsManager?.saveMedicationReminder(
                enabled = _uiState.value.settings.notifications.medicationReminderEnabled,
                time = time
            )
            _uiState.update { state ->
                state.copy(
                    settings = state.settings.copy(
                        notifications = state.settings.notifications.copy(
                            medicationReminderTime = time
                        )
                    ),
                    showMedicationTimePicker = false
                )
            }

            if (_uiState.value.settings.notifications.medicationReminderEnabled) {
                notificationHelper?.scheduleMedicationReminder(time, true)
            }
        }
    }

    fun toggleDailyLogReminder(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager?.saveDailyLogReminder(enabled)
            _uiState.update { state ->
                state.copy(
                    settings = state.settings.copy(
                        notifications = state.settings.notifications.copy(
                            dailyLogReminderEnabled = enabled
                        )
                    )
                )
            }

            notificationHelper?.scheduleDailyLogReminder(
                time = _uiState.value.settings.notifications.dailyLogReminderTime,
                enabled = enabled
            )
        }
    }

    fun showDailyLogTimePicker() {
        _uiState.update { it.copy(showDailyLogTimePicker = true) }
    }

    fun hideDailyLogTimePicker() {
        _uiState.update { it.copy(showDailyLogTimePicker = false) }
    }

    fun setDailyLogReminderTime(time: LocalTime) {
        viewModelScope.launch {
            settingsManager?.saveDailyLogReminder(
                enabled = _uiState.value.settings.notifications.dailyLogReminderEnabled,
                time = time
            )
            _uiState.update { state ->
                state.copy(
                    settings = state.settings.copy(
                        notifications = state.settings.notifications.copy(
                            dailyLogReminderTime = time
                        )
                    ),
                    showDailyLogTimePicker = false
                )
            }

            if (_uiState.value.settings.notifications.dailyLogReminderEnabled) {
                notificationHelper?.scheduleDailyLogReminder(time, true)
            }
        }
    }

    // ==================== CYCLE SETTINGS ====================

    fun showCycleLengthDialog() {
        _uiState.update {
            it.copy(
                showCycleLengthDialog = true,
                editCycleLengthInput = it.settings.cycleSettings.cycleLength
            )
        }
    }

    fun hideCycleLengthDialog() {
        _uiState.update { it.copy(showCycleLengthDialog = false) }
    }

    fun updateCycleLengthInput(length: Int) {
        _uiState.update { it.copy(editCycleLengthInput = length.coerceIn(21, 45)) }
    }

    fun saveCycleLength() {
        val newLength = _uiState.value.editCycleLengthInput
        viewModelScope.launch {
            settingsManager?.saveCycleLength(newLength)
            _uiState.update { state ->
                state.copy(
                    settings = state.settings.copy(
                        cycleSettings = state.settings.cycleSettings.copy(
                            cycleLength = newLength
                        )
                    ),
                    showCycleLengthDialog = false
                )
            }

            // Refresh widget after cycle length change
            context?.let { WidgetRefreshHelper.refreshNow(it) }
        }
    }

    fun showPeriodDurationDialog() {
        _uiState.update {
            it.copy(
                showPeriodDurationDialog = true,
                editPeriodDurationInput = it.settings.cycleSettings.periodDuration
            )
        }
    }

    fun hidePeriodDurationDialog() {
        _uiState.update { it.copy(showPeriodDurationDialog = false) }
    }

    fun updatePeriodDurationInput(duration: Int) {
        _uiState.update { it.copy(editPeriodDurationInput = duration.coerceIn(2, 10)) }
    }

    fun savePeriodDuration() {
        val newDuration = _uiState.value.editPeriodDurationInput
        viewModelScope.launch {
            settingsManager?.savePeriodDuration(newDuration)
            _uiState.update { state ->
                state.copy(
                    settings = state.settings.copy(
                        cycleSettings = state.settings.cycleSettings.copy(
                            periodDuration = newDuration
                        )
                    ),
                    showPeriodDurationDialog = false
                )
            }

            //Refresh widget after period duration change
            context?.let { WidgetRefreshHelper.refreshNow(it) }
        }
    }

    fun showLastPeriodDatePicker() {
        _uiState.update { it.copy(showLastPeriodDatePicker = true) }
    }

    fun hideLastPeriodDatePicker() {
        _uiState.update { it.copy(showLastPeriodDatePicker = false) }
    }

    fun saveLastPeriodDate(date: LocalDate) {
        viewModelScope.launch {
            settingsManager?.saveLastPeriodStartDate(date)
            _uiState.update { state ->
                state.copy(
                    settings = state.settings.copy(
                        cycleSettings = state.settings.cycleSettings.copy(
                            lastPeriodStartDate = date
                        )
                    ),
                    showLastPeriodDatePicker = false
                )
            }

            // Refresh widget after period date change
            context?.let { WidgetRefreshHelper.refreshNow(it) }
        }
    }

    // ==================== DESTRUCTIVE ACTIONS ====================

    fun showResetDataDialog() {
        _uiState.update { it.copy(showResetDataDialog = true) }
    }

    fun hideResetDataDialog() {
        _uiState.update { it.copy(showResetDataDialog = false) }
    }

    fun resetAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showResetDataDialog = false) }

            // Clear all settings
            settingsManager?.resetAllSettings()

            // Cancel all notifications
            notificationHelper?.cancelAllNotifications()

            // NEW: Clear widget data and cancel worker
            context?.let {
                WidgetRefreshHelper.clearAndReset(it)
                WidgetRefreshHelper.cancelAll(it)
            }

            // Navigate to setup screen
            _navigationEvent.emit(SettingsNavigationEvent.NavigateToSetup)

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun showLogoutDialog() {
        _uiState.update { it.copy(showLogoutDialog = true) }
    }

    fun hideLogoutDialog() {
        _uiState.update { it.copy(showLogoutDialog = false) }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showLogoutDialog = false) }

            // Clear auth state
            authManager?.logout()

            // Cancel all notifications
            notificationHelper?.cancelAllNotifications()

            // Clear widget data and cancel worker
            context?.let {
                WidgetRefreshHelper.clearAndReset(it)
                WidgetRefreshHelper.cancelAll(it)
            }


            // Navigate to login
            _navigationEvent.emit(SettingsNavigationEvent.NavigateToLogin)

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun showDeleteAccountDialog() {
        _uiState.update { it.copy(showDeleteAccountDialog = true) }
    }

    fun hideDeleteAccountDialog() {
        _uiState.update { it.copy(showDeleteAccountDialog = false) }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showDeleteAccountDialog = false) }

            // Clear all local data
            settingsManager?.resetAllSettings()
            authManager?.clearAllData()

            // Cancel all notifications
            notificationHelper?.cancelAllNotifications()

            // Delete profile photo
            context?.let { ctx ->
                val file = File(ctx.filesDir, "profile_photo.jpg")
                if (file.exists()) file.delete()

                // Clear widget data and cancel worker
                WidgetRefreshHelper.clearAndReset(ctx)
                WidgetRefreshHelper.cancelAll(ctx)
            }

            // TODO: Call backend to delete account when ready
            // apiService.deleteAccount()

            // Navigate to login
            _navigationEvent.emit(SettingsNavigationEvent.NavigateToLogin)

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    // ==================== SUPPORT LINKS ====================

    fun openHelpSupport() {
        viewModelScope.launch {
            _navigationEvent.emit(
                SettingsNavigationEvent.OpenEmail(
                    email = "support@moonsyncapp.com",
                    subject = "MoonSync App - Help Request"
                )
            )
        }
    }

    fun openPrivacyPolicy() {
        viewModelScope.launch {
            _navigationEvent.emit(
                SettingsNavigationEvent.OpenUrl("https://moonsyncapp.com/privacy")
            )
        }
    }

    fun openTermsOfService() {
        viewModelScope.launch {
            _navigationEvent.emit(
                SettingsNavigationEvent.OpenUrl("https://moonsyncapp.com/terms")
            )
        }
    }

    fun openAbout() {
        viewModelScope.launch {
            _navigationEvent.emit(
                SettingsNavigationEvent.ShowMessage(
                    "MoonSync v1.0.0\nYour personal cycle companion 🌙"
                )
            )
        }
    }

    fun openRateApp() {
        viewModelScope.launch {
            _navigationEvent.emit(
                SettingsNavigationEvent.OpenUrl(
                    "https://play.google.com/store/apps/details?id=com.example.moonsyncapp"
                )
            )
        }
    }

    // ==================== WIDGET SETTINGS ====================

    /**
     * Toggle widget detailed info display.
     * Updates both settings and widget immediately.
     */
    fun toggleWidgetDetailedInfo(enabled: Boolean) {
        viewModelScope.launch {
            // Save preference
            settingsManager?.saveWidgetShowDetailedInfo(enabled)

            // Update UI state
            _uiState.update { state ->
                state.copy(
                    settings = state.settings.copy(
                        widget = state.settings.widget.copy(
                            showDetailedInfo = enabled
                        )
                    )
                )
            }

            // Refresh widget with new privacy setting
            context?.let { ctx ->
                WidgetRefreshHelper.refreshWithPrivacy(ctx, enabled)
            }
        }
    }

    /**
     * Show lock screen privacy info dialog.
     */
    fun showLockScreenPrivacyInfo() {
        viewModelScope.launch {
            _navigationEvent.emit(
                SettingsNavigationEvent.ShowMessage(
                    "💡 Privacy Tip\n\n" +
                            "Your widget may appear on the lock screen depending on " +
                            "your device settings.\n\n" +
                            "To control this:\n" +
                            "Settings → Display → Lock screen → Widgets"
                )
            )
        }
    }
}