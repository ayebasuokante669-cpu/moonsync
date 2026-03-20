package com.example.moonsyncapp.ui.screens.healthtips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moonsyncapp.data.model.CyclePhase
import com.example.moonsyncapp.data.model.HealthTip
import com.example.moonsyncapp.data.model.TipCategory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ArticleViewModel : ViewModel() {

    private val _currentArticle = MutableStateFlow<HealthTip?>(null)
    val currentArticle: StateFlow<HealthTip?> = _currentArticle.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    private val _relatedArticles = MutableStateFlow<List<HealthTip>>(emptyList())
    val relatedArticles: StateFlow<List<HealthTip>> = _relatedArticles.asStateFlow()

    fun loadArticle(articleId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            delay(500) // Simulate network delay

            // In real app: fetch from repository by ID
            _currentArticle.value = getSampleArticle(articleId)
            _relatedArticles.value = getRelatedArticles(articleId)

            _isLoading.value = false
        }
    }

    fun toggleSave() {
        _isSaved.value = !_isSaved.value
        // In real app: save to database
    }

    private fun getSampleArticle(id: String): HealthTip {
        // Expanded content for better reading experience
        val article = HealthTip(
            id = id,
            title = "Managing Iron Levels During Your Period",
            description = "During menstruation, you lose iron through blood loss. Learn evidence-based strategies to maintain healthy iron levels throughout your cycle.",
            emoji = "🩸",
            category = TipCategory.NUTRITION,
            readTime = 8,
            relevantPhases = listOf(CyclePhase.MENSTRUAL, CyclePhase.FOLLICULAR),
            content = buildArticleContent()
        )
        return article
    }

    private fun buildArticleContent(): String {
        return """
During your menstrual period, your body loses iron through blood loss. For some women, this can lead to fatigue, weakness, and even anemia over time. Understanding how to maintain healthy iron levels is crucial for your overall wellbeing.

## Why Iron Matters

Iron is essential for producing hemoglobin, the protein in red blood cells that carries oxygen throughout your body. When iron levels drop, you may experience:

• Fatigue and weakness
• Dizziness or lightheadedness
• Cold hands and feet
• Pale skin
• Difficulty concentrating

## How Much Iron Do You Lose?

The average woman loses about 30-40ml of blood during a typical period, which translates to roughly 15-20mg of iron. Heavy periods can result in even greater losses.

> Women with heavy menstrual bleeding are at significantly higher risk of iron deficiency anemia and should be particularly mindful of their iron intake.

## Best Food Sources of Iron

1. Red meat and poultry - Contains heme iron, which is easily absorbed
2. Fish and seafood - Particularly shellfish like clams and oysters
3. Beans and lentils - Great plant-based options
4. Dark leafy greens - Spinach, kale, and collard greens
5. Fortified cereals - Check labels for iron content
6. Pumpkin seeds and quinoa - Nutritious alternatives

## Maximizing Iron Absorption

Pair iron-rich foods with vitamin C sources to enhance absorption:

• Add lemon juice to your spinach salad
• Eat oranges or strawberries with your iron-fortified cereal
• Drink orange juice with meals containing iron

Avoid coffee, tea, and calcium supplements around mealtimes, as they can inhibit iron absorption.

## When to Consider Supplements

If you have heavy periods or struggle to get enough iron from food, talk to your healthcare provider about supplementation. Iron supplements should be taken:

• On an empty stomach when possible
• With vitamin C for better absorption
• At different times than calcium supplements
• Under medical supervision to avoid excess

## Signs You Need More Iron

Watch for these warning signs:

- Persistent fatigue despite adequate sleep
- Unusual cravings for ice or non-food items
- Restless leg syndrome
- Frequent infections
- Rapid heartbeat

If you experience these symptoms, consult your doctor for a blood test to check your iron levels.

## Prevention is Key

Start increasing your iron intake a few days before your expected period and continue through your menstrual phase. This proactive approach can help prevent depletion.

Remember, every woman's needs are different. Listen to your body and work with healthcare professionals to find the right balance for you.
        """.trimIndent()
    }

    private fun getRelatedArticles(currentId: String): List<HealthTip> {
        // Mock related articles - in real app, fetch based on category/phase
        return listOf(
            HealthTip(
                id = "rel_1",
                title = "Yoga Poses for Cramp Relief",
                description = "Gentle stretches and poses to ease menstrual discomfort naturally",
                emoji = "🧘‍♀️",
                category = TipCategory.EXERCISE,
                readTime = 5,
                relevantPhases = listOf(CyclePhase.MENSTRUAL),
                content = "Yoga is one of the most effective natural remedies for menstrual cramps. Certain poses help relax the uterine muscles and improve blood flow."
            ),
            HealthTip(
                id = "rel_2",
                title = "Understanding Your Cycle Phases",
                description = "A complete guide to the four phases of your menstrual cycle",
                emoji = "🌙",
                category = TipCategory.LIFESTYLE,
                readTime = 10,
                relevantPhases = listOf(CyclePhase.MENSTRUAL, CyclePhase.FOLLICULAR, CyclePhase.OVULATION, CyclePhase.LUTEAL),
                content = "Your menstrual cycle is divided into four distinct phases, each with unique hormonal changes and characteristics."
            ),
            HealthTip(
                id = "rel_3",
                title = "Energy Boosting Foods for Your Period",
                description = "Combat period fatigue with these nutrient-dense food choices",
                emoji = "🥗",
                category = TipCategory.NUTRITION,
                readTime = 6,
                relevantPhases = listOf(CyclePhase.MENSTRUAL),
                content = "When your period arrives, your body needs extra nutrients to combat fatigue and maintain energy levels."
            )
        )
    }
}