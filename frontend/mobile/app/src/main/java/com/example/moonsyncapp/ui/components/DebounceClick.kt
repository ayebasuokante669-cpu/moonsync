package com.example.moonsyncapp.ui.components

import androidx.compose.runtime.*

@Composable
fun rememberDebounceClick(
    debounceTime: Long = 300L,
    onClick: () -> Unit
): () -> Unit {
    var lastClickTime by remember { mutableStateOf(0L) }

    return {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= debounceTime) {
            lastClickTime = currentTime
            onClick()
        }
    }
}