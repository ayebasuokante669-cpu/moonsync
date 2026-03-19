package com.example.moonsyncapp.ui.screens.archive.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moonsyncapp.data.model.*
import com.example.moonsyncapp.ui.theme.MoonSyncTheme
import java.time.LocalDate

/**
 * Day card component for Moon Archive timeline
 *
 * Features:
 * - Date display with cycle info
 * - Period/ovulation indicators
 * - Symptoms and moods preview (max 5-6 items)
 * - Notes preview (truncated)
 * - Attachment indicators
 * - Tap to view full detail
 *
 * Design: Clean, scannable, consistent with app style
 */
@Composable
fun ArchiveDayCard(
    entry: ArchiveDayEntry,
    formatDate: (LocalDate) -> String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Date, phase, indicators
            DayCardHeader(
                entry = entry,
                formatDate = formatDate
            )

            // Content: Symptoms, moods, notes
            if (entry.symptoms.isNotEmpty() || entry.moods.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                DayCardItems(
                    symptoms = entry.symptoms,
                    moods = entry.moods,
                    maxVisible = 6
                )
            }

            // Notes preview
            if (!entry.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                DayCardNotes(notes = entry.notes)
            }

            // Attachments and metadata footer
            if (entry.hasAttachments || entry.medications.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                DayCardFooter(entry = entry)
            }
        }
    }
}

/**
 * Header section: Date, cycle phase, period/ovulation indicators
 */
