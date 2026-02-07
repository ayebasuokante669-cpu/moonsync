package com.example.moonsyncapp.ui.screens.healthtips

import androidx.lifecycle.ViewModel
import com.example.moonsyncapp.data.model.CyclePhase
import com.example.moonsyncapp.data.model.HealthTip
import com.example.moonsyncapp.data.model.TipCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HealthTipsViewModel : ViewModel() {

    private val _selectedCategory = MutableStateFlow<TipCategory?>(null)
    val selectedCategory: StateFlow<TipCategory?> = _selectedCategory.asStateFlow()

    private val _currentPhase = MutableStateFlow(CyclePhase.FOLLICULAR)
    val currentPhase: StateFlow<CyclePhase> = _currentPhase.asStateFlow()

    fun selectCategory(category: TipCategory?) {
        _selectedCategory.value = category
    }

    fun getFilteredTips(): List<HealthTip> {
        val allTips = getSampleTips()
        val filtered = if (_selectedCategory.value != null) {
            allTips.filter { it.category == _selectedCategory.value }
        } else {
            allTips
        }

        // Sort by relevance to current phase
        return filtered.sortedByDescending { tip ->
            if (tip.relevantPhases.contains(_currentPhase.value)) 2 else 1
        }
    }

    private fun getSampleTips(): List<HealthTip> = listOf(
        HealthTip(
            id = "1",
            title = "Best Foods for Energy",
            description = "Boost your energy naturally with these nutrient-rich foods perfect for your follicular phase",
            emoji = "🥗",
            category = TipCategory.NUTRITION,
            readTime = 5,
            relevantPhases = listOf(CyclePhase.FOLLICULAR, CyclePhase.OVULATION),
            content = "During the follicular phase, your body needs..."
        ),
        HealthTip(
            id = "2",
            title = "Yoga Poses for Cramp Relief",
            description = "Gentle stretches and poses to ease menstrual discomfort",
            emoji = "🧘‍♀️",
            category = TipCategory.EXERCISE,
            readTime = 3,
            relevantPhases = listOf(CyclePhase.MENSTRUAL),
            content = "These gentle yoga poses can help..."
        ),
        HealthTip(
            id = "3",
            title = "Managing PMS Naturally",
            description = "Evidence-based strategies to reduce PMS symptoms",
            emoji = "🌸",
            category = TipCategory.SYMPTOMS,
            readTime = 7,
            relevantPhases = listOf(CyclePhase.LUTEAL),
            content = "PMS affects many women differently..."
        ),
        HealthTip(
            id = "4",
            title = "Sleep Better During Your Cycle",
            description = "How hormones affect sleep and what you can do about it",
            emoji = "😴",
            category = TipCategory.LIFESTYLE,
            readTime = 4,
            relevantPhases = listOf(CyclePhase.LUTEAL, CyclePhase.MENSTRUAL),
            content = "Hormonal fluctuations can significantly impact..."
        ),
        HealthTip(
            id = "5",
            title = "Mindfulness for Hormonal Balance",
            description = "Simple meditation techniques for every phase of your cycle",
            emoji = "🧠",
            category = TipCategory.MENTAL_HEALTH,
            readTime = 6,
            relevantPhases = listOf(CyclePhase.FOLLICULAR, CyclePhase.LUTEAL, CyclePhase.OVULATION, CyclePhase.MENSTRUAL),
            content = "Mindfulness practices can help regulate..."
        ),
        HealthTip(
            id = "6",
            title = "High-Energy Workouts for Ovulation",
            description = "Make the most of your peak energy during ovulation",
            emoji = "💪",
            category = TipCategory.EXERCISE,
            readTime = 5,
            relevantPhases = listOf(CyclePhase.OVULATION),
            content = "During ovulation, your energy peaks..."
        ),
        HealthTip(
            id = "7",
            title = "Iron-Rich Meals for Your Period",
            description = "Replenish iron levels with these delicious recipes",
            emoji = "🥘",
            category = TipCategory.NUTRITION,
            readTime = 4,
            relevantPhases = listOf(CyclePhase.MENSTRUAL),
            content = "Iron loss during menstruation can leave you..."
        ),
        HealthTip(
            id = "8",
            title = "Stress Management Throughout Your Cycle",
            description = "Phase-specific techniques to manage stress and anxiety",
            emoji = "🌿",
            category = TipCategory.MENTAL_HEALTH,
            readTime = 8,
            relevantPhases = listOf(CyclePhase.FOLLICULAR, CyclePhase.LUTEAL, CyclePhase.OVULATION, CyclePhase.MENSTRUAL),
            content = "Stress affects your cycle, and your cycle..."
        )
    )
}