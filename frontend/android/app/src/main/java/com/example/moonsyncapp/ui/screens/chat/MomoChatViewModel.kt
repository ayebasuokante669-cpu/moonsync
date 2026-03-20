package com.example.moonsyncapp.ui.screens.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import kotlin.random.Random
import com.example.moonsyncapp.data.model.ReportReason
import android.content.Context
import com.example.moonsyncapp.util.AudioRecorder
import com.example.moonsyncapp.util.AudioPlayer

enum class ChatAuthor { USER, MOMO }

sealed interface ChatAttachment {
    data class Image(val uri: Uri? = null, val description: String = "Image") : ChatAttachment
    data class Audio(val uri: Uri? = null, val durationMs: Long = 0L) : ChatAttachment
}

data class ChatMessage(
    val id: String,
    val author: ChatAuthor,
    val createdAt: Instant = Instant.now(),
    val text: String? = null,
    val attachments: List<ChatAttachment> = emptyList(),
    val hasCurrentUserReported: Boolean = false
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isMomoTyping: Boolean = false,
    val pendingAttachment: ChatAttachment? = null,
    val isRecording: Boolean = false,
    val recordingMs: Long = 0L,
    val reportTargetMessage: ChatMessage? = null
)

class MomoChatViewModel : ViewModel() {
    private var audioRecorder: AudioRecorder? = null

