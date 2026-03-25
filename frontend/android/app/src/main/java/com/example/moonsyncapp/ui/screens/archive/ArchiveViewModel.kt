package com.example.moonsyncapp.ui.screens.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moonsyncapp.data.ApiClient
import com.example.moonsyncapp.data.LoggingDataStore
import com.example.moonsyncapp.data.model.*
import com.example.moonsyncapp.ui.viewmodels.CalendarDisplayData
import com.example.moonsyncapp.ui.viewmodels.CalendarViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

/**
 * UI State for Moon Archive screen
 */
data class ArchiveUiState(
    // Loading states
    val isInitialLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,

    // Data
    val summary: ArchiveSummary = ArchiveSummary(),
    val entries: List<ArchiveEntry> = emptyList(),

    // Filters & Search
    val filters: ArchiveFilters = ArchiveFilters(),
    val showFilterSheet: Boolean = false,

    // Month picker
    val showMonthPicker: Boolean = false,
    val selectedMonthYear: SelectedMonthYear = SelectedMonthYear.now(),

    // Pagination
    val oldestLoadedMonth: YearMonth = YearMonth.now(),
    val hasMoreData: Boolean = true,

    // Detail navigation
    val selectedDate: LocalDate? = null
)

/**
 * ViewModel for Moon Archive
 *
 * Responsibilities:
 * - Load data from multiple sources (LoggingDataStore, CalendarViewModel, CycleData)
 * - Filter and search entries
 * - Handle pagination (lazy loading)
 * - Format dates for display
 * - Manage month picker state
 *
 * Read-only design: No editing or deleting functionality
 */
