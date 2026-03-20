package com.example.moonsyncapp.ui.screens.archive

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moonsyncapp.data.model.*
import com.example.moonsyncapp.ui.theme.MoonSyncTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

/**
 * Archive Detail Screen - Read-only view of a single day
 *
 * Features:
 * - Full date and cycle information
 * - Complete list of symptoms, moods, medications
 * - Full notes (not truncated)
 * - Attachment details
 * - NO editing or deleting (read-only)
 * - Back button → Moon Archive
 *
 * Design: Clean, comprehensive, easy to read
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveDetailScreen(
    dateString: String,
    navController: NavController,
    viewModel: ArchiveViewModel = viewModel(
        factory = ArchiveViewModelFactory(LocalContext.current)
    )
) {
    // Parse the date from navigation parameter
    val date = remember {
        viewModel.parseDateFromNavigation(dateString) ?: LocalDate.now()
    }

    // Get the entry for this date
    val entry = remember(date) {
        viewModel.getEntryForDate(date)
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.04f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar (read-only - no edit button)
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Moon Archive"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0f)
                )
            )

            // Content
            if (entry == null) {
                // No entry for this date
                DetailEmptyState(date = date)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 32.dp)
                ) {
                    // Date header
                    DetailDateHeader(
                        date = date,
                        entry = entry,
                        formatDate = viewModel::formatDateLong
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Phase card (if available)
                    entry.phase?.let { phase ->
                        DetailPhaseCard(
                            phase = phase,
                            cycleDay = entry.cycleDay
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Period info (if period day)
                    if (entry.isPeriodDay) {
                        DetailPeriodCard(
                            flowIntensity = entry.flowIntensity
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Ovulation indicator (if ovulation day)
                    if (entry.isOvulationDay) {
                        DetailOvulationCard()
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Fertile window indicator
                    if (entry.isFertileDay && !entry.isOvulationDay) {
                        DetailFertileCard()
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Physical symptoms
                    if (entry.symptoms.isNotEmpty()) {
                        DetailSection(
                            title = "Physical Symptoms",
                            emoji = "💪",
                            items = entry.symptoms
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Emotional state
                    if (entry.moods.isNotEmpty()) {
                        DetailSection(
                            title = "Emotional State",
                            emoji = "💜",
                            items = entry.moods
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Medications
                    if (entry.medications.isNotEmpty()) {
                        DetailMedicationsSection(
                            medications = entry.medications
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Notes (full text)
                    if (!entry.notes.isNullOrBlank()) {
                        DetailNotesSection(
                            notes = entry.notes
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Attachments
                    if (entry.attachments.isNotEmpty()) {
                        DetailAttachmentsSection(
                            attachments = entry.attachments
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Future: Chatbot events
                    if (entry.chatbotEvents.isNotEmpty()) {
                        DetailChatbotSection(
                            events = entry.chatbotEvents
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Footer metadata
                    Spacer(modifier = Modifier.height(24.dp))
                    DetailFooter(entry = entry)
                }
            }
        }
    }
}

/**
 * Date header with full date and cycle info
 */
@Composable
private fun DetailDateHeader(
    date: LocalDate,
    entry: ArchiveDayEntry,
    formatDate: (LocalDate) -> String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Day of week
        Text(
            text = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )

        // Full date
        Text(
            text = formatDate(date),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        // Cycle day
        if (entry.cycleDay != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "Day ${entry.cycleDay} of your cycle",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/**
 * Phase information card
 */
@Composable
private fun DetailPhaseCard(
    phase: CyclePhase,
    cycleDay: Int?
) {
    val fillColor = PhaseColors.getFillColor(phase)
    val borderColor = PhaseColors.getBorderColor(phase)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = fillColor,
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(borderColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (phase) {
                        CyclePhase.MENSTRUAL -> "🩸"
                        CyclePhase.FOLLICULAR -> "🌱"
                        CyclePhase.OVULATION -> "🥚"
                        CyclePhase.LUTEAL -> "🌙"
                    },
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = phase.displayName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = borderColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = phase.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Period day card
 */
@Composable
private fun DetailPeriodCard(
    flowIntensity: FlowIntensity?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🩸", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Period Day",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.tertiary
                )
                if (flowIntensity != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = flowIntensity.emoji,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${flowIntensity.label} flow",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Ovulation day card
 */
@Composable
private fun DetailOvulationCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🥚", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Ovulation Day",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Peak fertility window",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Fertile window card
 */
@Composable
private fun DetailFertileCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🌸", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Fertile Window",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "High chance of conception",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Generic section for symptoms and moods
 */
@Composable
private fun DetailSection(
    title: String,
    emoji: String,
    items: List<QuickTapItem>
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "${items.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { item ->
                            DetailItemChip(
                                item = item,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Fill empty space if odd number
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual item chip (larger than in card view)
 */
@Composable
private fun DetailItemChip(
    item: QuickTapItem,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = item.emoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = item.label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Medications section
 */
@Composable
private fun DetailMedicationsSection(
    medications: List<MedicationEntry>
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(text = "💊", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Medications",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                medications.forEach { medication ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = medication.emoji, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = medication.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (medication.dosage != null) {
                                Text(
                                    text = medication.dosage,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Notes section (full text)
 */
@Composable
private fun DetailNotesSection(notes: String) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(text = "📝", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Notes",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Text(
                text = notes,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

/**
 * Attachments section
 */
@Composable
private fun DetailAttachmentsSection(
    attachments: List<LogAttachment>
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(text = "📎", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Attachments",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                attachments.forEach { attachment ->
                    DetailAttachmentItem(attachment = attachment)
                }
            }
        }
    }
}

/**
 * Single attachment item
 */
@Composable
private fun DetailAttachmentItem(attachment: LogAttachment) {
    val (emoji, label, sublabel) = when (attachment) {
        is LogAttachment.VoiceNote -> {
            val seconds = attachment.durationMs / 1000
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            val duration = if (minutes > 0) {
                "${minutes}m ${remainingSeconds}s"
            } else {
                "${seconds}s"
            }
            Triple("🎙️", "Voice note", duration)
        }
        is LogAttachment.Photo -> {
            Triple("📷", "Photo", attachment.description.ifBlank { "Image attachment" })
        }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = sublabel,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Outlined.PlayArrow,
                contentDescription = "Play/View",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * Chatbot events section (future)
 */
@Composable
private fun DetailChatbotSection(
    events: List<ChatbotHealthEvent>
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(text = "🤖", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Momo Chat Events",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                events.forEach { event ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text(text = event.type.emoji, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.type.label,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = event.description,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Footer with metadata
 */
@Composable
private fun DetailFooter(entry: ArchiveDayEntry) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            thickness = 0.5.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Logged on ${entry.createdAt.atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
                .format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        if (entry.totalItems > 0) {
            Text(
                text = "${entry.totalItems} total items recorded",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Empty state when no data for date
 */
@Composable
private fun DetailEmptyState(date: LocalDate) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🌙", fontSize = 56.sp)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "No entries for this day",
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}