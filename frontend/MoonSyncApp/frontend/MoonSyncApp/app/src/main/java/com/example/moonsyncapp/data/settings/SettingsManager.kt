package com.example.moonsyncapp.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings_preferences"
)

data class UserSettings(
    // Profile
    val userName: String = "User",
    val profilePhotoPath: String? = null,

    // Cycle settings
    val cycleLength: Int = 28,
    val periodDuration: Int = 5,
    val lastPeriodStartDate: LocalDate = LocalDate.now().minusDays(14),

    // Notifications
    val periodReminderEnabled: Boolean = true,
    val periodReminderTime: LocalTime = LocalTime.of(9, 0),
    val periodReminderDaysBefore: Int = 2,

    val ovulationReminderEnabled: Boolean = false,
    val ovulationReminderTime: LocalTime = LocalTime.of(9, 0),

    val periodEndReminderEnabled: Boolean = false,
    val periodEndReminderTime: LocalTime = LocalTime.of(9, 0),

    val medicationReminderEnabled: Boolean = false,
    val medicationReminderTime: LocalTime = LocalTime.of(8, 0),

    val dailyLogReminderEnabled: Boolean = false,
    val dailyLogReminderTime: LocalTime = LocalTime.of(20, 0)
)

class SettingsManager(private val context: Context) {

    companion object {
        // Profile keys
        private val USER_NAME = stringPreferencesKey("user_name")
        private val PROFILE_PHOTO_PATH = stringPreferencesKey("profile_photo_path")

        // Cycle settings keys
        private val CYCLE_LENGTH = intPreferencesKey("cycle_length")
        private val PERIOD_DURATION = intPreferencesKey("period_duration")
        private val LAST_PERIOD_START_DATE = stringPreferencesKey("last_period_start_date")

        // Notification keys
        private val PERIOD_REMINDER_ENABLED = booleanPreferencesKey("period_reminder_enabled")
        private val PERIOD_REMINDER_TIME = stringPreferencesKey("period_reminder_time")
        private val PERIOD_REMINDER_DAYS_BEFORE = intPreferencesKey("period_reminder_days_before")

        private val OVULATION_REMINDER_ENABLED = booleanPreferencesKey("ovulation_reminder_enabled")
        private val OVULATION_REMINDER_TIME = stringPreferencesKey("ovulation_reminder_time")

        private val PERIOD_END_REMINDER_ENABLED = booleanPreferencesKey("period_end_reminder_enabled")
        private val PERIOD_END_REMINDER_TIME = stringPreferencesKey("period_end_reminder_time")

        private val MEDICATION_REMINDER_ENABLED = booleanPreferencesKey("medication_reminder_enabled")
        private val MEDICATION_REMINDER_TIME = stringPreferencesKey("medication_reminder_time")

        private val DAILY_LOG_REMINDER_ENABLED = booleanPreferencesKey("daily_log_reminder_enabled")
        private val DAILY_LOG_REMINDER_TIME = stringPreferencesKey("daily_log_reminder_time")

        private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
        private val DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE
    }

    private val dataStore = context.settingsDataStore