@Composable
private fun DayCardHeader(
    entry: ArchiveDayEntry,
    formatDate: (LocalDate) -> String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Period indicator dot
            if (entry.isPeriodDay) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Ovulation indicator
            if (entry.isOvulationDay) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Date
            Column {
                Text(
                    text = formatDate(entry.date),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Phase and cycle day (if available)
                if (entry.phase != null || entry.cycleDay != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        entry.phase?.let { phase ->
                            Text(
                                text = phase.displayName.replace(" Phase", ""),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (entry.phase != null && entry.cycleDay != null) {
                            Text(
                                text = "•",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        entry.cycleDay?.let { day ->
                            Text(
                                text = "Day $day",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Chevron indicator
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = "View details",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(22.dp)
        )
    }
}

/**
 * Items section: Symptoms and moods chips
 */
@Composable
private fun DayCardItems(
    symptoms: List<QuickTapItem>,
    moods: List<QuickTapItem>,
    maxVisible: Int
) {
    val allItems = symptoms + moods
    val visibleItems = allItems.take(maxVisible)
    val remainingCount = (allItems.size - maxVisible).coerceAtLeast(0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        visibleItems.forEach { item ->
            ItemChip(item = item)
        }

        // "+X more" indicator
        if (remainingCount > 0) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Text(
                    text = "+$remainingCount",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/**
 * Single item chip
 */
@Composable
private fun ItemChip(item: QuickTapItem) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = item.emoji, fontSize = 12.sp)
            Text(
                text = item.label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Notes preview section
 */
@Composable
private fun DayCardNotes(notes: String) {
    Text(
        text = notes,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        lineHeight = 18.sp
    )
}

/**
 * Footer section: Attachments and medications
 */
@Composable
private fun DayCardFooter(entry: ArchiveDayEntry) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Voice notes indicator
        if (entry.hasVoiceNotes) {
            FooterIndicator(
                emoji = "🎙️",
                count = entry.attachments.count { it is LogAttachment.VoiceNote }
            )
        }

        // Photos indicator
        if (entry.hasPhotos) {
            FooterIndicator(
                emoji = "📷",
                count = entry.attachments.count { it is LogAttachment.Photo }
            )
        }

        // Medications indicator
        if (entry.medications.isNotEmpty()) {
            FooterIndicator(
                emoji = "💊",
                count = entry.medications.size
            )
        }
    }
}

/**
 * Small indicator for attachments/medications
 */
@Composable
private fun FooterIndicator(
    emoji: String,
    count: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = emoji, fontSize = 14.sp)
        if (count > 1) {
            Text(
                text = "×$count",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun ArchiveDayCardPreview() {
    MoonSyncTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                ArchiveDayCard(
                    entry = ArchiveDayEntry(
                        date = LocalDate.now().minusDays(2),
                        cycleDay = 14,
                        phase = CyclePhase.FOLLICULAR,
                        isPeriodDay = false,
                        isOvulationDay = false,
                        symptoms = listOf(
                            QuickTapItem(emoji = "😴", label = "Tired", category = QuickTapCategory.PHYSICAL),
                            QuickTapItem(emoji = "🤕", label = "Headache", category = QuickTapCategory.PHYSICAL),
                            QuickTapItem(emoji = "🩸", label = "Cramps", category = QuickTapCategory.PHYSICAL)
                        ),
                        moods = listOf(
                            QuickTapItem(emoji = "😊", label = "Happy", category = QuickTapCategory.EMOTIONAL),
                            QuickTapItem(emoji = "😌", label = "Calm", category = QuickTapCategory.EMOTIONAL)
                        ),
                        notes = "Had a wonderful day! Went for a walk in the morning and felt energized. Took some time to rest in the afternoon.",
                        attachments = listOf(
                            LogAttachment.VoiceNote(durationMs = 45000)
                        ),
                        medications = listOf(
                            MedicationEntry(name = "Vitamin D")
                        )
                    ),
                    formatDate = { date ->
                        val today = LocalDate.now()
                        when (date) {
                            today -> "Today"
                            today.minusDays(1) -> "Yesterday"
                            else -> "Mon, Jan 27"
                        }
                    },
                    onClick = {}
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ArchiveDayCardPeriodPreview() {
    MoonSyncTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                ArchiveDayCard(
                    entry = ArchiveDayEntry(
                        date = LocalDate.now().minusDays(5),
                        cycleDay = 3,
                        phase = CyclePhase.MENSTRUAL,
                        isPeriodDay = true,
                        flowIntensity = FlowIntensity.MEDIUM,
                        symptoms = listOf(
                            QuickTapItem(emoji = "🩸", label = "Cramps", category = QuickTapCategory.PHYSICAL),
                            QuickTapItem(emoji = "😴", label = "Tired", category = QuickTapCategory.PHYSICAL)
                        ),
                        moods = listOf(
                            QuickTapItem(emoji = "😔", label = "Low", category = QuickTapCategory.EMOTIONAL)
                        ),
                        notes = "Period day 3, flow is medium. Taking it easy today."
                    ),
                    formatDate = { "Thu, Jan 23" },
                    onClick = {}
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ArchiveDayCardOvulationPreview() {
    MoonSyncTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                ArchiveDayCard(
                    entry = ArchiveDayEntry(
                        date = LocalDate.now().minusDays(14),
                        cycleDay = 14,
                        phase = CyclePhase.OVULATION,
                        isOvulationDay = true,
                        symptoms = listOf(
                            QuickTapItem(emoji = "✨", label = "Energetic", category = QuickTapCategory.PHYSICAL)
                        ),
                        moods = listOf(
                            QuickTapItem(emoji = "😊", label = "Happy", category = QuickTapCategory.EMOTIONAL),
                            QuickTapItem(emoji = "💪", label = "Confident", category = QuickTapCategory.EMOTIONAL)
                        ),
                        notes = null
                    ),
                    formatDate = { "Mon, Jan 14" },
                    onClick = {}
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ArchiveDayCardMinimalPreview() {
    MoonSyncTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                ArchiveDayCard(
                    entry = ArchiveDayEntry(
                        date = LocalDate.now().minusDays(1),
                        symptoms = listOf(
                            QuickTapItem(emoji = "😴", label = "Tired", category = QuickTapCategory.PHYSICAL)
                        ),
                        moods = emptyList(),
                        notes = null
                    ),
                    formatDate = { "Yesterday" },
                    onClick = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ArchiveDayCardDarkPreview() {
    MoonSyncTheme(darkTheme = true) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                ArchiveDayCard(
                    entry = ArchiveDayEntry(
                        date = LocalDate.now(),
                        cycleDay = 12,
                        phase = CyclePhase.FOLLICULAR,
                        symptoms = listOf(
                            QuickTapItem(emoji = "😴", label = "Tired", category = QuickTapCategory.PHYSICAL),
                            QuickTapItem(emoji = "🤕", label = "Headache", category = QuickTapCategory.PHYSICAL)
                        ),
                        moods = listOf(
                            QuickTapItem(emoji = "😊", label = "Happy", category = QuickTapCategory.EMOTIONAL)
                        ),
                        notes = "Feeling good overall, just a bit tired.",
                        attachments = listOf(
                            LogAttachment.Photo()
                        )
                    ),
                    formatDate = { "Today" },
                    onClick = {}
                )
            }
        }
    }
}