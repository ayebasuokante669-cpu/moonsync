package com.example.moonsyncapp.ui.screens.archive.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moonsyncapp.data.model.ArchiveFilters
import com.example.moonsyncapp.ui.theme.MoonSyncTheme

/**
 * Filter bottom sheet for Moon Archive
 *
 * Features:
 * - Content type filters (period, symptoms, moods, etc.)
 * - Active filter count
 * - Apply and Reset buttons
 * - Smooth animations
 *
 * Design: Clean, organized, easy to scan
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveFilterSheet(
    currentFilters: ArchiveFilters,
    onApplyFilters: (ArchiveFilters) -> Unit,
    onDismiss: () -> Unit
) {
    // Local state for editing filters
    var filters by remember { mutableStateOf(currentFilters) }

    // Track if filters have changed
    val hasChanges = filters != currentFilters

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            BottomSheetDefaults.DragHandle()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            FilterSheetHeader(
                activeFilterCount = filters.activeFilterCount,
                onClearAll = { filters = ArchiveFilters() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Filter sections
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .weight(1f, fill = false)
            ) {
                // Show content section
                FilterSectionTitle(title = "Show entries with:")

                Spacer(modifier = Modifier.height(16.dp))

                // Content type filters
                FilterToggleRow(
                    emoji = "🩸",
                    label = "Period days",
                    description = "Days marked as period",
                    isChecked = filters.showPeriodDays,
                    onCheckedChange = { filters = filters.copy(showPeriodDays = it) }
                )

                FilterToggleRow(
                    emoji = "💪",
                    label = "Symptoms",
                    description = "Physical symptoms logged",
                    isChecked = filters.showSymptoms,
                    onCheckedChange = { filters = filters.copy(showSymptoms = it) }
                )

                FilterToggleRow(
                    emoji = "💜",
                    label = "Moods",
                    description = "Emotional states logged",
                    isChecked = filters.showMoods,
                    onCheckedChange = { filters = filters.copy(showMoods = it) }
                )

                FilterToggleRow(
                    emoji = "💊",
                    label = "Medications",
                    description = "Pills or supplements taken",
                    isChecked = filters.showMedications,
                    onCheckedChange = { filters = filters.copy(showMedications = it) }
                )

                FilterToggleRow(
                    emoji = "📝",
                    label = "Notes",
                    description = "Days with written notes",
                    isChecked = filters.showNotes,
                    onCheckedChange = { filters = filters.copy(showNotes = it) }
                )

                FilterToggleRow(
                    emoji = "📎",
                    label = "Attachments",
                    description = "Voice notes or photos",
                    isChecked = filters.showAttachments,
                    onCheckedChange = { filters = filters.copy(showAttachments = it) }
                )

                // Future: Chatbot events (hidden for now)
                if (false) { // Change to true when chatbot is integrated
                    FilterToggleRow(
                        emoji = "🤖",
                        label = "Chatbot entries",
                        description = "Events from Momo chat",
                        isChecked = filters.showChatbotEvents,
                        onCheckedChange = { filters = filters.copy(showChatbotEvents = it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom buttons
            FilterSheetButtons(
                hasChanges = hasChanges,
                onReset = { filters = ArchiveFilters() },
                onApply = { onApplyFilters(filters) }
            )
        }
    }
}

/**
 * Filter sheet header with title and clear button
 */
@Composable
private fun FilterSheetHeader(
    activeFilterCount: Int,
    onClearAll: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Filter Entries 🔍",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (activeFilterCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$activeFilterCount active ${if (activeFilterCount == 1) "filter" else "filters"}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (activeFilterCount > 0) {
            TextButton(onClick = onClearAll) {
                Icon(
                    imageVector = Icons.Outlined.Clear,
                    contentDescription = "Clear all",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Clear all",
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * Section title for filter groups
 */
@Composable
private fun FilterSectionTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

/**
 * Individual filter toggle row
 */
@Composable
private fun FilterToggleRow(
    emoji: String,
    label: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onCheckedChange(!isChecked) },
        shape = RoundedCornerShape(14.dp),
        color = if (isChecked) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji icon
            Text(
                text = emoji,
                fontSize = 20.sp,
                modifier = Modifier.padding(end = 14.dp)
            )

            // Label and description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isChecked) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (isChecked) 0.8f else 0.6f
                    )
                )
            }

            // Checkbox
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            )
        }
    }
}

