package com.example.moonsyncapp.data.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * User profile data with phase info
 */
data class UserProfile(
    val name: String = "Ada",
    val email: String = "ada@gmail.com",
    val photoUri: String? = null,
    val currentCycleDay: Int = 12,
    val currentPhaseName: String = "Follicular",
    val currentPhaseEmoji: String = "🌱",
    val daysUntilNextPeriod: Int = 16
)

/**
 * Notification reminder settings
 */
data class NotificationSettings(
    val periodReminderEnabled: Boolean = true,
    val periodReminderTime: LocalTime = LocalTime.of(10, 0),
    val periodReminderDaysBefore: Int = 2,

    val ovulationReminderEnabled: Boolean = false,
    val ovulationReminderTime: LocalTime = LocalTime.of(10, 0),

    val periodEndReminderEnabled: Boolean = false,
    val periodEndReminderTime: LocalTime = LocalTime.of(10, 0),

    val medicationReminderEnabled: Boolean = false,
    val medicationReminderTime: LocalTime = LocalTime.of(9, 0),

    val dailyLogReminderEnabled: Boolean = false,
    val dailyLogReminderTime: LocalTime = LocalTime.of(19, 0)
)

/**
 * App appearance preferences
 */
data class AppearanceSettings(
    val useDarkMode: Boolean = false,
    val useSystemTheme: Boolean = true
)

/**
 * Cycle configuration (from setup)
 */
data class CycleSettings(
    val cycleLength: Int = 28,
    val periodDuration: Int = 5,
    val lastPeriodStartDate: LocalDate = LocalDate.now().minusDays(12)
)

/**
 * Complete settings state
 */
data class AppSettings(
    val profile: UserProfile = UserProfile(),
    val notifications: NotificationSettings = NotificationSettings(),
    val appearance: AppearanceSettings = AppearanceSettings(),
    val cycleSettings: CycleSettings = CycleSettings()
)