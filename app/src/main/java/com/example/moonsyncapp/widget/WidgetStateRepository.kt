package com.example.moonsyncapp.widget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.example.moonsyncapp.data.CycleCalculator
import com.example.moonsyncapp.data.settings.SettingsManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Repository for managing widget state persistence and updates.
 *
 * IMPORTANT: Uses Glance's internal state system (updateAppWidgetState)
 * NOT a separate DataStore. This ensures the widget can read the data.
 *
 * Responsibilities:
 * - Serialize WidgetState to Glance preferences
 * - Calculate widget state from user settings
 * - Trigger widget updates when state changes
 * - Handle stale data detection
 */
class WidgetStateRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    companion object {
        /**
         * Key for storing serialized WidgetState in Glance preferences.
         * Must match the key used in MoonSyncWidget.kt
         */
        val WIDGET_STATE_KEY = stringPreferencesKey("widget_state")
    }

    // ==================== WRITE OPERATIONS ====================

    /**
     * Save widget state to ALL widget instances using Glance state.
     *
     * This uses updateAppWidgetState which writes to the same
     * PreferencesGlanceStateDefinition that the widget reads from.
     */
    private suspend fun saveStateToAllWidgets(state: WidgetState) {
        try {
            val stateJson = json.encodeToString(state)
            val manager = GlanceAppWidgetManager(context)

            // Update Small widgets
            val smallIds = manager.getGlanceIds(MoonSyncWidget::class.java)
            smallIds.forEach { glanceId ->
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[WIDGET_STATE_KEY] = stateJson
                    }
                }
            }

            // Trigger UI refresh for all widgets
            MoonSyncWidget().updateAll(context)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Update widget state and refresh all widget instances.
     *
     * Call this when:
     * - User logs a period
     * - User changes cycle settings
     * - App opens (to sync any changes)
     * - Midnight refresh (new day)
     *
     * @param state The new WidgetState to display
     */
    suspend fun updateWidgetState(state: WidgetState) {
        saveStateToAllWidgets(state)
    }

    /**
     * Clear all widget data - shows stale prompt.
     *
     * Use when:
     * - User logs out
     * - User resets app data
     * - Debugging/testing
     */
    suspend fun clearWidgetData() {
        val emptyState = WidgetState.empty()
        saveStateToAllWidgets(emptyState)
    }

    // ==================== CALCULATION OPERATIONS ====================

    /**
     * Calculate and update widget state from current settings.
     *
     * This is the primary method for keeping widget in sync with app data.
     * It reads from SettingsManager, calculates via CycleCalculator,
     * and updates the widget.
     *
     * @param settingsManager Source of user's cycle settings
     * @param showDetailedInfo User's privacy preference for widget
     */
    suspend fun refreshWidgetState(
        settingsManager: SettingsManager,
        showDetailedInfo: Boolean = false
    ) {
        try {
            val settings = settingsManager.getCurrentSettings()
            val today = LocalDate.now()

            // Check data freshness
            val isDataFresh = CycleCalculator.isDataFresh(
                lastPeriodStart = settings.lastPeriodStartDate,
                cycleLength = settings.cycleLength,
                today = today
            )

            if (!isDataFresh) {
                // Data is stale — show update prompt
                val staleState = WidgetState.stale().copy(
                    showDetailedInfo = showDetailedInfo,
                    lastUpdatedMillis = System.currentTimeMillis()
                )
                updateWidgetState(staleState)
                return
            }

            // Calculate cycle data
            val cycleDay = CycleCalculator.calculateCycleDay(
                lastPeriodStart = settings.lastPeriodStartDate,
                today = today,
                cycleLength = settings.cycleLength
            )

            val phase = CycleCalculator.determinePhase(
                cycleDay = cycleDay,
                periodDuration = settings.periodDuration,
                cycleLength = settings.cycleLength
            )

            val phaseProgress = CycleCalculator.phaseProgress(
                cycleDay = cycleDay,
                phase = phase,
                periodDuration = settings.periodDuration,
                cycleLength = settings.cycleLength
            )

            val daysUntilPeriod = CycleCalculator.daysUntilNextPeriod(
                cycleDay = cycleDay,
                cycleLength = settings.cycleLength
            )

            val daysUntilOvulation = CycleCalculator.daysUntilOvulation(
                cycleDay = cycleDay,
                cycleLength = settings.cycleLength
            )

            val nextPeriodDate = CycleCalculator.calculateNextPeriodDate(
                lastPeriodStart = settings.lastPeriodStartDate,
                cycleLength = settings.cycleLength,
                today = today
            )

            val ovulationDate = CycleCalculator.calculateOvulationDate(
                lastPeriodStart = settings.lastPeriodStartDate,
                cycleLength = settings.cycleLength,
                today = today
            )

            // Build widget state
            val widgetState = WidgetState(
                cycleDay = cycleDay,
                cycleLength = settings.cycleLength,
                phaseKey = phase.name,
                phaseProgress = phaseProgress,
                daysUntilPeriod = daysUntilPeriod,
                daysUntilOvulation = daysUntilOvulation,
                nextPeriodDateString = nextPeriodDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                ovulationDateString = ovulationDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                isDataFresh = true,
                showDetailedInfo = showDetailedInfo,
                lastUpdatedMillis = System.currentTimeMillis()
            )

            updateWidgetState(widgetState)

        } catch (e: Exception) {
            e.printStackTrace()
            // On error, show stale state
            updateWidgetState(WidgetState.stale())
        }
    }

    /**
     * Convenience method to refresh widget with current privacy setting preserved.
     *
     * @param settingsManager Source of user's cycle settings
     */
    suspend fun refreshWidgetStatePreservingPrivacy(settingsManager: SettingsManager) {
        // Get current privacy setting from SettingsManager
        val settings = settingsManager.getCurrentSettings()
        refreshWidgetState(
            settingsManager = settingsManager,
            showDetailedInfo = settings.widgetShowDetailedInfo
        )
    }

    // ==================== UTILITY ====================

    /**
     * Check if any widget instances are currently placed on home screen.
     * Checks all three widget receiver types.
     *
     * @return true if at least one widget instance exists (any size)
     */
    suspend fun hasActiveWidgets(): Boolean {
        return try {
            val manager = GlanceAppWidgetManager(context)
            val smallIds = manager.getGlanceIds(MoonSyncWidget::class.java)
            smallIds.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}