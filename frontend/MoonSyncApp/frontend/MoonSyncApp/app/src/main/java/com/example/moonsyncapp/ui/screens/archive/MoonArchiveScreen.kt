package com.example.moonsyncapp.ui.screens.archive

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.moonsyncapp.navigation.Routes
import com.example.moonsyncapp.ui.screens.archive.components.*
import java.time.YearMonth

/**
 * Moon Archive main screen
 *
 * Features:
 * - Read-only archive view
 * - Search functionality
 * - Content filters
 * - Month/year picker
 * - Lazy loading pagination
 * - Summary statistics
 * - No bottom navbar (back → Settings)
 *
 * Design: Clean, scannable, moon-themed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoonArchiveScreen(
    navController: NavController,
    viewModel: ArchiveViewModel = viewModel(
        factory = ArchiveViewModelFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Detect when to load more
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem >= totalItems - 5 && uiState.hasMoreData && !uiState.isLoadingMore
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && uiState.entries.isNotEmpty() && !uiState.isInitialLoading) {
            viewModel.loadMoreHistory()
        }
    }

    // Filter sheet
    if (uiState.showFilterSheet) {
        ArchiveFilterSheet(
            currentFilters = uiState.filters,
            onApplyFilters = viewModel::updateFilters,
            onDismiss = viewModel::hideFilterSheet
        )
    }

    // Month picker dialog
    if (uiState.showMonthPicker) {
        MonthYearPickerDialog(
            selectedMonthYear = uiState.selectedMonthYear,
            onMonthYearSelected = { monthYear ->
                viewModel.jumpToMonth(monthYear.yearMonth)
            },
            onDismiss = viewModel::hideMonthPicker
        )
    }

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
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.04f)
                        )
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            MoonArchiveHeader(
                searchQuery = uiState.filters.searchQuery,
                onSearchChange = viewModel::updateSearchQuery,
                onClearSearch = viewModel::clearSearch,
                activeFilterCount = uiState.filters.activeFilterCount,
                onFilterClick = viewModel::showFilterSheet,
                onMonthPickerClick = viewModel::showMonthPicker,
                onBackClick = { navController.navigateUp() }
            )

            // Content
            when {
                // Initial loading
                uiState.isInitialLoading -> {
                    MoonArchiveLoadingState()
                }

                // Error state
                uiState.errorMessage != null -> {
                    MoonArchiveErrorState(
                        message = uiState.errorMessage!!,
                        onRetry = viewModel::retryLoading
                    )
                }

                // Empty state (no data at all)
                uiState.entries.isEmpty() ||
                        uiState.entries.all { it is ArchiveEntry.MonthHeader } -> {
                    when {
                        uiState.filters.searchQuery.isNotBlank() -> {
                            ArchiveSearchEmptyState(searchQuery = uiState.filters.searchQuery)
                        }
                        uiState.filters.isFiltering -> {
                            ArchiveFilteredEmptyState()
                        }
                        else -> {
                            ArchiveEmptyState()
                        }
                    }
                }

                // Main content
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = 8.dp,
                            bottom = 24.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Summary card (at top)
                        item(key = "summary", contentType = "summary") {
                            if (uiState.summary.hasData) {
                                ArchiveSummaryCard(
                                    summary = uiState.summary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }

                        // Archive entries
                        items(
                            items = uiState.entries,
                            key = { entry ->
                                when (entry) {
                                    is ArchiveEntry.MonthHeader -> "month_${entry.yearMonth}"
                                    is ArchiveEntry.DayEntry -> "day_${entry.entry.date}"
                                    is ArchiveEntry.PeriodRange -> "period_${entry.range.startDate}"
                                    is ArchiveEntry.LoadingMore -> "loading_more"
                                }
                            },
                            contentType = { entry ->
                                when (entry) {
                                    is ArchiveEntry.MonthHeader -> "month_header"
                                    is ArchiveEntry.DayEntry -> "day_entry"
                                    is ArchiveEntry.PeriodRange -> "period_range"
                                    is ArchiveEntry.LoadingMore -> "loading"
                                }
                            }
                        ) { entry ->
                            when (entry) {
                                is ArchiveEntry.MonthHeader -> {
                                    ArchiveMonthHeader(
                                        yearMonth = entry.yearMonth,
                                        info = entry.info
                                    )
                                }

                                is ArchiveEntry.DayEntry -> {
                                    ArchiveDayCard(
                                        entry = entry.entry,
                                        formatDate = viewModel::formatDate,
                                        onClick = {
                                            val dateStr = viewModel.formatDateForNavigation(entry.entry.date)
                                            navController.navigate(Routes.archiveDetail(dateStr))
                                        }
                                    )
                                }

                                is ArchiveEntry.PeriodRange -> {
                                    // Future: Implement period range card
                                    // For now, skip or show as regular days
                                }

                                is ArchiveEntry.LoadingMore -> {
                                    LoadingMoreIndicator()
                                }
                            }
                        }

                        // Loading more at bottom
                        if (uiState.isLoadingMore) {
                            item(key = "loading_more_bottom", contentType = "loading") {
                                LoadingMoreIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Moon Archive header with search and controls
 */
