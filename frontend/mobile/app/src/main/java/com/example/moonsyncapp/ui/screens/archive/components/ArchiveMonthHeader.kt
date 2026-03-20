package com.example.moonsyncapp.ui.screens.archive.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moonsyncapp.data.model.MonthCycleInfo
import com.example.moonsyncapp.ui.theme.MoonSyncTheme
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * Month header component for Moon Archive timeline
 *
 * Features:
 * - Month name and year
 * - Entry count badge
 * - Optional cycle info (period days, fertile days, etc.)
 * - Sticky scroll behavior (handled by parent LazyColumn)
 *
 * Design: Minimal, clear hierarchy, brand colors
 */
@Composable
fun ArchiveMonthHeader(
    yearMonth: YearMonth,
    info: MonthCycleInfo?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 20.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Month and year
        Text(
            text = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )

        // Entry count badge
        if (info != null && info.loggedDays > 0) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${info.loggedDays}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (info.loggedDays == 1) "entry" else "entries",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * Variant with expanded cycle info
 * Shows period days, fertile days, ovulation
 */
@Composable
fun ArchiveMonthHeaderExpanded(
    yearMonth: YearMonth,
    info: MonthCycleInfo?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 20.dp, bottom = 12.dp)
    ) {
        // Month and year row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (info != null && info.loggedDays > 0) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${info.loggedDays} ${if (info.loggedDays == 1) "entry" else "entries"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Cycle info chips (optional)
        if (info != null && (info.periodDays > 0 || info.fertileWindowDays > 0 || info.ovulationDate != null)) {
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Period days
                if (info.periodDays > 0) {
                    MonthInfoChip(
                        emoji = "🩸",
                        text = "${info.periodDays}d",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }

                // Fertile window
                if (info.fertileWindowDays > 0) {
                    MonthInfoChip(
                        emoji = "🌸",
                        text = "${info.fertileWindowDays}d",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                // Ovulation indicator
                if (info.ovulationDate != null) {
                    MonthInfoChip(
                        emoji = "🥚",
                        text = "Ovulation",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Small info chip for month header
 */
@Composable
private fun MonthInfoChip(
    emoji: String,
    text: String,
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = tint.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = emoji, fontSize = 12.sp)
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = tint
            )
        }
    }
}

/**
 * Simplified header for months with no data
 */
@Composable
fun ArchiveMonthHeaderEmpty(
    yearMonth: YearMonth,
    modifier: Modifier = Modifier
) {
    Text(
        text = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 20.dp, bottom = 12.dp)
    )
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun ArchiveMonthHeaderPreview() {
    MoonSyncTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                ArchiveMonthHeader(
                    yearMonth = YearMonth.of(2025, 1),
                    info = MonthCycleInfo(
                        cycleNumbers = listOf(11, 12),
                        periodDays = 5,
                        loggedDays = 18,
                        fertileWindowDays = 6,
                        ovulationDate = java.time.LocalDate.of(2025, 1, 14)
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ArchiveMonthHeaderExpandedPreview() {
    MoonSyncTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                ArchiveMonthHeaderExpanded(
                    yearMonth = YearMonth.of(2025, 1),
                    info = MonthCycleInfo(
                        cycleNumbers = listOf(11, 12),
                        periodDays = 5,
                        loggedDays = 18,
                        fertileWindowDays = 6,
                        ovulationDate = java.time.LocalDate.of(2025, 1, 14)
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ArchiveMonthHeaderEmptyPreview() {
    MoonSyncTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                ArchiveMonthHeaderEmpty(
                    yearMonth = YearMonth.of(2024, 12)
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ArchiveMonthHeaderDarkPreview() {
    MoonSyncTheme(darkTheme = true) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                ArchiveMonthHeader(
                    yearMonth = YearMonth.of(2025, 1),
                    info = MonthCycleInfo(
                        cycleNumbers = emptyList(),
                        periodDays = 5,
                        loggedDays = 18,
                        fertileWindowDays = 6,
                        ovulationDate = null
                    )
                )
            }
        }
    }
}