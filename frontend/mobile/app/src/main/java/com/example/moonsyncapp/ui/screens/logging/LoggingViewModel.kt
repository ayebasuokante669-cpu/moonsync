package com.example.moonsyncapp.ui.screens.logging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moonsyncapp.data.LoggingDataStore
import com.example.moonsyncapp.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID
import android.content.Context
import com.example.moonsyncapp.util.AudioRecorder
import com.example.moonsyncapp.util.AudioPlayer

data class LoggingUiState(
    // Today's log state
    val todayLog: DailyLog = DailyLog(date = LocalDate.now()),
    val recentLogs: List<DailyLog> = emptyList(),
    val allQuickTapItems: List<QuickTapItem> = DefaultQuickTapItems.all,
    val customItems: List<QuickTapItem> = emptyList(),
    val streak: LoggingStreak = LoggingStreak(),
    val freeFormInput: String = "",
    val isAddingCustomItem: Boolean = false,
    val customItemInput: String = "",
    val customItemCategory: QuickTapCategory = QuickTapCategory.LIFESTYLE,
    val expandedLogId: String? = null,
    val isRecordingVoice: Boolean = false,
    val recordingDurationMs: Long = 0L,
    val showAllLogs: Boolean = false,

    // Edit mode state
    val editingLog: DailyLog? = null,
    val editSelectedItems: List<QuickTapItem> = emptyList(),
    val editFormText: String = "",
    val editAttachments: List<LogAttachment> = emptyList(),
    val isEditRecordingVoice: Boolean = false,
    val editRecordingDurationMs: Long = 0L,
    val showTodayLogMessage: Boolean = false,
    val hasLoggedToday: Boolean = false,
    val editExpiredMessage: Boolean = false
)

