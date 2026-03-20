package com.example.moonsyncapp.data.model

import java.time.LocalDate
import java.time.Instant
import java.util.UUID

/**
 * Categories for quick-tap items
 */
enum class QuickTapCategory {
    PHYSICAL,
    EMOTIONAL,
    LIFESTYLE
}

/**
 * A quick-tap symptom/mood/lifestyle item
 */
data class QuickTapItem(
    val id: String = UUID.randomUUID().toString(),
    val emoji: String,
    val label: String,
    val category: QuickTapCategory,
    val isCustom: Boolean = false
)

/**
 * Attachment types for free-form logs
 */
sealed class LogAttachment {
    data class VoiceNote(
        val id: String = UUID.randomUUID().toString(),
        val uri: String? = null,
        val durationMs: Long = 0L,
        val createdAt: Instant = Instant.now()
    ) : LogAttachment()

    data class Photo(
        val id: String = UUID.randomUUID().toString(),
        val uri: String? = null,
        val description: String = "",
        val createdAt: Instant = Instant.now()
    ) : LogAttachment()
}

/**
 * A single day's log entry
 */
data class DailyLog(
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDate,
    val selectedItems: List<QuickTapItem> = emptyList(),
    val freeFormText: String = "",
    val attachments: List<LogAttachment> = emptyList(),
    val lockedAt: Instant? = null,
    val isEdited: Boolean = false,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    val isLocked: Boolean get() = lockedAt != null
    val isToday: Boolean get() = date == LocalDate.now()
    val hasContent: Boolean get() = selectedItems.isNotEmpty() || freeFormText.isNotBlank() || attachments.isNotEmpty()
}

/**
 * User's logging streak info
 */
data class LoggingStreak(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastLogDate: LocalDate? = null
) {
    val isActiveToday: Boolean get() = lastLogDate == LocalDate.now()
}

/**
 * Default quick-tap items
 */
object DefaultQuickTapItems {

    val physical = listOf(
        QuickTapItem(emoji = "😴", label = "Tired", category = QuickTapCategory.PHYSICAL),
        QuickTapItem(emoji = "🤕", label = "Headache", category = QuickTapCategory.PHYSICAL),
        QuickTapItem(emoji = "🩸", label = "Cramps", category = QuickTapCategory.PHYSICAL),
        QuickTapItem(emoji = "🤢", label = "Nausea", category = QuickTapCategory.PHYSICAL),
        QuickTapItem(emoji = "💆", label = "Back pain", category = QuickTapCategory.PHYSICAL),
        QuickTapItem(emoji = "🌡️", label = "Bloating", category = QuickTapCategory.PHYSICAL),
        QuickTapItem(emoji = "🍫", label = "Cravings", category = QuickTapCategory.PHYSICAL),
        QuickTapItem(emoji = "✨", label = "Energetic", category = QuickTapCategory.PHYSICAL),
        QuickTapItem(emoji = "💫", label = "Dizzy", category = QuickTapCategory.PHYSICAL),
        QuickTapItem(emoji = "🔥", label = "Hot flash", category = QuickTapCategory.PHYSICAL),
        QuickTapItem(emoji = "🥶", label = "Chills", category = QuickTapCategory.PHYSICAL),
        QuickTapItem(emoji = "🍽️", label = "No appetite", category = QuickTapCategory.PHYSICAL),
        QuickTapItem(emoji = "💪", label = "Strong", category = QuickTapCategory.PHYSICAL),
        QuickTapItem(emoji = "🤧", label = "Sick", category = QuickTapCategory.PHYSICAL),
        QuickTapItem(emoji = "🌊", label = "Tender", category = QuickTapCategory.PHYSICAL)
    )

    val emotional = listOf(
        QuickTapItem(emoji = "😊", label = "Happy", category = QuickTapCategory.EMOTIONAL),
        QuickTapItem(emoji = "😢", label = "Sad", category = QuickTapCategory.EMOTIONAL),
        QuickTapItem(emoji = "😤", label = "Irritable", category = QuickTapCategory.EMOTIONAL),
        QuickTapItem(emoji = "😰", label = "Anxious", category = QuickTapCategory.EMOTIONAL),
        QuickTapItem(emoji = "😌", label = "Calm", category = QuickTapCategory.EMOTIONAL),
        QuickTapItem(emoji = "🥰", label = "Loving", category = QuickTapCategory.EMOTIONAL),
        QuickTapItem(emoji = "😶", label = "Meh", category = QuickTapCategory.EMOTIONAL),
        QuickTapItem(emoji = "😔", label = "Low", category = QuickTapCategory.EMOTIONAL),
        QuickTapItem(emoji = "🤗", label = "Social", category = QuickTapCategory.EMOTIONAL),
        QuickTapItem(emoji = "🙈", label = "Withdrawn", category = QuickTapCategory.EMOTIONAL),
        QuickTapItem(emoji = "🥺", label = "Sensitive", category = QuickTapCategory.EMOTIONAL),
        QuickTapItem(emoji = "💪", label = "Confident", category = QuickTapCategory.EMOTIONAL),
        QuickTapItem(emoji = "😵", label = "Overwhelmed", category = QuickTapCategory.EMOTIONAL),
        QuickTapItem(emoji = "🌈", label = "Hopeful", category = QuickTapCategory.EMOTIONAL)
    )

    val lifestyle = listOf(
        QuickTapItem(emoji = "💤", label = "Slept well", category = QuickTapCategory.LIFESTYLE),
        QuickTapItem(emoji = "😫", label = "Slept poorly", category = QuickTapCategory.LIFESTYLE),
        QuickTapItem(emoji = "🏃", label = "Exercised", category = QuickTapCategory.LIFESTYLE),
        QuickTapItem(emoji = "💊", label = "Took meds", category = QuickTapCategory.LIFESTYLE),
        QuickTapItem(emoji = "🧘", label = "Self-care", category = QuickTapCategory.LIFESTYLE),
        QuickTapItem(emoji = "💦", label = "Hydrated", category = QuickTapCategory.LIFESTYLE),
        QuickTapItem(emoji = "☕", label = "Caffeine", category = QuickTapCategory.LIFESTYLE),
        QuickTapItem(emoji = "🍷", label = "Alcohol", category = QuickTapCategory.LIFESTYLE),
        QuickTapItem(emoji = "🌙", label = "Period day", category = QuickTapCategory.LIFESTYLE),
        QuickTapItem(emoji = "❤️", label = "Intimacy", category = QuickTapCategory.LIFESTYLE),
        QuickTapItem(emoji = "🧪", label = "Took test", category = QuickTapCategory.LIFESTYLE)
    )

    val all = physical + emotional + lifestyle
}