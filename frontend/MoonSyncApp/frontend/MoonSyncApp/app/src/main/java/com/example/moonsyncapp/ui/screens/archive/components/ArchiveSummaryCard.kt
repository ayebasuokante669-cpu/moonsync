package com.example.moonsyncapp.ui.screens.archive.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moonsyncapp.data.model.ArchiveSummary
import com.example.moonsyncapp.ui.theme.MoonSyncTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Summary card component for Moon Archive
 *
 * Features:
 * - Total days logged
 * - Average cycle length
 * - Average period length
 * - Tracking duration
 * - Current cycle day
 *
 * Design: Prominent, informative, moon-themed
 */
@Composable
fun ArchiveSummaryCard(
    summary: ArchiveSummary,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "📊", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Your cycle at a glance",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Stats grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Days logged
                SummaryStatItem(
                    value = summary.totalLoggedDays.toString(),
                    label = "Days logged",
                    emoji = "📝",
                    modifier = Modifier.weight(1f)
                )

                // Average cycle
                SummaryStatItem(
                    value = summary.averageCycleLength?.let { "${it}d" } ?: "--",
                    label = "Avg cycle",
                    emoji = "🔄",
                    modifier = Modifier.weight(1f)
                )

                // Average period
                SummaryStatItem(
                    value = summary.averagePeriodLength?.let { "${it}d" } ?: "--",
                    label = "Avg period",
                    emoji = "🩸",
                    modifier = Modifier.weight(1f)
                )
            }

            // Tracking duration
            val trackingDuration = summary.trackingDuration
            if (trackingDuration != null) {
                Spacer(modifier = Modifier.height(16.dp))
                TrackingDurationChip(
                    duration = trackingDuration,
                    since = summary.trackingSince
                )
            }

            // Current cycle info
            if (summary.currentCycleDay != null) {
                Spacer(modifier = Modifier.height(12.dp))
                CurrentCycleChip(day = summary.currentCycleDay)
            }
        }
    }
}

/**
 * Individual stat item
 */
@Composable
private fun SummaryStatItem(
    value: String,
    label: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Tracking duration chip
 */
@Composable
private fun TrackingDurationChip(
    duration: String,
    since: LocalDate?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "🌙", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Tracking for $duration",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            since?.let { date ->
                Text(
                    text = " • Since ${date.format(DateTimeFormatter.ofPattern("MMM yyyy"))}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Current cycle day chip
 */
@Composable
private fun CurrentCycleChip(day: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "🌸", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Currently on day $day of your cycle",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

/**
 * Expanded summary card with additional stats
 */
@Composable
fun ArchiveSummaryCardExpanded(
    summary: ArchiveSummary,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "📊", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Moon Archive Statistics",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Your complete tracking overview",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Primary stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryStatItem(
                    value = summary.totalLoggedDays.toString(),
                    label = "Days logged",
                    emoji = "📝",
                    modifier = Modifier.weight(1f)
                )
                SummaryStatItem(
                    value = summary.averageCycleLength?.let { "${it}d" } ?: "--",
                    label = "Avg cycle",
                    emoji = "🔄",
                    modifier = Modifier.weight(1f)
                )
                SummaryStatItem(
                    value = summary.averagePeriodLength?.let { "${it}d" } ?: "--",
                    label = "Avg period",
                    emoji = "🩸",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Secondary stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryStatItem(
                    value = summary.totalSymptomLogs.toString(),
                    label = "Symptoms",
                    emoji = "💪",
                    modifier = Modifier.weight(1f)
                )
                SummaryStatItem(
                    value = summary.totalMoodLogs.toString(),
                    label = "Moods",
                    emoji = "💜",
                    modifier = Modifier.weight(1f)
                )
                SummaryStatItem(
                    value = summary.totalCycles.toString(),
                    label = "Cycles",
                    emoji = "🌙",
                    modifier = Modifier.weight(1f)
                )
            }

            // Cycle range info
            if (summary.shortestCycle != null && summary.longestCycle != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${summary.shortestCycle}d",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Shortest cycle",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${summary.longestCycle}d",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = "Longest cycle",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Footer info
            val trackingDuration = summary.trackingDuration
            if (trackingDuration != null) {
                Spacer(modifier = Modifier.height(16.dp))
                TrackingDurationChip(
                    duration = trackingDuration,
                    since = summary.trackingSince
                )
            }
        }
    }
}

/**
 * Minimal summary for empty/new users
 */
@Composable
fun ArchiveSummaryCardEmpty(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "📊", fontSize = 32.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No data yet",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Start logging to see your statistics",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun ArchiveSummaryCardPreview() {
    MoonSyncTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                ArchiveSummaryCard(
                    summary = ArchiveSummary(
                        totalCycles = 12,
                        trackingSince = LocalDate.of(2024, 10, 1),
                        averageCycleLength = 28,
                        averagePeriodLength = 5,
                        currentCycleDay = 14,
                        totalLoggedDays = 89,
                        totalSymptomLogs = 145,
                        totalMoodLogs = 112
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ArchiveSummaryCardExpandedPreview() {
    MoonSyncTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                ArchiveSummaryCardExpanded(
                    summary = ArchiveSummary(
                        totalCycles = 12,
                        trackingSince = LocalDate.of(2024, 10, 1),
                        averageCycleLength = 28,
                        averagePeriodLength = 5,
                        currentCycleDay = 14,
                        totalLoggedDays = 89,
                        totalSymptomLogs = 145,
                        totalMoodLogs = 112,
                        longestCycle = 32,
                        shortestCycle = 26
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ArchiveSummaryCardEmptyPreview() {
    MoonSyncTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                ArchiveSummaryCardEmpty()
            }
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ArchiveSummaryCardDarkPreview() {
    MoonSyncTheme(darkTheme = true) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                ArchiveSummaryCard(
                    summary = ArchiveSummary(
                        totalCycles = 8,
                        trackingSince = LocalDate.of(2024, 6, 1),
                        averageCycleLength = 29,
                        averagePeriodLength = 4,
                        currentCycleDay = 7,
                        totalLoggedDays = 45,
                        totalSymptomLogs = 78,
                        totalMoodLogs = 56
                    )
                )
            }
        }
    }
}