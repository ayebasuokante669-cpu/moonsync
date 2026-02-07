package com.example.moonsyncapp.ui.screens.archive

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.moonsyncapp.data.LoggingDataStore
import com.example.moonsyncapp.ui.viewmodels.CalendarViewModel

/**
 * Factory for creating ArchiveViewModel with required dependencies
 *
 * Dependencies:
 * - LoggingDataStore: For daily logs (symptoms, moods, notes, attachments)
 * - CalendarViewModel: For period dates, fertile window, ovulation, cycle data
 *
 * Usage:
 * ```
 * val viewModel: ArchiveViewModel = viewModel(
 *     factory = ArchiveViewModelFactory(LocalContext.current)
 * )
 * ```
 */
class ArchiveViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArchiveViewModel::class.java)) {
            // Create data store instance
            val loggingDataStore = LoggingDataStore(context)

            // Create calendar view model instance
            // Note: CalendarViewModel uses mock data for now
            // Backend team will replace with real data sources
            val calendarViewModel = CalendarViewModel()

            return ArchiveViewModel(
                loggingDataStore = loggingDataStore,
                calendarViewModel = calendarViewModel
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}