class LoggingViewModel(
    private val dataStore: LoggingDataStore
) : ViewModel() {
    private var audioRecorder: AudioRecorder? = null

    private val _uiState = MutableStateFlow(LoggingUiState())
    val audioPlayer = AudioPlayer()
    val uiState: StateFlow<LoggingUiState> = _uiState.asStateFlow()

    companion object {
        const val MAX_CUSTOM_LABEL_LENGTH = 20
        const val EDIT_WINDOW_DAYS = 3L  // Logs older than 3 days can't be edited
    }

    init {
        loadInitialData()
        loadAllLogs()
    }

//    private fun loadInitialData() {
//        viewModelScope.launch {
//            // Load persisted streak
//            val savedStreak = dataStore.getStreak()
//            val today = LocalDate.now()
//            val hasLoggedToday = savedStreak?.lastLogDate == today
//
//            // If streak was broken (missed more than 1 day), reset
//            val validatedStreak = if (savedStreak != null) {
//                val daysSinceLastLog = if (savedStreak.lastLogDate != null) {
//                    java.time.temporal.ChronoUnit.DAYS.between(savedStreak.lastLogDate, today)
//                } else {
//                    Long.MAX_VALUE
//                }
//
//                when {
//                    daysSinceLastLog <= 1L -> savedStreak // Today or yesterday — streak intact
//                    else -> savedStreak.copy(currentStreak = 0) // Broken
//                }
//            } else {
//                LoggingStreak()
//            }
//
//            _uiState.update {
//                it.copy(
//                    streak = validatedStreak,
//                    hasLoggedToday = hasLoggedToday
//                )
//            }
//        }
//
//        // Simulate loading past logs and streak
//        val mockLogs = listOf(
//            DailyLog(
//                date = LocalDate.now().minusDays(1),
//                selectedItems = listOf(
//                    DefaultQuickTapItems.emotional[0], // Happy
//                    DefaultQuickTapItems.lifestyle[0]  // Slept well
//                ),
//                freeFormText = "Had a wonderful day! Went for a walk and felt great.",
//                lockedAt = Instant.now().minus(12, ChronoUnit.HOURS)
//            ),
//            DailyLog(
//                date = LocalDate.now().minusDays(2),
//                selectedItems = listOf(
//                    DefaultQuickTapItems.physical[0],  // Tired
//                    DefaultQuickTapItems.physical[2],  // Cramps
//                    DefaultQuickTapItems.emotional[3]  // Anxious
//                ),
//                freeFormText = "Rough day, but managed to rest.",
//                lockedAt = Instant.now().minus(36, ChronoUnit.HOURS)
//            ),
//            DailyLog(
//                date = LocalDate.now().minusDays(3),
//                selectedItems = listOf(
//                    DefaultQuickTapItems.physical[7],  // Energetic
//                    DefaultQuickTapItems.lifestyle[2]  // Exercised
//                ),
//                freeFormText = "",
//                lockedAt = Instant.now().minus(60, ChronoUnit.HOURS)
//            )
//        )
//
//        _uiState.update {
//            it.copy(
//                recentLogs = mockLogs,
//                streak = LoggingStreak(
//                    currentStreak = 4,
//                    longestStreak = 12,
//                    lastLogDate = LocalDate.now().minusDays(1)
//                )
//            )
//        }
//    }
//private fun loadInitialData() {
//    // Load streak from DataStore
//    viewModelScope.launch {
//        try {
//            val savedStreak = dataStore.getStreak()
//            val today = LocalDate.now()
//
//            if (savedStreak != null) {
//                val hasLoggedToday = savedStreak.lastLogDate == today
//
//                val daysSinceLastLog = if (savedStreak.lastLogDate != null) {
//                    ChronoUnit.DAYS.between(savedStreak.lastLogDate, today)
//                } else {
//                    Long.MAX_VALUE
//                }
//
//                val validatedStreak = when {
//                    daysSinceLastLog <= 1L -> savedStreak
//                    else -> savedStreak.copy(currentStreak = 0)
//                }
//
//                _uiState.update {
//                    it.copy(
//                        streak = validatedStreak,
//                        hasLoggedToday = hasLoggedToday
//                    )
//                }
//            }
//        } catch (e: Exception) {
//            // If DataStore methods don't exist yet, fall back to mock
//            _uiState.update {
//                it.copy(
//                    streak = LoggingStreak(
//                        currentStreak = 0,
//                        longestStreak = 0,
//                        lastLogDate = null
//                    )
//                )
//            }
//        }
//    }
//
//    // Load mock logs
//    val mockLogs = listOf(
//        DailyLog(
//            date = LocalDate.now().minusDays(1),
//            selectedItems = listOf(
//                DefaultQuickTapItems.emotional[0],
//                DefaultQuickTapItems.lifestyle[0]
//            ),
//            freeFormText = "Had a wonderful day! Went for a walk and felt great.",
//            lockedAt = Instant.now().minus(12, ChronoUnit.HOURS)
//        ),
//        DailyLog(
//            date = LocalDate.now().minusDays(2),
//            selectedItems = listOf(
//                DefaultQuickTapItems.physical[0],
//                DefaultQuickTapItems.physical[2],
//                DefaultQuickTapItems.emotional[3]
//            ),
//            freeFormText = "Rough day, but managed to rest.",
//            lockedAt = Instant.now().minus(36, ChronoUnit.HOURS)
//        ),
//        DailyLog(
//            date = LocalDate.now().minusDays(3),
//            selectedItems = listOf(
//                DefaultQuickTapItems.physical[7],
//                DefaultQuickTapItems.lifestyle[2]
//            ),
//            freeFormText = "",
//            lockedAt = Instant.now().minus(60, ChronoUnit.HOURS)
//        )
//    )
//
//    _uiState.update {
//        it.copy(recentLogs = mockLogs)
//    }
//}
    private fun loadInitialData() {
        // Load streak from DataStore
        viewModelScope.launch {
            try {
                val savedStreak = dataStore.getStreak()
                val today = LocalDate.now()

                if (savedStreak != null) {
                    val hasLoggedToday = savedStreak.lastLogDate == today

                    val daysSinceLastLog = if (savedStreak.lastLogDate != null) {
                        ChronoUnit.DAYS.between(savedStreak.lastLogDate, today)
                    } else {
                        Long.MAX_VALUE
                    }

                    val validatedStreak = when {
                        daysSinceLastLog <= 1L -> savedStreak
                        else -> savedStreak.copy(currentStreak = 0)
                    }

                    _uiState.update {
                        it.copy(
                            streak = validatedStreak,
                            hasLoggedToday = hasLoggedToday
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        streak = LoggingStreak(
                            currentStreak = 0,
                            longestStreak = 0,
                            lastLogDate = null
                        )
                    )
                }
            }
        }

        // Load custom items from DataStore
        viewModelScope.launch {
            dataStore.getCustomItems().collect { items ->
                _uiState.update { it.copy(customItems = items) }
            }
        }
    }

    // ==================== TODAY'S LOG METHODS ====================

    fun toggleQuickTapItem(item: QuickTapItem) {
        _uiState.update { state ->
            val currentSelected = state.todayLog.selectedItems
            val newSelected = if (currentSelected.any { it.id == item.id }) {
                currentSelected.filter { it.id != item.id }
            } else {
                currentSelected + item
            }

            state.copy(
                todayLog = state.todayLog.copy(
                    selectedItems = newSelected,
                    updatedAt = Instant.now()
                )
            )
        }
        recordDailyActivity()
    }

    fun dismissEditExpiredMessage() {
        _uiState.update { it.copy(editExpiredMessage = false) }
    }

    /**
     * Check if a log can still be edited (within 3-day window).
     */
    fun isLogEditable(log: DailyLog): Boolean {
        if (log.date == LocalDate.now()) return false // Use form above
        val daysSinceLog = ChronoUnit.DAYS.between(log.date, LocalDate.now())
        return daysSinceLog <= EDIT_WINDOW_DAYS
    }
    fun isItemSelected(item: QuickTapItem): Boolean {
        return _uiState.value.todayLog.selectedItems.any { it.id == item.id }
    }

    fun updateFreeFormInput(text: String) {
        _uiState.update { state ->
            state.copy(
                freeFormInput = text,
                todayLog = state.todayLog.copy(
                    freeFormText = text,
                    updatedAt = Instant.now()
                )
            )
        }
    }

    fun showAddCustomItem(category: QuickTapCategory = QuickTapCategory.LIFESTYLE) {
        _uiState.update {
            it.copy(
                isAddingCustomItem = true,
                customItemInput = "",
                customItemCategory = category
            )
        }
    }

    fun hideAddCustomItem() {
        _uiState.update {
            it.copy(
                isAddingCustomItem = false,
                customItemInput = "",
                customItemCategory = QuickTapCategory.LIFESTYLE
            )
        }
    }

    fun updateCustomItemInput(text: String) {
        if (text.length <= MAX_CUSTOM_LABEL_LENGTH) {
            _uiState.update { it.copy(customItemInput = text) }
        }
    }

    fun updateCustomItemCategory(category: QuickTapCategory) {
        _uiState.update { it.copy(customItemCategory = category) }
    }

    fun saveCustomItem() {
        val input = _uiState.value.customItemInput.trim()
        if (input.isBlank()) return

        val newItem = QuickTapItem(
            emoji = "✨",
            label = input,
            category = _uiState.value.customItemCategory,
            isCustom = true
        )

        _uiState.update { state ->
            state.copy(
                customItems = state.customItems + newItem,
                isAddingCustomItem = false,
                customItemInput = ""
            )
        }

        // Persist custom items
        viewModelScope.launch {
            dataStore.saveCustomItems(_uiState.value.customItems)
        }

        // Auto-select the new custom item
        toggleQuickTapItem(newItem)
    }

    fun toggleLogExpanded(logId: String) {
        _uiState.update { state ->
            state.copy(
                expandedLogId = if (state.expandedLogId == logId) null else logId
            )
        }
    }

    fun startVoiceRecording() {
        _uiState.update { it.copy(isRecordingVoice = true, recordingDurationMs = 0L) }

        viewModelScope.launch {
            while (_uiState.value.isRecordingVoice) {
                delay(100)
                _uiState.update { it.copy(recordingDurationMs = it.recordingDurationMs + 100) }
            }
        }
    }

    fun stopVoiceRecording() {
        val duration = _uiState.value.recordingDurationMs
        _uiState.update { it.copy(isRecordingVoice = false) }

        if (duration > 500) { // At least 0.5 seconds
            val voiceNote = LogAttachment.VoiceNote(durationMs = duration)
            addAttachment(voiceNote)
        }
    }

    /**
     * Start real audio recording using MediaRecorder.
     * Requires RECORD_AUDIO permission to be granted.
     */
    fun startRealVoiceRecording(context: Context) {
        if (_uiState.value.isRecordingVoice) return

        audioRecorder = AudioRecorder(context)
        val file = audioRecorder?.start()

        if (file != null) {
            _uiState.update { it.copy(isRecordingVoice = true, recordingDurationMs = 0L) }

            // Timer to show recording duration
            viewModelScope.launch {
                while (_uiState.value.isRecordingVoice) {
                    delay(100)
                    _uiState.update { it.copy(recordingDurationMs = it.recordingDurationMs + 100) }
                }
            }
        }
    }

    /**
     * Stop real audio recording and attach the file.
     */
    fun stopRealVoiceRecording() {
        _uiState.update { it.copy(isRecordingVoice = false) }

        val result = audioRecorder?.stop()
        audioRecorder = null

        if (result != null) {
            val (file, durationMs) = result
            val voiceNote = LogAttachment.VoiceNote(
                uri = file.absolutePath,
                durationMs = durationMs
            )
            addAttachment(voiceNote)
        }
    }

    fun attachPhoto() {
        // In real implementation, this would open image picker
        val photo = LogAttachment.Photo(description = "Photo attachment")
        addAttachment(photo)
    }

    /**
     * Attach a photo from a real URI (gallery or camera).
     */
    fun attachPhotoFromUri(uriString: String) {
        val photo = LogAttachment.Photo(
            uri = uriString,
            description = "Photo"
        )
        addAttachment(photo)
    }

    private fun addAttachment(attachment: LogAttachment) {
        _uiState.update { state ->
            state.copy(
                todayLog = state.todayLog.copy(
                    attachments = state.todayLog.attachments + attachment,
                    updatedAt = Instant.now()
                )
            )
        }
    }

    fun removeAttachment(attachment: LogAttachment) {
        _uiState.update { state ->
            val attachmentId = when (attachment) {
                is LogAttachment.VoiceNote -> attachment.id
                is LogAttachment.Photo -> attachment.id
            }
            state.copy(
                todayLog = state.todayLog.copy(
                    attachments = state.todayLog.attachments.filterNot {
                        when (it) {
                            is LogAttachment.VoiceNote -> it.id == attachmentId
                            is LogAttachment.Photo -> it.id == attachmentId
                        }
                    },
                    updatedAt = Instant.now()
                )
            )
        }
    }

    fun toggleShowAllLogs() {
        _uiState.update { it.copy(showAllLogs = !it.showAllLogs) }
    }

    fun getItemsByCategory(category: QuickTapCategory): List<QuickTapItem> {
        val defaultItems = _uiState.value.allQuickTapItems.filter { it.category == category }
        val customItems = _uiState.value.customItems.filter { it.category == category }
        return defaultItems + customItems
    }

    fun formatDate(date: LocalDate): String {
        val today = LocalDate.now()
        return when (date) {
            today -> "Today"
            today.minusDays(1) -> "Yesterday"
            else -> date.format(DateTimeFormatter.ofPattern("MMM d"))
        }
    }

    fun saveFreeFormEntry() {
        val currentLog = _uiState.value.todayLog
        if (currentLog.freeFormText.isNotBlank() || currentLog.attachments.isNotEmpty()) {
            viewModelScope.launch {
                dataStore.saveLog(
                    currentLog.copy(
                        id = UUID.randomUUID().toString(),
                        createdAt = Instant.now()
                    )
                )
                // Clear the form after saving
                _uiState.update { it.copy(
                    freeFormInput = "",
                    todayLog = it.todayLog.copy(
                        freeFormText = "",
                        attachments = emptyList()
                    )
                ) }
            }
            recordDailyActivity()
        }
    }

//    fun loadAllLogs() {
//        viewModelScope.launch {
//            dataStore.getAllLogs().collect { logs ->
//                _uiState.update { it.copy(recentLogs = logs) }
//            }
//        }
//    }

    fun loadAllLogs() {
        viewModelScope.launch {
            dataStore.getAllLogs().collect { logs ->
                if (logs.isNotEmpty()) {
                    // Real logs from DataStore — use these
                    _uiState.update { it.copy(recentLogs = logs) }
                } else {
                    // No saved logs yet — show mock data for first-time experience
                    val mockLogs = listOf(
                        DailyLog(
                            date = LocalDate.now().minusDays(1),
                            selectedItems = listOf(
                                DefaultQuickTapItems.emotional[0],
                                DefaultQuickTapItems.lifestyle[0]
                            ),
                            freeFormText = "Had a wonderful day! Went for a walk and felt great.",
                            lockedAt = Instant.now().minus(12, ChronoUnit.HOURS)
                        ),
                        DailyLog(
                            date = LocalDate.now().minusDays(2),
                            selectedItems = listOf(
                                DefaultQuickTapItems.physical[0],
                                DefaultQuickTapItems.physical[2],
                                DefaultQuickTapItems.emotional[3]
                            ),
                            freeFormText = "Rough day, but managed to rest.",
                            lockedAt = Instant.now().minus(36, ChronoUnit.HOURS)
                        ),
                        DailyLog(
                            date = LocalDate.now().minusDays(3),
                            selectedItems = listOf(
                                DefaultQuickTapItems.physical[7],
                                DefaultQuickTapItems.lifestyle[2]
                            ),
                            freeFormText = "",
                            lockedAt = Instant.now().minus(60, ChronoUnit.HOURS)
                        )
                    )
                    _uiState.update { it.copy(recentLogs = mockLogs) }
                }
            }
        }
    }

    fun deleteLog(logId: String) {
        viewModelScope.launch {
            dataStore.deleteLog(logId)
            loadAllLogs()
        }
    }

    fun updateLog(log: DailyLog) {
        viewModelScope.launch {
            dataStore.updateLog(log)
            loadAllLogs()
        }
    }

    // ==================== EDIT MODE METHODS ====================

    fun startEditingLog(log: DailyLog) {
        // Block editing today's log via edit sheet
        if (log.date == LocalDate.now()) {
            _uiState.update { it.copy(showTodayLogMessage = true) }
            return
        }


        // Block editing logs older than 3 days
        val daysSinceLog = ChronoUnit.DAYS.between(log.date, LocalDate.now())
        if (daysSinceLog > EDIT_WINDOW_DAYS) {
            _uiState.update { it.copy(editExpiredMessage = true) }
            return
        }

        _uiState.update { state ->
            state.copy(
                editingLog = log,
                editSelectedItems = log.selectedItems,
                editFormText = log.freeFormText,
                editAttachments = log.attachments,
                isEditRecordingVoice = false,
                editRecordingDurationMs = 0L
            )
        }
    }

    fun cancelEditingLog() {
        _uiState.update { state ->
            state.copy(
                editingLog = null,
                editSelectedItems = emptyList(),
                editFormText = "",
                editAttachments = emptyList(),
                isEditRecordingVoice = false,
                editRecordingDurationMs = 0L
            )
        }
    }

    fun saveEditedLog() {
        val editingLog = _uiState.value.editingLog ?: return
        val editSelectedItems = _uiState.value.editSelectedItems
        val editFormText = _uiState.value.editFormText
        val editAttachments = _uiState.value.editAttachments

        // Validate: at least one item, text, or attachment
        if (editSelectedItems.isEmpty() && editFormText.isBlank() && editAttachments.isEmpty()) {
            return
        }

        val updatedLog = editingLog.copy(
            selectedItems = editSelectedItems,
            freeFormText = editFormText,
            attachments = editAttachments,
            isEdited = true,
            updatedAt = Instant.now()
        )

        viewModelScope.launch {
            dataStore.updateLog(updatedLog)
            cancelEditingLog()
            loadAllLogs()
        }
    }

    fun updateEditFormText(text: String) {
        _uiState.update { it.copy(editFormText = text) }
    }

    fun toggleEditItem(item: QuickTapItem) {
        _uiState.update { state ->
            val currentSelected = state.editSelectedItems
            val newSelected = if (currentSelected.any { it.id == item.id }) {
                currentSelected.filter { it.id != item.id }
            } else {
                currentSelected + item
            }
            state.copy(editSelectedItems = newSelected)
        }
    }

    fun isEditItemSelected(item: QuickTapItem): Boolean {
        return _uiState.value.editSelectedItems.any { it.id == item.id }
    }

    fun removeEditAttachment(attachment: LogAttachment) {
        _uiState.update { state ->
            val attachmentId = when (attachment) {
                is LogAttachment.VoiceNote -> attachment.id
                is LogAttachment.Photo -> attachment.id
            }
            state.copy(
                editAttachments = state.editAttachments.filterNot {
                    when (it) {
                        is LogAttachment.VoiceNote -> it.id == attachmentId
                        is LogAttachment.Photo -> it.id == attachmentId
                    }
                }
            )
        }
    }

    fun startEditVoiceRecording() {
        _uiState.update { it.copy(isEditRecordingVoice = true, editRecordingDurationMs = 0L) }

        viewModelScope.launch {
            while (_uiState.value.isEditRecordingVoice) {
                delay(100)
                _uiState.update { it.copy(editRecordingDurationMs = it.editRecordingDurationMs + 100) }
            }
        }
    }

    fun stopEditVoiceRecording() {
        val duration = _uiState.value.editRecordingDurationMs
        _uiState.update { it.copy(isEditRecordingVoice = false) }

        if (duration > 500) { // At least 0.5 seconds
            val voiceNote = LogAttachment.VoiceNote(durationMs = duration)
            addEditAttachment(voiceNote)
        }
    }

    fun attachEditPhoto() {
        // In real implementation, this would open image picker
        val photo = LogAttachment.Photo(description = "Photo attachment")
        addEditAttachment(photo)
    }

    /**
     * Attach a photo from URI in edit mode.
     */
    fun attachEditPhotoFromUri(uriString: String) {
        val photo = LogAttachment.Photo(
            uri = uriString,
            description = "Photo"
        )
        addEditAttachment(photo)
    }

    /**
     * Start real audio recording in edit mode.
     */
    fun startRealEditVoiceRecording(context: Context) {
        if (_uiState.value.isEditRecordingVoice) return

        audioRecorder = AudioRecorder(context)
        val file = audioRecorder?.start()

        if (file != null) {
            _uiState.update { it.copy(isEditRecordingVoice = true, editRecordingDurationMs = 0L) }

            viewModelScope.launch {
                while (_uiState.value.isEditRecordingVoice) {
                    delay(100)
                    _uiState.update { it.copy(editRecordingDurationMs = it.editRecordingDurationMs + 100) }
                }
            }
        }
    }

    /**
     * Stop real audio recording in edit mode.
     */
    fun stopRealEditVoiceRecording() {
        _uiState.update { it.copy(isEditRecordingVoice = false) }

        val result = audioRecorder?.stop()
        audioRecorder = null

        if (result != null) {
            val (file, durationMs) = result
            val voiceNote = LogAttachment.VoiceNote(
                uri = file.absolutePath,
                durationMs = durationMs
            )
            addEditAttachment(voiceNote)
        }
    }

    private fun addEditAttachment(attachment: LogAttachment) {
        _uiState.update { state ->
            state.copy(editAttachments = state.editAttachments + attachment)
        }
    }

    fun dismissTodayLogMessage() {
        _uiState.update { it.copy(showTodayLogMessage = false) }
    }

    // ==================== STREAK MANAGEMENT ====================

    /**
     * Records that user has logged today.
     * Called when quick-tap items are toggled or free-form is saved.
     * Only counts once per day.
     */
    private fun recordDailyActivity() {
        if (_uiState.value.hasLoggedToday) return

        val today = LocalDate.now()
        val currentStreak = _uiState.value.streak
        val lastLogDate = currentStreak.lastLogDate

        val newStreak = when {
            // First ever log
            lastLogDate == null -> {
                LoggingStreak(
                    currentStreak = 1,
                    longestStreak = maxOf(currentStreak.longestStreak, 1),
                    lastLogDate = today
                )
            }
            // Already logged today (safety check)
            lastLogDate == today -> {
                return
            }
            // Consecutive day
            lastLogDate == today.minusDays(1) -> {
                val newCount = currentStreak.currentStreak + 1
                LoggingStreak(
                    currentStreak = newCount,
                    longestStreak = maxOf(currentStreak.longestStreak, newCount),
                    lastLogDate = today
                )
            }
            // Missed a day
            else -> {
                LoggingStreak(
                    currentStreak = 1,
                    longestStreak = maxOf(currentStreak.longestStreak, 1),
                    lastLogDate = today
                )
            }
        }

        _uiState.update { state ->
            state.copy(
                streak = newStreak,
                hasLoggedToday = true
            )
        }

        // Persist streak — wrapped in try/catch in case DataStore method isn't ready
        viewModelScope.launch {
            try {
                dataStore.saveStreak(newStreak)
            } catch (e: Exception) {
                // DataStore method may not exist yet — streak still works in memory
            }
        }
    }
    // UPDATE onCleared():
    override fun onCleared() {
        audioRecorder?.cancel()
        audioRecorder = null
        audioPlayer.release()
        super.onCleared()
    }
}