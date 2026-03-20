package com.example.moonsyncapp.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/**
 * Simple audio recorder wrapper.
 * Creates audio files in app cache directory.
 */
class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null
    private var startTimeMs: Long = 0L

    /**
     * Start recording audio to a cache file.
     * Returns the output file path.
     *
     * Caller must ensure RECORD_AUDIO permission is granted before calling.
     */
    fun start(): File? {
        return try {
            val audioDir = File(context.cacheDir, "audio").apply { mkdirs() }
            val outputFile = File(audioDir, "recording_${System.currentTimeMillis()}.m4a")

            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            currentFile = outputFile
            startTimeMs = System.currentTimeMillis()
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            cleanup()
            null
        }
    }

    /**
     * Stop recording and return the file + duration.
     */
    fun stop(): Pair<File, Long>? {
        return try {
            val durationMs = System.currentTimeMillis() - startTimeMs
            recorder?.apply {
                stop()
                release()
            }
            recorder = null

            val file = currentFile
            currentFile = null

            if (file != null && file.exists() && durationMs > 500) {
                Pair(file, durationMs)
            } else {
                file?.delete()
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            cleanup()
            null
        }
    }

    /**
     * Cancel recording and delete file.
     */
    fun cancel() {
        cleanup()
    }

    private fun cleanup() {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            // Recorder may not have started
        }
        recorder = null
        currentFile?.delete()
        currentFile = null
    }
}