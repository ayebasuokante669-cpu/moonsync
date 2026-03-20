package com.example.moonsyncapp.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.moonsyncapp.MainActivity
import com.example.moonsyncapp.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID_REMINDERS = "moonsync_reminders"
        const val CHANNEL_NAME = "Cycle Reminders"

        // Notification IDs
        const val NOTIFICATION_PERIOD_REMINDER = 1001
        const val NOTIFICATION_OVULATION_REMINDER = 1002
        const val NOTIFICATION_PERIOD_END_REMINDER = 1003
        const val NOTIFICATION_MEDICATION_REMINDER = 1004
        const val NOTIFICATION_DAILY_LOG_REMINDER = 1005

        // Request codes for alarms
        const val REQUEST_PERIOD = 2001
        const val REQUEST_OVULATION = 2002
        const val REQUEST_PERIOD_END = 2003
        const val REQUEST_MEDICATION = 2004
        const val REQUEST_DAILY_LOG = 2005

        // Intent extras
        const val EXTRA_NOTIFICATION_TYPE = "notification_type"
        const val EXTRA_NOTIFICATION_TITLE = "notification_title"
        const val EXTRA_NOTIFICATION_MESSAGE = "notification_message"
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_REMINDERS,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for your cycle, medications, and daily logging"
                enableLights(true)
                enableVibration(true)
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    // Check notification permission (Android 13+)
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    // Schedule period reminder
    fun schedulePeriodReminder(
        nextPeriodDate: LocalDate,
        daysBefore: Int,
        time: LocalTime
    ) {
        val reminderDate = nextPeriodDate.minusDays(daysBefore.toLong())
        val reminderDateTime = LocalDateTime.of(reminderDate, time)

        // Only schedule if in the future
        if (reminderDateTime.isAfter(LocalDateTime.now())) {
            scheduleNotification(
                requestCode = REQUEST_PERIOD,
                dateTime = reminderDateTime,
                title = "Period Coming Soon 🌙",
                message = "Your period is expected in $daysBefore days. Stay prepared!"
            )
        }
    }

    // Schedule ovulation reminder
    fun scheduleOvulationReminder(
        ovulationDate: LocalDate,
        time: LocalTime
    ) {
        val reminderDateTime = LocalDateTime.of(ovulationDate.minusDays(1), time)

        if (reminderDateTime.isAfter(LocalDateTime.now())) {
            scheduleNotification(
                requestCode = REQUEST_OVULATION,
                dateTime = reminderDateTime,
                title = "Fertile Window Alert 🌸",
                message = "Your fertile window is starting. Ovulation expected tomorrow."
            )
        }
    }

    // Schedule period end reminder
    fun schedulePeriodEndReminder(
        periodEndDate: LocalDate,
        time: LocalTime
    ) {
        val reminderDateTime = LocalDateTime.of(periodEndDate.minusDays(1), time)

        if (reminderDateTime.isAfter(LocalDateTime.now())) {
            scheduleNotification(
                requestCode = REQUEST_PERIOD_END,
                dateTime = reminderDateTime,
                title = "Period Ending Soon 🌷",
                message = "Your period should end tomorrow based on your cycle."
            )
        }
    }

    // Schedule daily medication reminder (repeating)
    fun scheduleMedicationReminder(time: LocalTime, enabled: Boolean) {
        if (enabled) {
            scheduleDailyRepeatingNotification(
                requestCode = REQUEST_MEDICATION,
                time = time,
                title = "Medication Reminder 💊",
                message = "Time to take your daily medication."
            )
        } else {
            cancelNotification(REQUEST_MEDICATION)
        }
    }

    // Schedule daily log reminder (repeating)
    fun scheduleDailyLogReminder(time: LocalTime, enabled: Boolean) {
        if (enabled) {
            scheduleDailyRepeatingNotification(
                requestCode = REQUEST_DAILY_LOG,
                time = time,
                title = "Daily Check-in 📝",
                message = "How are you feeling today? Log your symptoms and mood."
            )
        } else {
            cancelNotification(REQUEST_DAILY_LOG)
        }
    }

    private fun scheduleNotification(
        requestCode: Int,
        dateTime: LocalDateTime,
        title: String,
        message: String
    ) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_TYPE, requestCode)
            putExtra(EXTRA_NOTIFICATION_TITLE, title)
            putExtra(EXTRA_NOTIFICATION_MESSAGE, message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    // Fall back to inexact alarm
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Handle missing permission
            e.printStackTrace()
        }
    }

    private fun scheduleDailyRepeatingNotification(
        requestCode: Int,
        time: LocalTime,
        title: String,
        message: String
    ) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_TYPE, requestCode)
            putExtra(EXTRA_NOTIFICATION_TITLE, title)
            putExtra(EXTRA_NOTIFICATION_MESSAGE, message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculate next trigger time
        var triggerDateTime = LocalDateTime.of(LocalDate.now(), time)
        if (triggerDateTime.isBefore(LocalDateTime.now())) {
            triggerDateTime = triggerDateTime.plusDays(1)
        }

        val triggerTime = triggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        try {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun cancelNotification(requestCode: Int) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun cancelAllNotifications() {
        cancelNotification(REQUEST_PERIOD)
        cancelNotification(REQUEST_OVULATION)
        cancelNotification(REQUEST_PERIOD_END)
        cancelNotification(REQUEST_MEDICATION)
        cancelNotification(REQUEST_DAILY_LOG)
    }
}

// Broadcast Receiver for notifications
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_TYPE, 0)
        val title = intent.getStringExtra(NotificationHelper.EXTRA_NOTIFICATION_TITLE) ?: "MoonSync"
        val message = intent.getStringExtra(NotificationHelper.EXTRA_NOTIFICATION_MESSAGE) ?: ""

        // Create intent to open app
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)

        // Check permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(type, notification)
            }
        } else {
            notificationManager.notify(type, notification)
        }
    }
}