    // Flow of all settings
    val settings: Flow<UserSettings> = dataStore.data.map { prefs ->
        UserSettings(
            userName = prefs[USER_NAME] ?: "User",
            profilePhotoPath = prefs[PROFILE_PHOTO_PATH],

            cycleLength = prefs[CYCLE_LENGTH] ?: 28,
            periodDuration = prefs[PERIOD_DURATION] ?: 5,
            lastPeriodStartDate = prefs[LAST_PERIOD_START_DATE]?.let {
                LocalDate.parse(it, DATE_FORMATTER)
            } ?: LocalDate.now().minusDays(14),

            periodReminderEnabled = prefs[PERIOD_REMINDER_ENABLED] ?: true,
            periodReminderTime = prefs[PERIOD_REMINDER_TIME]?.let {
                LocalTime.parse(it, TIME_FORMATTER)
            } ?: LocalTime.of(9, 0),
            periodReminderDaysBefore = prefs[PERIOD_REMINDER_DAYS_BEFORE] ?: 2,

            ovulationReminderEnabled = prefs[OVULATION_REMINDER_ENABLED] ?: false,
            ovulationReminderTime = prefs[OVULATION_REMINDER_TIME]?.let {
                LocalTime.parse(it, TIME_FORMATTER)
            } ?: LocalTime.of(9, 0),

            periodEndReminderEnabled = prefs[PERIOD_END_REMINDER_ENABLED] ?: false,
            periodEndReminderTime = prefs[PERIOD_END_REMINDER_TIME]?.let {
                LocalTime.parse(it, TIME_FORMATTER)
            } ?: LocalTime.of(9, 0),

            medicationReminderEnabled = prefs[MEDICATION_REMINDER_ENABLED] ?: false,
            medicationReminderTime = prefs[MEDICATION_REMINDER_TIME]?.let {
                LocalTime.parse(it, TIME_FORMATTER)
            } ?: LocalTime.of(8, 0),

            dailyLogReminderEnabled = prefs[DAILY_LOG_REMINDER_ENABLED] ?: false,
            dailyLogReminderTime = prefs[DAILY_LOG_REMINDER_TIME]?.let {
                LocalTime.parse(it, TIME_FORMATTER)
            } ?: LocalTime.of(20, 0)
        )
    }

    // Profile settings
    suspend fun saveUserName(name: String) {
        dataStore.edit { it[USER_NAME] = name }
    }

    suspend fun saveProfilePhotoPath(path: String?) {
        dataStore.edit { prefs ->
            if (path != null) {
                prefs[PROFILE_PHOTO_PATH] = path
            } else {
                prefs.remove(PROFILE_PHOTO_PATH)
            }
        }
    }

    // Cycle settings
    suspend fun saveCycleLength(length: Int) {
        dataStore.edit { it[CYCLE_LENGTH] = length }
    }

    suspend fun savePeriodDuration(duration: Int) {
        dataStore.edit { it[PERIOD_DURATION] = duration }
    }

    suspend fun saveLastPeriodStartDate(date: LocalDate) {
        dataStore.edit { it[LAST_PERIOD_START_DATE] = date.format(DATE_FORMATTER) }
    }

    // Notification settings
    suspend fun savePeriodReminder(enabled: Boolean, time: LocalTime? = null) {
        dataStore.edit { prefs ->
            prefs[PERIOD_REMINDER_ENABLED] = enabled
            time?.let { prefs[PERIOD_REMINDER_TIME] = it.format(TIME_FORMATTER) }
        }
    }

    suspend fun saveOvulationReminder(enabled: Boolean, time: LocalTime? = null) {
        dataStore.edit { prefs ->
            prefs[OVULATION_REMINDER_ENABLED] = enabled
            time?.let { prefs[OVULATION_REMINDER_TIME] = it.format(TIME_FORMATTER) }
        }
    }

    suspend fun savePeriodEndReminder(enabled: Boolean, time: LocalTime? = null) {
        dataStore.edit { prefs ->
            prefs[PERIOD_END_REMINDER_ENABLED] = enabled
            time?.let { prefs[PERIOD_END_REMINDER_TIME] = it.format(TIME_FORMATTER) }
        }
    }

    suspend fun saveMedicationReminder(enabled: Boolean, time: LocalTime? = null) {
        dataStore.edit { prefs ->
            prefs[MEDICATION_REMINDER_ENABLED] = enabled
            time?.let { prefs[MEDICATION_REMINDER_TIME] = it.format(TIME_FORMATTER) }
        }
    }

    suspend fun saveDailyLogReminder(enabled: Boolean, time: LocalTime? = null) {
        dataStore.edit { prefs ->
            prefs[DAILY_LOG_REMINDER_ENABLED] = enabled
            time?.let { prefs[DAILY_LOG_REMINDER_TIME] = it.format(TIME_FORMATTER) }
        }
    }

    // Get current settings (one-time read)
    suspend fun getCurrentSettings(): UserSettings {
        return settings.first()
    }

    // Reset all settings
    suspend fun resetAllSettings() {
        dataStore.edit { it.clear() }
    }
}