/**
 * Bottom action buttons
 */
@Composable
private fun FilterSheetButtons(
    hasChanges: Boolean,
    onReset: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Reset button
        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            enabled = hasChanges
        ) {
            Text(
                text = "Reset",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Apply button
        Button(
            onClick = onApply,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            enabled = hasChanges
        ) {
            Text(
                text = "Apply Filters",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Quick filter chips (alternative compact design)
 * For future use in main screen
 */
@Composable
fun ArchiveQuickFilterChips(
    filters: ArchiveFilters,
    onToggleFilter: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickFilterChip(
            emoji = "🩸",
            label = "Period",
            isSelected = filters.showPeriodDays,
            onClick = { onToggleFilter("period") }
        )
        QuickFilterChip(
            emoji = "💪",
            label = "Symptoms",
            isSelected = filters.showSymptoms,
            onClick = { onToggleFilter("symptoms") }
        )
        QuickFilterChip(
            emoji = "💜",
            label = "Moods",
            isSelected = filters.showMoods,
            onClick = { onToggleFilter("moods") }
        )
        QuickFilterChip(
            emoji = "📝",
            label = "Notes",
            isSelected = filters.showNotes,
            onClick = { onToggleFilter("notes") }
        )
    }
}

/**
 * Individual quick filter chip
 */
@Composable
private fun QuickFilterChip(
    emoji: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = emoji, fontSize = 14.sp)
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                )
            }
        },
        shape = RoundedCornerShape(14.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            selectedLabelColor = MaterialTheme.colorScheme.primary
        )
    )
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun ArchiveFilterSheetPreview() {
    MoonSyncTheme {
        Surface {
            var filters by remember { mutableStateOf(ArchiveFilters()) }

            Column {
                // Simulate bottom sheet content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    FilterSheetHeader(
                        activeFilterCount = 2,
                        onClearAll = { filters = ArchiveFilters() }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    FilterToggleRow(
                        emoji = "🩸",
                        label = "Period days",
                        description = "Days marked as period",
                        isChecked = true,
                        onCheckedChange = {}
                    )

                    FilterToggleRow(
                        emoji = "💪",
                        label = "Symptoms",
                        description = "Physical symptoms logged",
                        isChecked = false,
                        onCheckedChange = {}
                    )

                    FilterToggleRow(
                        emoji = "💜",
                        label = "Moods",
                        description = "Emotional states logged",
                        isChecked = true,
                        onCheckedChange = {}
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    FilterSheetButtons(
                        hasChanges = true,
                        onReset = {},
                        onApply = {}
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ArchiveQuickFilterChipsPreview() {
    MoonSyncTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                ArchiveQuickFilterChips(
                    filters = ArchiveFilters(
                        showPeriodDays = true,
                        showSymptoms = false,
                        showMoods = true,
                        showNotes = false
                    ),
                    onToggleFilter = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ArchiveFilterSheetDarkPreview() {
    MoonSyncTheme(darkTheme = true) {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                FilterSheetHeader(
                    activeFilterCount = 3,
                    onClearAll = {}
                )

                Spacer(modifier = Modifier.height(24.dp))

                FilterToggleRow(
                    emoji = "🩸",
                    label = "Period days",
                    description = "Days marked as period",
                    isChecked = true,
                    onCheckedChange = {}
                )

                FilterToggleRow(
                    emoji = "💊",
                    label = "Medications",
                    description = "Pills or supplements taken",
                    isChecked = false,
                    onCheckedChange = {}
                )

                Spacer(modifier = Modifier.height(24.dp))

                FilterSheetButtons(
                    hasChanges = true,
                    onReset = {},
                    onApply = {}
                )
            }
        }
    }
}