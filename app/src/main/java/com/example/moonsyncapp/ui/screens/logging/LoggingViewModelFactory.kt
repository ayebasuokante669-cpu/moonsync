package com.example.moonsyncapp.ui.screens.logging

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.moonsyncapp.data.LoggingDataStore

class LoggingViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoggingViewModel::class.java)) {
            return LoggingViewModel(LoggingDataStore(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}