@Composable
private fun MoonArchiveHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    activeFilterCount: Int,
    onFilterClick: () -> Unit,
    onMonthPickerClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var isSearchExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top row: Back, Title/Search, Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Settings",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            // Title or Search field
            AnimatedContent(
                targetState = isSearchExpanded,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith
                            fadeOut(animationSpec = tween(200))
                },
                modifier = Modifier.weight(1f),
                label = "search_animation"
            ) { expanded ->
                if (expanded) {
                    // Search field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        placeholder = {
                            Text(
                                "Search symptoms, moods, notes...",
                                fontSize = 14.sp
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Search,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = onClearSearch,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.Close,
                                        contentDescription = "Clear search",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    )
                } else {
                    // Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Moon Archive",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "🌙", fontSize = 20.sp)
                    }
                }
            }

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Search toggle
                IconButton(
                    onClick = {
                        isSearchExpanded = !isSearchExpanded
                        if (!isSearchExpanded) onClearSearch()
                    }
                ) {
                    Icon(
                        imageVector = if (isSearchExpanded)
                            Icons.Outlined.Close
                        else
                            Icons.Outlined.Search,
                        contentDescription = if (isSearchExpanded) "Close search" else "Search",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Filter button (with badge)
                BadgedBox(
                    badge = {
                        if (activeFilterCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Text(text = activeFilterCount.toString())
                            }
                        }
                    }
                ) {
                    IconButton(onClick = onFilterClick) {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
                            contentDescription = "Filter",
                            tint = if (activeFilterCount > 0)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Month picker button
                IconButton(onClick = onMonthPickerClick) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = "Jump to month",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // Search results info
        AnimatedVisibility(visible = searchQuery.isNotBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Text(
                    text = "Searching for \"$searchQuery\"",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
        }
    }
}

/**
 * Loading state with simple placeholders (no shimmer library needed)
 */
@Composable
private fun MoonArchiveLoadingState() {
    // Simple pulsing animation for loading
    val infiniteTransition = rememberInfiniteTransition(label = "loading_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary card placeholder
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
            ) {}
        }

        // Month header placeholder
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha))
            )
        }

        // Day cards placeholders
        items(4) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
            ) {}
        }
    }
}

/**
 * Error state
 */
@Composable
private fun MoonArchiveErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(text = "😔", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Try Again")
            }
        }
    }
}

/**
 * Loading more indicator (for pagination)
 */
@Composable
private fun LoadingMoreIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Month/Year picker dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthYearPickerDialog(
    selectedMonthYear: SelectedMonthYear,
    onMonthYearSelected: (SelectedMonthYear) -> Unit,
    onDismiss: () -> Unit
) {
    var year by remember { mutableStateOf(selectedMonthYear.year) }
    var month by remember { mutableStateOf(selectedMonthYear.month) }

    AlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Jump to Month",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Year selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = { year-- }) {
                        Icon(Icons.Outlined.ChevronLeft, contentDescription = "Previous year")
                    }
                    Text(
                        text = year.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.widthIn(min = 60.dp),
                        textAlign = TextAlign.Center
                    )
                    IconButton(onClick = { year++ }) {
                        Icon(Icons.Outlined.ChevronRight, contentDescription = "Next year")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Month grid (3x4)
                val months = listOf(
                    "Jan", "Feb", "Mar",
                    "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep",
                    "Oct", "Nov", "Dec"
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (row in 0..3) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (col in 0..2) {
                                val monthIndex = row * 3 + col
                                if (monthIndex < 12) {
                                    val monthNum = monthIndex + 1
                                    Surface(
                                        modifier = Modifier
                                            .size(72.dp, 48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { month = monthNum },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (month == monthNum)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = months[monthIndex],
                                                fontSize = 14.sp,
                                                fontWeight = if (month == monthNum)
                                                    FontWeight.Bold
                                                else
                                                    FontWeight.Normal,
                                                color = if (month == monthNum)
                                                    MaterialTheme.colorScheme.onPrimary
                                                else
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onMonthYearSelected(SelectedMonthYear(year, month))
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Go")
                    }
                }
            }
        }
    }
}