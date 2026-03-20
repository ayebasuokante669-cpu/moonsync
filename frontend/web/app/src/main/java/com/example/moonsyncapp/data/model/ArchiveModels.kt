package com.example.moonsyncapp.data.model

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

// ============================================
// CORE ENTRY MODEL
// ============================================

/**
 * Represents a single day's complete archive entry.
 * Aggregates data from multiple sources for read-only display.
 *
 * Data sources:
 * - LoggingDataStore (symptoms, moods, notes, attachments)
 * - CalendarViewModel (period dates, fertile window, ovulation)
 * - CycleData (phase info, cycle day)
 * - Future: Chatbot confirmed events
 */
data class ArchiveDayEntry(
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDate,
    val cycleDay: Int? = null,
    val phase: CyclePhase? = null,
    val isPeriodDay: Boolean = false,
    val flowIntensity: FlowIntensity? = null,
    val isFertileDay: Boolean = false,
    val isOvulationDay: Boolean = false,
    val symptoms: List<QuickTapItem> = emptyList(),
    val moods: List<QuickTapItem> = emptyList(),
    val medications: List<MedicationEntry> = emptyList(),
    val notes: String? = null,
    val attachments: List<LogAttachment> = emptyList(),
    val chatbotEvents: List<ChatbotHealthEvent> = emptyList(),  // Future: from MomoChat
    val createdAt: Instant = Instant.now()
) {
    /**
     * Whether this day has any content worth displaying
     */
    val hasContent: Boolean
        get() = isPeriodDay ||
                isOvulationDay ||
                symptoms.isNotEmpty() ||
                moods.isNotEmpty() ||
                medications.isNotEmpty() ||
                !notes.isNullOrBlank() ||
                attachments.isNotEmpty() ||
                chatbotEvents.isNotEmpty()

    /**
     * Total number of logged items (for display badges)
     */
    val totalItems: Int
        get() = symptoms.size + moods.size + medications.size +
                attachments.size + chatbotEvents.size +
                (if (!notes.isNullOrBlank()) 1 else 0)

    /**
     * Whether this day has any attachments
     */
    val hasAttachments: Boolean
        get() = attachments.isNotEmpty()

    /**
     * Whether this day has voice notes
     */
    val hasVoiceNotes: Boolean
        get() = attachments.any { it is LogAttachment.VoiceNote }

    /**
     * Whether this day has photos
     */
    val hasPhotos: Boolean
        get() = attachments.any { it is LogAttachment.Photo }
}

// ============================================
// SUPPORTING ENUMS & MODELS
// ============================================

/**
 * Period flow intensity levels
 */
enum class FlowIntensity(
    val emoji: String,
    val label: String,
    val description: String
) {
    SPOTTING("💧", "Spotting", "Very light, occasional spots"),
    LIGHT("🩸", "Light", "Light flow"),
    MEDIUM("🩸🩸", "Medium", "Moderate flow"),
    HEAVY("🩸🩸🩸", "Heavy", "Heavy flow")
}

/**
 * Medication/supplement log entry
 */
data class MedicationEntry(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val emoji: String = "💊",
    val dosage: String? = null,
    val takenAt: Instant? = null,
    val notes: String? = null
)

/**
 * Chatbot-confirmed health event (future integration)
 * These are events that user confirmed via chatbot conversation
 */
