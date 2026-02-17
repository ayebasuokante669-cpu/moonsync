package com.example.moonsyncapp.ui.screens.archive.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moonsyncapp.ui.theme.MoonSyncTheme

/**
 * Empty state for Moon Archive
 *
 * Displayed when:
 * - User has no logged data yet
 * - All data is filtered out
 * - Search returns no results
 *
 * Design: Warm, encouraging, brand-aligned
 */
@Composable
fun ArchiveEmptyState(
    modifier: Modifier = Modifier,
    isFiltered: Boolean = false,
    searchQuery: String = ""
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Moon emoji
            Text(
                text = "🌙",
                fontSize = 72.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Title
            Text(
                text = when {
                    searchQuery.isNotBlank() -> "No matches found"
                    isFiltered -> "No entries match filters"
                    else -> "Your archive begins here"
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle
            Text(
                text = when {
                    searchQuery.isNotBlank() -> "Try adjusting your search or filters"
                    isFiltered -> "Try changing your filter settings"
                    else -> "Start logging your daily experiences\nand watch your story unfold over time"
                },
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Hint card
            if (!isFiltered && searchQuery.isBlank()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EditNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Start logging",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Go to Daily Log to record your first entry",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Filter hint
            if (isFiltered && searchQuery.isBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { /* Will be handled by parent */ }) {
                    Text(
                        text = "Clear all filters",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Empty state variant for "no results" during search
 */
@Composable
fun ArchiveSearchEmptyState(
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    ArchiveEmptyState(
        modifier = modifier,
        isFiltered = false,
        searchQuery = searchQuery
    )
}

/**
 * Empty state variant for filtered results
 */
@Composable
fun ArchiveFilteredEmptyState(
    modifier: Modifier = Modifier
) {
    ArchiveEmptyState(
        modifier = modifier,
        isFiltered = true,
        searchQuery = ""
    )
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ArchiveEmptyStatePreview() {
    MoonSyncTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ArchiveEmptyState()
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ArchiveEmptyStateFilteredPreview() {
    MoonSyncTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ArchiveFilteredEmptyState()
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ArchiveEmptyStateSearchPreview() {
    MoonSyncTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ArchiveSearchEmptyState(searchQuery = "headache")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ArchiveEmptyStateDarkPreview() {
    MoonSyncTheme(darkTheme = true) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ArchiveEmptyState()
        }
    }
}