package com.example.moonsyncapp.ui.screens.chat

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moonsyncapp.data.auth.AuthManager
import com.example.moonsyncapp.data.model.ReportReason
import com.example.moonsyncapp.util.AudioPlayer
import com.example.moonsyncapp.util.AudioRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant

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

class MomoChatViewModel(private val context: Context) : ViewModel() {

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MomoChatViewModel(context.applicationContext) as T
            }
        }
    }

    private var audioRecorder: AudioRecorder? = null

    private val _uiState = MutableStateFlow(
        ChatUiState(
            messages = listOf(
                ChatMessage(
                    id = "m1",
                    author = ChatAuthor.MOMO,
                    text = "Hi, I’m Cyra. Ask me anything about your cycle, symptoms, or wellbeing."
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

        viewModelScope.launch {
            val replyText = fetchCyraReply(outgoing)
            val reply = ChatMessage(
                id = "m_${System.currentTimeMillis()}",
                author = ChatAuthor.MOMO,
                text = replyText
            )
            _uiState.update { state ->
                state.copy(
                    isMomoTyping = false,
                    messages = state.messages + reply
                )
            }
        }
    }

    private suspend fun fetchCyraReply(userMsg: ChatMessage): String {
        val hasImage = userMsg.attachments.any { it is ChatAttachment.Image }
        val hasAudio = userMsg.attachments.any { it is ChatAttachment.Audio }

        val inputText = when {
            hasImage -> "User shared an image. ${userMsg.text?.let { "They said: $it" } ?: "Please acknowledge the image."}"
            hasAudio -> "User sent a voice note. ${userMsg.text?.let { "They said: $it" } ?: "Please acknowledge the audio."}"
            else -> userMsg.text ?: "Hello"
        }

        return withContext(Dispatchers.IO) {
            try {
                val token = AuthManager(context).currentUser.first()?.token ?: ""

                val url = java.net.URL("https://moonsync-production.up.railway.app/ai/chat")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.doOutput = true
                conn.connectTimeout = 30000
                conn.readTimeout = 60000

                val escaped = inputText.replace("\\", "\\\\").replace("\"", "\\\"")
                val body = """{"message":"$escaped","session_id":null}"""
                conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

                val responseCode = conn.responseCode
                if (responseCode == 200) {
                    val response = conn.inputStream.bufferedReader(Charsets.UTF_8).readText()
                    org.json.JSONObject(response).optString("response", "").takeIf { it.isNotBlank() }
                        ?: "I’m here for you. Could you tell me more?"
                } else {
                    val errorBody = conn.errorStream?.bufferedReader(Charsets.UTF_8)?.readText() ?: "no error body"
                    Log.e("MomoChatViewModel", "AI chat failed: HTTP $responseCode — $errorBody")
                    "I’m having trouble connecting right now. Please try again."
                }
            } catch (e: Exception) {
                Log.e("MomoChatViewModel", "AI chat exception: ${e.message}", e)
                "I’m having trouble connecting right now. Please try again."
            }
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