    private val _uiState = MutableStateFlow(
        ChatUiState(
            messages = listOf(
                ChatMessage(
                    id = "m1",
                    author = ChatAuthor.MOMO,
                    text = "Hi, I’m Cyra. Ask me anything about your cycle, symptoms, or wellbeing."
                ),
                ChatMessage(
                    id = "m2",
                    author = ChatAuthor.MOMO,
                    text = "You can attach a photo or hold the mic to record a voice note."
                )
            )
        )
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var recordingJob: Job? = null

    val audioPlayer = AudioPlayer()

    // Track reported message IDs for duplicate prevention
    private val _reportedMessageIds = MutableStateFlow<Set<String>>(emptySet())

    fun onInputChange(text: String) {
        _uiState.update { it.copy(input = text) }
    }

    fun attachImage(uri: Uri? = null, description: String = "Attached photo") {
        _uiState.update { it.copy(pendingAttachment = ChatAttachment.Image(uri, description)) }
    }

    fun attachAudio(uri: Uri? = null, durationMs: Long) {
        _uiState.update { it.copy(pendingAttachment = ChatAttachment.Audio(uri, durationMs)) }
    }

    fun clearPendingAttachment() {
        _uiState.update { it.copy(pendingAttachment = null) }
    }

    fun startRecording() {
        val s = _uiState.value
        if (s.isRecording) return

        _uiState.update { it.copy(isRecording = true, recordingMs = 0L) }

        recordingJob?.cancel()
        recordingJob = viewModelScope.launch {
            while (_uiState.value.isRecording) {
                delay(100L)
                _uiState.update { state -> state.copy(recordingMs = state.recordingMs + 100L) }
            }
        }
    }

    fun stopRecordingAndStageAudio() {
        val s = _uiState.value
        if (!s.isRecording) return

        recordingJob?.cancel()
        recordingJob = null

        val duration = s.recordingMs.coerceAtLeast(600L)
        _uiState.update {
            it.copy(
                isRecording = false,
                recordingMs = 0L,
                pendingAttachment = ChatAttachment.Audio(uri = null, durationMs = duration)
            )
        }
    }

    /**
     * Start real audio recording.
     * Called after RECORD_AUDIO permission is granted.
     */
    fun startRealRecording(context: Context) {
        if (_uiState.value.isRecording) return

        audioRecorder = AudioRecorder(context)
        val file = audioRecorder?.start()

        if (file != null) {
            _uiState.update { it.copy(isRecording = true, recordingMs = 0L) }

            recordingJob?.cancel()
            recordingJob = viewModelScope.launch {
                while (_uiState.value.isRecording) {
                    delay(100L)
                    _uiState.update { state -> state.copy(recordingMs = state.recordingMs + 100L) }
                }
            }
        }
    }

    /**
     * Stop real audio recording and stage as pending attachment.
     */
    fun stopRealRecording() {
        recordingJob?.cancel()
        recordingJob = null

        _uiState.update { it.copy(isRecording = false) }

        val result = audioRecorder?.stop()
        audioRecorder = null

        if (result != null) {
            val (file, durationMs) = result
            _uiState.update {
                it.copy(
                    recordingMs = 0L,
                    pendingAttachment = ChatAttachment.Audio(
                        uri = Uri.fromFile(file),
                        durationMs = durationMs
                    )
                )
            }
        }
    }

    fun send() {
        val s = _uiState.value
        val trimmed = s.input.trim()
        val attachment = s.pendingAttachment

        if (trimmed.isEmpty() && attachment == null) return

        val outgoing = ChatMessage(
            id = "u_${System.currentTimeMillis()}",
            author = ChatAuthor.USER,
            text = trimmed.ifEmpty { null },
            attachments = attachment?.let { listOf(it) } ?: emptyList()
        )

        _uiState.update {
            it.copy(
                messages = it.messages + outgoing,
                input = "",
                pendingAttachment = null,
                isMomoTyping = true
            )
        }

        // Frontend-only fake reply (backend can replace later)
        viewModelScope.launch {
            delay(850)
            val reply = ChatMessage(
                id = "m_${System.currentTimeMillis()}",
                author = ChatAuthor.MOMO,
                text = generateMomoReply(outgoing)
            )
            _uiState.update { state ->
                state.copy(
                    isMomoTyping = false,
                    messages = state.messages + reply
                )
            }
        }
    }

    private fun generateMomoReply(userMsg: ChatMessage): String {
        val hasImage = userMsg.attachments.any { it is ChatAttachment.Image }
        val hasAudio = userMsg.attachments.any { it is ChatAttachment.Audio }

        return when {
            hasImage ->
                "I got your photo. Tell me what you want me to check in it."
            hasAudio ->
                "I received your voice note. If you can, summarize the main point in one sentence too."
            else ->
                listOf(
                    "Thanks for sharing. Want to tell me which day of your cycle you’re on?",
                    "Got it. Rate the intensity from 1–10 so we can track patterns.",
                    "Noted. If this is new or severe, consider checking in with a clinician.",
                    "I can help you spot patterns—does this happen before, during, or after your period?"
                )[Random.nextInt(4)]
        }
    }

    override fun onCleared() {
        recordingJob?.cancel()
        audioRecorder?.cancel()
        audioRecorder = null
        audioPlayer.release()
        super.onCleared()
    }

    // ==========================================
    // REPORT MANAGEMENT
    // ==========================================

    fun setReportTarget(message: ChatMessage) {
        _uiState.update { it.copy(reportTargetMessage = message) }
    }

    fun clearReportTarget() {
        _uiState.update { it.copy(reportTargetMessage = null) }
    }

    fun reportMomoMessage(messageId: String, reason: ReportReason, notes: String?) {
        viewModelScope.launch {
            // Check if already reported
            if (messageId in _reportedMessageIds.value) {
                // Already reported, just dismiss sheet
                _uiState.update { it.copy(reportTargetMessage = null) }
                return@launch
            }

            // Mark message as reported
            _uiState.update { state ->
                state.copy(
                    messages = state.messages.map { msg ->
                        if (msg.id == messageId) {
                            msg.copy(hasCurrentUserReported = true)
                        } else {
                            msg
                        }
                    },
                    reportTargetMessage = null
                )
            }

            // Track reported message ID
            _reportedMessageIds.update { it + messageId }

            // TODO: Backend integration
            // api.reportAiMessage(messageId, reason, notes)

            // Note: We DON'T hide Momo messages after reporting
            // This is AI feedback, not content moderation
            // Just track the report for analytics/improvement
        }
    }
}