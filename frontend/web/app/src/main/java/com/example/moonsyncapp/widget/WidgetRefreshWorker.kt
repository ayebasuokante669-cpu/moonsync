package com.example.moonsyncapp.widget

import android.content.Context
import androidx.work.*
import com.example.moonsyncapp.data.settings.SettingsManager
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

/**
 * Background worker for refreshing widget data at midnight.
 *
 * Why midnight?
 * - Cycle day changes once per day (Day 12 → Day 13 at midnight)
 * - Phase may change at day boundaries
 * - Countdowns decrement daily
 * - No value in more frequent updates
 *
 * Battery efficiency:
 * - Uses WorkManager (respects Doze mode)
 * - Only runs if widgets are placed on home screen
 * - Single execution per day
 * - No wake locks or foreground service
 *
 * Additional triggers (handled elsewhere):
 * - App opens → MainActivity calls WidgetStateRepository
 * - Settings change → SettingsViewModel calls WidgetStateRepository
 * - Period logged → LoggingViewModel calls WidgetStateRepository
 *
 * This worker handles the "user didn't open app today" case.
 */
class WidgetRefreshWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        /**
         * Unique work name for preventing duplicate scheduling.
         */
        const val WORK_NAME = "moonsync_widget_midnight_refresh"

        /**
         * Tag for identifying widget refresh work.
         */
        const val WORK_TAG = "widget_refresh"

        /**
         * Schedule the midnight refresh worker.
         *
         * Call this when:
         * - App starts (MainActivity.onCreate)
         * - First widget is added (MoonSyncWidgetReceiver.onEnabled)
         * - After boot (if boot receiver is implemented)
         *
         * Uses ExistingPeriodicWorkPolicy.KEEP to avoid rescheduling
         * if already scheduled.
         *
         * @param context Application context
         */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(
                repeatInterval = 24,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setInitialDelay(
                    calculateDelayUntilMidnight(),
                    TimeUnit.MILLISECONDS
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false) // Run even on low battery
                        .build()
                )
                .addTag(WORK_TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        /**
         * Cancel the midnight refresh worker.
         *
         * Call this when:
         * - Last widget is removed (MoonSyncWidgetReceiver.onDisabled)
         * - User logs out
         * - User resets app data
         *
         * @param context Application context
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        /**
         * Check if the midnight refresh worker is currently scheduled.
         *
         * @param context Application context
         * @return true if worker is scheduled
         */
        suspend fun isScheduled(context: Context): Boolean {
            val workInfos = WorkManager.getInstance(context)
                .getWorkInfosForUniqueWork(WORK_NAME)
                .get()

            return workInfos.any {
                it.state == WorkInfo.State.ENQUEUED ||
                        it.state == WorkInfo.State.RUNNING
            }
        }

        /**
         * Calculate milliseconds until next midnight.
         *
         * Ensures the first execution happens at midnight,
         * then repeats every 24 hours.
         *
         * @return Milliseconds until next midnight
         */
        private fun calculateDelayUntilMidnight(): Long {
            val now = LocalDateTime.now()
            val nextMidnight = LocalDate.now().plusDays(1).atStartOfDay()

            // Add 1 minute buffer to ensure we're past midnight
            val targetTime = nextMidnight.plusMinutes(1)

            return ChronoUnit.MILLIS.between(now, targetTime)
        }
    }

    /**
     * Main worker execution.
     *
     * Steps:
     * 1. Check if any widgets are placed
     * 2. If no widgets, skip (save battery)
     * 3. Calculate new widget state from settings
     * 4. Update all widget instances
     *
     * @return Result.success() on completion, Result.retry() on transient error
     */
    override suspend fun doWork(): Result {
        return try {
            val repository = WidgetStateRepository(applicationContext)

            // Skip if no widgets are placed
            if (!repository.hasActiveWidgets()) {
                return Result.success()
            }

            // Refresh widget state from current settings
            val settingsManager = SettingsManager(applicationContext)
            repository.refreshWidgetStatePreservingPrivacy(settingsManager)

            Result.success()
        } catch (e: Exception) {
            // Log error but don't crash
            e.printStackTrace()

            // Retry on transient errors (up to 3 times)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                // Give up after 3 attempts
                Result.failure()
            }
        }
    }
}

/**
 * Helper object for managing widget refresh from various app entry points.
 *
 * Provides convenience methods to refresh widget state from
 * different parts of the app without importing repository directly.
 */
object WidgetRefreshHelper {

    /**
     * Refresh widget data immediately.
     *
     * ALWAYS saves data to DataStore (so future widgets have data).
     * Only updates placed widgets if they exist.
     *
     * Call from:
     * - MainActivity.onResume
     * - After period logging
     * - After settings change
     * - After setup completion
     *
     * @param context Application context
     */
    suspend fun refreshNow(context: Context) {
        try {
            val repository = WidgetStateRepository(context)
            val settingsManager = SettingsManager(context)

            // ✅ ALWAYS save data to DataStore (even if no widgets placed)
            // This ensures data is ready when user adds widget later
            repository.refreshWidgetStatePreservingPrivacy(settingsManager)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Refresh widget with specific privacy setting.
     *
     * Call when user changes the widget privacy toggle.
     *
     * @param context Application context
     * @param showDetailedInfo New privacy setting
     */
    suspend fun refreshWithPrivacy(context: Context, showDetailedInfo: Boolean) {
        try {
            val repository = WidgetStateRepository(context)
            val settingsManager = SettingsManager(context)

            // ✅ ALWAYS save data (removed hasActiveWidgets check)
            repository.refreshWidgetState(
                settingsManager = settingsManager,
                showDetailedInfo = showDetailedInfo
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Clear widget data and show stale prompt.
     *
     * Call when:
     * - User logs out
     * - User resets all data
     * - User deletes account
     *
     * @param context Application context
     */
    suspend fun clearAndReset(context: Context) {
        try {
            val repository = WidgetStateRepository(context)
            repository.clearWidgetData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Ensure midnight refresh worker is scheduled.
     *
     * Safe to call multiple times — uses KEEP policy.
     *
     * @param context Application context
     */
    fun ensureScheduled(context: Context) {
        WidgetRefreshWorker.schedule(context)
    }

    /**
     * Cancel all widget background work.
     *
     * @param context Application context
     */
    fun cancelAll(context: Context) {
        WidgetRefreshWorker.cancel(context)
    }
}