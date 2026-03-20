package com.example.moonsyncapp.util

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val activeFileId: String? = null
) {
    val progress: Float
        get() = if (durationMs > 0) {
            (currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f)
        } else 0f
}

/**
 * Simple audio player wrapper with state tracking.
 * Supports play/pause/stop and progress tracking.
 */
class AudioPlayer {

    private var mediaPlayer: MediaPlayer? = null

    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    /**
     * Play or resume audio from a file path or URI string.
     * If a different file is already playing, stops it first.
     */
    fun playOrPause(context: Context, fileId: String, uriString: String) {
        val currentState = _state.value

        // If same file is playing, toggle pause
        if (currentState.activeFileId == fileId && currentState.isPlaying) {
            pause()
            return
        }

        // If same file is paused, resume
        if (currentState.activeFileId == fileId && !currentState.isPlaying && mediaPlayer != null) {
            resume()
            return
        }

        // Different file or fresh start — stop current and play new
        stop()

        try {
            mediaPlayer = MediaPlayer().apply {
                // Try as file path first, then as URI
                val file = File(uriString)
                if (file.exists()) {
                    setDataSource(file.absolutePath)
                } else {
                    setDataSource(context, Uri.parse(uriString))
                }

                prepare()
                start()

                val duration = duration.toLong()
                _state.value = PlaybackState(
                    isPlaying = true,
                    currentPositionMs = 0L,
                    durationMs = duration,
                    activeFileId = fileId
                )

                setOnCompletionListener {
                    _state.value = _state.value.copy(
                        isPlaying = false,
                        currentPositionMs = duration
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _state.value = PlaybackState()
        }
    }

    /**
     * Update current position (call from a periodic coroutine).
     */
    fun updatePosition() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    _state.value = _state.value.copy(
                        currentPositionMs = player.currentPosition.toLong()
                    )
                }
            }
        } catch (e: Exception) {
            // Player may have been released
        }
    }

    private fun pause() {
        try {
            mediaPlayer?.pause()
            _state.value = _state.value.copy(isPlaying = false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun resume() {
        try {
            mediaPlayer?.start()
            _state.value = _state.value.copy(isPlaying = true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            // Already released
        }
        mediaPlayer = null
        _state.value = PlaybackState()
    }

    fun release() {
        stop()
    }
}