package com.example.moonsyncapp.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Helper to create temporary camera URIs and manage media files.
 */
object MediaHelper {

    /**
     * Create a temporary file URI for camera capture.
     * Uses FileProvider for secure sharing with camera app.
     */
    fun createCameraImageUri(context: Context): Uri {
        val photoDir = File(context.cacheDir, "photos").apply { mkdirs() }
        val photoFile = File(photoDir, "camera_${System.currentTimeMillis()}.jpg")

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
    }
}