class ArchiveViewModel(
    private val loggingDataStore: LoggingDataStore,
    private val calendarViewModel: CalendarViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()

    // Cache of all data sources
    private var allDailyLogs: List<DailyLog> = emptyList()
    private var cachedCalendarDisplayData: CalendarDisplayData? = null
    private var cachedCycleData: CycleData? = null

    // Summary stats fetched from backend (supplements local logs)
    private var backendSummaryJson: JSONObject? = null

    init {
        loadInitialData()
        observeCalendarData()
        fetchBackendHistory()
    }

    private fun fetchBackendHistory() {
        val uid = ApiClient.userId ?: return
        viewModelScope.launch {
            try {
                val result = ApiClient.get("/history/$uid")
                result.onSuccess { json ->
                    backendSummaryJson = runCatching { JSONObject(json) }.getOrNull()
                }
            } catch (_: Exception) {
                // Keep local data only
            }
        }
    }

    // ==================== DATA LOADING ====================

    /**
     * Observe calendar and cycle data from CalendarViewModel
     */
    private fun observeCalendarData() {
        viewModelScope.launch {
            calendarViewModel.calendarDisplayData.collect { displayData ->
                cachedCalendarDisplayData = displayData
            }
        }

        viewModelScope.launch {
            calendarViewModel.cycleData.collect { data ->
                cachedCycleData = data
            }
        }
    }

    /**
     * Load initial 3 months of data from all sources
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isInitialLoading = true, errorMessage = null) }

            try {
                // Load from all data sources
                loggingDataStore.getAllLogs().collect { logs ->
                    allDailyLogs = logs

                    // Process and display initial 3 months
                    val now = YearMonth.now()
                    val threeMonthsAgo = now.minusMonths(2)

                    val entries = processDataToEntries(
                        fromMonth = threeMonthsAgo,
                        toMonth = now,
                        filters = _uiState.value.filters
                    )

                    val summary = calculateSummary()

                    _uiState.update { state ->
                        state.copy(
                            isInitialLoading = false,
                            entries = entries,
                            summary = summary,
                            oldestLoadedMonth = threeMonthsAgo,
                            hasMoreData = hasDataBefore(threeMonthsAgo)
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isInitialLoading = false,
                        errorMessage = "Couldn't load your archive. Please try again."
                    )
                }
            }
        }
    }

    /**
     * Load more historical data (3 more months)
     */
    fun loadMoreHistory() {
        val currentState = _uiState.value
        if (currentState.isLoadingMore || !currentState.hasMoreData) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }

            val newOldestMonth = currentState.oldestLoadedMonth.minusMonths(3)

            val newEntries = processDataToEntries(
                fromMonth = newOldestMonth,
                toMonth = currentState.oldestLoadedMonth.minusMonths(1),
                filters = currentState.filters
            )

            _uiState.update { state ->
                state.copy(
                    isLoadingMore = false,
                    entries = newEntries + state.entries,  // Prepend older data
                    oldestLoadedMonth = newOldestMonth,
                    hasMoreData = hasDataBefore(newOldestMonth)
                )
            }
        }
    }

    /**
     * Check if there's data before a given month
     */
    private fun hasDataBefore(month: YearMonth): Boolean {
        return allDailyLogs.any {
            YearMonth.from(it.date) < month
        }
    }

    /**
     * Jump to a specific month
     */
    fun jumpToMonth(yearMonth: YearMonth) {
        viewModelScope.launch {
            _uiState.update { it.copy(isInitialLoading = true, showMonthPicker = false) }

            val now = YearMonth.now()
            val fromMonth = if (yearMonth > now.minusMonths(2)) {
                now.minusMonths(2)
            } else {
                yearMonth
            }

            val toMonth = if (yearMonth > now) now else yearMonth.plusMonths(2)

            val entries = processDataToEntries(
                fromMonth = fromMonth,
                toMonth = toMonth,
                filters = _uiState.value.filters
            )

            _uiState.update { state ->
                state.copy(
                    isInitialLoading = false,
                    entries = entries,
                    oldestLoadedMonth = fromMonth,
                    hasMoreData = hasDataBefore(fromMonth),
                    selectedMonthYear = SelectedMonthYear.from(yearMonth)
                )
            }
        }
    }

    // ==================== DATA PROCESSING ====================

    /**
     * Process all data sources into displayable entries
     * Combines: DailyLogs + Calendar data + Cycle data
     */
    private fun processDataToEntries(
        fromMonth: YearMonth,
        toMonth: YearMonth,
        filters: ArchiveFilters
    ): List<ArchiveEntry> {
        val result = mutableListOf<ArchiveEntry>()

        // Filter logs by date range
        val logsInRange = allDailyLogs.filter { log ->
            val logMonth = YearMonth.from(log.date)
            logMonth in fromMonth..toMonth
        }

        // Group by month (newest first)
        val logsByMonth = logsInRange.groupBy { YearMonth.from(it.date) }

        var currentMonth = toMonth
        while (currentMonth >= fromMonth) {
            val monthLogs = logsByMonth[currentMonth] ?: emptyList()

            // Build archive entries for this month by combining all data sources
            val monthEntries = buildMonthEntries(currentMonth, monthLogs)

            // Apply filters
            val filteredEntries = monthEntries.filter { entry ->
                when (entry) {
                    is ArchiveEntry.DayEntry -> filters.matches(entry.entry)
                    else -> true  // Always show headers, etc.
                }
            }

            // Add month header
            val monthInfo = calculateMonthInfo(currentMonth, monthLogs)
            result.add(ArchiveEntry.MonthHeader(currentMonth, monthInfo))

            // Add filtered entries
            result.addAll(filteredEntries)

            currentMonth = currentMonth.minusMonths(1)
        }

        return result
    }

    /**
     * Build archive entries for a single month by merging all data sources
     */
    private fun buildMonthEntries(
        month: YearMonth,
        logs: List<DailyLog>
    ): List<ArchiveEntry> {
        val entries = mutableListOf<ArchiveEntry>()
        val calendarData = cachedCalendarDisplayData

        // Create a map of logs by date for quick lookup
        val logsByDate = logs.associateBy { it.date }

        // Get all unique dates from logs and calendar data
        val allDates = mutableSetOf<LocalDate>()
        allDates.addAll(logs.map { it.date })

        // Add period dates from calendar
        calendarData?.periodDates?.forEach { date ->
            if (YearMonth.from(date) == month) allDates.add(date)
        }

        // Add ovulation date
        calendarData?.ovulationDate?.let { date ->
            if (YearMonth.from(date) == month) allDates.add(date)
        }

        // Sort dates descending (newest first)
        val sortedDates = allDates.sortedDescending()

        // Build archive entry for each date
        sortedDates.forEach { date ->
            val dayEntry = buildDayEntry(date, logsByDate[date])
            if (dayEntry.hasContent) {
                entries.add(ArchiveEntry.DayEntry(dayEntry))
            }
        }

        return entries
    }

    /**
     * Build a single day entry by merging all data sources for that date
     */
    private fun buildDayEntry(date: LocalDate, log: DailyLog?): ArchiveDayEntry {
        val calendarData = cachedCalendarDisplayData
        val cycle = cachedCycleData

        // Separate symptoms and moods from log
        val symptoms = log?.selectedItems?.filter {
            it.category == QuickTapCategory.PHYSICAL
        } ?: emptyList()

        val moods = log?.selectedItems?.filter {
            it.category == QuickTapCategory.EMOTIONAL
        } ?: emptyList()

        val medications = log?.selectedItems?.filter {
            it.category == QuickTapCategory.LIFESTYLE &&
                    (it.label.contains("meds", ignoreCase = true) || it.emoji == "💊")
        }?.map {
            MedicationEntry(id = it.id, name = it.label, emoji = it.emoji)
        } ?: emptyList()

        // Determine if it's a period day
        val isPeriodDay = calendarData?.periodDates?.contains(date) == true

        // Determine if it's ovulation day
        val isOvulationDay = calendarData?.ovulationDate == date

        // Determine if it's fertile day
        val isFertileDay = calendarData?.fertileDates?.contains(date) == true

        // Get cycle phase (mock for now)
        val phase = cycle?.currentPhase

        // Calculate cycle day (mock for now - would be calculated from cycle data)
        val cycleDay = cycle?.cycleDay

        return ArchiveDayEntry(
            date = date,
            cycleDay = cycleDay,
            phase = phase,
            isPeriodDay = isPeriodDay,
            flowIntensity = null,  // Would come from detailed period tracking
            isFertileDay = isFertileDay,
            isOvulationDay = isOvulationDay,
            symptoms = symptoms,
            moods = moods,
            medications = medications,
            notes = log?.freeFormText?.takeIf { it.isNotBlank() },
            attachments = log?.attachments ?: emptyList(),
            chatbotEvents = emptyList(),  // Future: from chatbot backend
            createdAt = log?.createdAt ?: java.time.Instant.now()
        )
    }

    /**
     * Calculate summary info for a specific month
     */
    private fun calculateMonthInfo(month: YearMonth, logs: List<DailyLog>): MonthCycleInfo {
        val calendarData = cachedCalendarDisplayData

        val periodDays = calendarData?.periodDates?.count {
            YearMonth.from(it) == month
        } ?: 0

        val ovulationDate = calendarData?.ovulationDate?.takeIf {
            YearMonth.from(it) == month
        }

        val fertileWindowDays = calendarData?.fertileDates?.count {
            YearMonth.from(it) == month
        } ?: 0

        return MonthCycleInfo(
            cycleNumbers = emptyList(),  // Would calculate from cycle data
            periodDays = periodDays,
            loggedDays = logs.size,
            fertileWindowDays = fertileWindowDays,
            ovulationDate = ovulationDate
        )
    }

    /**
     * Calculate overall archive summary statistics
     */
    private fun calculateSummary(): ArchiveSummary {
        val sortedLogs = allDailyLogs.sortedBy { it.date }
        val oldestDate = sortedLogs.firstOrNull()?.date
        val cycle = cachedCycleData

        return ArchiveSummary(
            totalCycles = 0,  // Would calculate from cycle tracking
            trackingSince = oldestDate,
            averageCycleLength = cycle?.cycleLength,
            averagePeriodLength = 5,  // Would calculate from period data
            currentCycleDay = cycle?.cycleDay,
            totalLoggedDays = allDailyLogs.size,
            totalSymptomLogs = allDailyLogs.sumOf { log ->
                log.selectedItems.count { it.category == QuickTapCategory.PHYSICAL }
            },
            totalMoodLogs = allDailyLogs.sumOf { log ->
                log.selectedItems.count { it.category == QuickTapCategory.EMOTIONAL }
            },
            longestCycle = null,   // Would calculate from cycle data
            shortestCycle = null   // Would calculate from cycle data
        )
    }

    // ==================== FILTERS & SEARCH ====================

    /**
     * Update filters and refresh entries
     */
    fun updateFilters(filters: ArchiveFilters) {
        _uiState.update { it.copy(filters = filters, showFilterSheet = false) }
        refreshEntries()
    }

    /**
     * Update search query
     */
    fun updateSearchQuery(query: String) {
        _uiState.update { state ->
            state.copy(filters = state.filters.copy(searchQuery = query))
        }
        refreshEntries()
    }

    /**
     * Clear search query
     */
    fun clearSearch() {
        updateSearchQuery("")
    }

    /**
     * Reset all filters to defaults
     */
    fun resetFilters() {
        updateFilters(ArchiveFilters())
    }

    /**
     * Show filter bottom sheet
     */
    fun showFilterSheet() {
        _uiState.update { it.copy(showFilterSheet = true) }
    }

    /**
     * Hide filter bottom sheet
     */
    fun hideFilterSheet() {
        _uiState.update { it.copy(showFilterSheet = false) }
    }

    /**
     * Refresh entries with current filters
     */
    private fun refreshEntries() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val entries = processDataToEntries(
                fromMonth = currentState.oldestLoadedMonth,
                toMonth = YearMonth.now(),
                filters = currentState.filters
            )
            _uiState.update { it.copy(entries = entries) }
        }
    }

    // ==================== MONTH PICKER ====================

    /**
     * Show month/year picker
     */
    fun showMonthPicker() {
        _uiState.update { it.copy(showMonthPicker = true) }
    }

    /**
     * Hide month/year picker
     */
    fun hideMonthPicker() {
        _uiState.update { it.copy(showMonthPicker = false) }
    }

    /**
     * Update selected month/year in picker
     */
    fun updateSelectedMonthYear(yearMonth: SelectedMonthYear) {
        _uiState.update { it.copy(selectedMonthYear = yearMonth) }
    }

    // ==================== ERROR HANDLING ====================

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Retry loading after error
     */
    fun retryLoading() {
        clearError()
        loadInitialData()
    }

    // ==================== FORMATTING HELPERS ====================

    /**
     * Format a date for display in list
     * Examples: "Today", "Yesterday", "Mon, Jan 28"
     */
    fun formatDate(date: LocalDate): String {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        return when (date) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> {
                val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                val formatted = date.format(DateTimeFormatter.ofPattern("MMM d"))
                "$dayOfWeek, $formatted"
            }
        }
    }

    /**
     * Format a date for detail screen header
     * Example: "Monday, January 28, 2025"
     */
    fun formatDateLong(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
    }

    /**
     * Format month header
     * Example: "January 2025"
     */
    fun formatMonthHeader(yearMonth: YearMonth): String {
        return yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    }

    /**
     * Format date for navigation parameter (ISO format)
     * Example: "2025-01-28"
     */
    fun formatDateForNavigation(date: LocalDate): String {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    /**
     * Parse date from navigation parameter
     * Example: "2025-01-28" -> LocalDate
     */
    fun parseDateFromNavigation(dateString: String): LocalDate? {
        return try {
            LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            null
        }
    }

    // ==================== NAVIGATION HELPERS ====================

    /**
     * Select a date (for detail navigation)
     */
    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    /**
     * Clear selected date
     */
    fun clearSelectedDate() {
        _uiState.update { it.copy(selectedDate = null) }
    }

    /**
     * Get entry for a specific date
     */
    fun getEntryForDate(date: LocalDate): ArchiveDayEntry? {
        return _uiState.value.entries
            .filterIsInstance<ArchiveEntry.DayEntry>()
            .find { it.entry.date == date }
            ?.entry
    }
}