data class ChatbotHealthEvent(
    val id: String = UUID.randomUUID().toString(),
    val type: ChatbotEventType,
    val description: String,
    val rawUserMessage: String? = null,  // Optional: what user said
    val confirmedAt: Instant = Instant.now(),
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Types of health events that can be confirmed via chatbot
 */
enum class ChatbotEventType(
    val emoji: String,
    val label: String
) {
    SYMPTOM_REPORTED("🤕", "Symptom reported"),
    MOOD_REPORTED("💜", "Mood reported"),
    MEDICATION_TAKEN("💊", "Medication taken"),
    PERIOD_STARTED("🩸", "Period started"),
    PERIOD_ENDED("✨", "Period ended"),
    PAIN_LEVEL("😣", "Pain level logged"),
    SLEEP_QUALITY("😴", "Sleep logged"),
    EXERCISE("🏃", "Exercise logged"),
    WATER_INTAKE("💧", "Hydration logged"),
    GENERAL_NOTE("📝", "Note added"),
    MEDICAL_ATTENTION("🏥", "Medical attention suggested")
}

// ============================================
// GROUPING MODELS
// ============================================

/**
 * Grouped period entry for collapsed period range display
 */
data class ArchivePeriodRange(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val durationDays: Int,
    val averageFlow: FlowIntensity? = null,
    val dailyFlows: List<FlowIntensity?> = emptyList(),
    val symptoms: List<QuickTapItem> = emptyList(),
    val moods: List<QuickTapItem> = emptyList(),
    val notes: List<String> = emptyList()
) {
    /**
     * Visual representation of flow across days
     * e.g., "🩸🩸🩸 → 🩸🩸 → 🩸"
     */
    val flowProgression: String
        get() = dailyFlows
            .filterNotNull()
            .map { it.emoji }
            .joinToString(" → ")
}

/**
 * Month grouping for timeline display
 */
data class ArchiveMonth(
    val yearMonth: YearMonth,
    val entries: List<ArchiveEntry>,
    val periodRanges: List<ArchivePeriodRange> = emptyList(),
    val info: MonthCycleInfo? = null
)

/**
 * Summary info for a single month
 */
data class MonthCycleInfo(
    val cycleNumbers: List<Int> = emptyList(),  // e.g., [11, 12] if month spans two cycles
    val periodDays: Int = 0,
    val loggedDays: Int = 0,
    val fertileWindowDays: Int = 0,
    val ovulationDate: LocalDate? = null
)

// ============================================
// TIMELINE ENTRY (SEALED CLASS FOR MIXED LIST)
// ============================================

/**
 * Sealed class for timeline items.
 * Enables heterogeneous list with type-safe rendering.
 */
sealed class ArchiveEntry {
    /**
     * Month section header
     */
    data class MonthHeader(
        val yearMonth: YearMonth,
        val info: MonthCycleInfo? = null
    ) : ArchiveEntry() {
        val key: String get() = "month_${yearMonth}"
    }

    /**
     * Single day entry
     */
    data class DayEntry(
        val entry: ArchiveDayEntry
    ) : ArchiveEntry() {
        val key: String get() = "day_${entry.date}"
    }

    /**
     * Collapsed period range (optional, for condensed view)
     */
    data class PeriodRange(
        val range: ArchivePeriodRange
    ) : ArchiveEntry() {
        val key: String get() = "period_${range.startDate}_${range.endDate}"
    }

    /**
     * Loading indicator for pagination
     */
    data object LoadingMore : ArchiveEntry() {
        val key: String get() = "loading_more"
    }
}

// ============================================
// SUMMARY & STATISTICS
// ============================================

/**
 * Overall archive summary statistics
 * Displayed at top of Moon Archive screen
 */
data class ArchiveSummary(
    val totalCycles: Int = 0,
    val trackingSince: LocalDate? = null,
    val averageCycleLength: Int? = null,
    val averagePeriodLength: Int? = null,
    val currentCycleDay: Int? = null,
    val totalLoggedDays: Int = 0,
    val totalSymptomLogs: Int = 0,
    val totalMoodLogs: Int = 0,
    val longestCycle: Int? = null,
    val shortestCycle: Int? = null
) {
    /**
     * Whether there's enough data to show meaningful stats
     */
    val hasData: Boolean
        get() = totalLoggedDays > 0 || trackingSince != null

    /**
     * Formatted tracking duration
     * e.g., "3 months", "1 year"
     */
    val trackingDuration: String?
        get() {
            val since = trackingSince ?: return null
            val now = LocalDate.now()
            val months = java.time.Period.between(since, now).toTotalMonths()
            return when {
                months < 1 -> "Less than a month"
                months == 1L -> "1 month"
                months < 12 -> "$months months"
                months == 12L -> "1 year"
                else -> {
                    val years = months / 12
                    val remainingMonths = months % 12
                    if (remainingMonths == 0L) "$years years"
                    else "$years years, $remainingMonths months"
                }
            }
        }
}

// ============================================
// FILTER OPTIONS
// ============================================

/**
 * Filter options for Moon Archive
 * All filters default to true (show everything)
 */
data class ArchiveFilters(
    // Content type filters
    val showPeriodDays: Boolean = true,
    val showSymptoms: Boolean = true,
    val showMoods: Boolean = true,
    val showMedications: Boolean = true,
    val showNotes: Boolean = true,
    val showAttachments: Boolean = true,
    val showChatbotEvents: Boolean = true,  // Future

    // Search
    val searchQuery: String = "",

    // Advanced filters (optional, for future)
    val selectedSymptoms: Set<String> = emptySet(),  // Filter by specific symptoms
    val selectedMoods: Set<String> = emptySet(),      // Filter by specific moods
    val dateRange: ClosedRange<LocalDate>? = null     // Filter by date range
) {
    /**
     * Whether any filter is actively restricting results
     */
    val isFiltering: Boolean
        get() = !showPeriodDays || !showSymptoms || !showMoods ||
                !showMedications || !showNotes || !showAttachments ||
                !showChatbotEvents || searchQuery.isNotBlank() ||
                selectedSymptoms.isNotEmpty() || selectedMoods.isNotEmpty() ||
                dateRange != null

    /**
     * Number of active content filters (for badge display)
     */
    val activeFilterCount: Int
        get() = listOf(
            !showPeriodDays,
            !showSymptoms,
            !showMoods,
            !showMedications,
            !showNotes,
            !showAttachments,
            !showChatbotEvents
        ).count { it } +
                (if (searchQuery.isNotBlank()) 1 else 0) +
                (if (selectedSymptoms.isNotEmpty()) 1 else 0) +
                (if (selectedMoods.isNotEmpty()) 1 else 0) +
                (if (dateRange != null) 1 else 0)

    /**
     * Reset all filters to default (show all)
     */
    fun reset(): ArchiveFilters = ArchiveFilters()

    /**
     * Check if a day entry passes all active filters
     */
    fun matches(entry: ArchiveDayEntry): Boolean {
        // Search query filter
        if (searchQuery.isNotBlank()) {
            val query = searchQuery.lowercase()
            val matchesSearch =
                entry.notes?.lowercase()?.contains(query) == true ||
                        entry.symptoms.any { it.label.lowercase().contains(query) } ||
                        entry.moods.any { it.label.lowercase().contains(query) } ||
                        entry.medications.any { it.name.lowercase().contains(query) } ||
                        entry.chatbotEvents.any { it.description.lowercase().contains(query) }

            if (!matchesSearch) return false
        }

        // Date range filter
        if (dateRange != null && entry.date !in dateRange) {
            return false
        }

        // Content type filters - entry must have at least one visible content type
        val hasVisibleContent =
            (showPeriodDays && entry.isPeriodDay) ||
                    (showSymptoms && entry.symptoms.isNotEmpty()) ||
                    (showMoods && entry.moods.isNotEmpty()) ||
                    (showMedications && entry.medications.isNotEmpty()) ||
                    (showNotes && !entry.notes.isNullOrBlank()) ||
                    (showAttachments && entry.attachments.isNotEmpty()) ||
                    (showChatbotEvents && entry.chatbotEvents.isNotEmpty())

        return hasVisibleContent
    }
}

// ============================================
// UI STATE HELPERS
// ============================================

/**
 * Selected month/year for month picker
 */
data class SelectedMonthYear(
    val year: Int,
    val month: Int  // 1-12
) {
    val yearMonth: YearMonth
        get() = YearMonth.of(year, month)

    companion object {
        fun now(): SelectedMonthYear {
            val now = YearMonth.now()
            return SelectedMonthYear(now.year, now.monthValue)
        }

        fun from(yearMonth: YearMonth): SelectedMonthYear {
            return SelectedMonthYear(yearMonth.year, yearMonth.monthValue)
        }
    }
}