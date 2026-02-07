package com.example.moonsyncapp.data.model

import androidx.compose.ui.graphics.Color

data class HealthTip(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val category: TipCategory,
    val readTime: Int, // in minutes
    val relevantPhases: List<CyclePhase>,
    val content: String // Full article content
)

enum class TipCategory(
    val displayName: String,
    val emoji: String,
    val color: Color
) {
    NUTRITION("Nutrition", "🥗", Color(0xFF66BB6A)),
    EXERCISE("Exercise", "💪", Color(0xFFFFA726)),
    MENTAL_HEALTH("Mental Health", "🧠", Color(0xFFAB47BC)),
    SYMPTOMS("Symptoms", "🩺", Color(0xFFEC407A)),
    LIFESTYLE("Lifestyle", "🌸", Color(0xFF42A5F5))
}