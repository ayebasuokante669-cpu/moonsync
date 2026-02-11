package com.example.moonsyncapp.widget

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import com.example.moonsyncapp.widget.ui.LargeWidgetContent
import com.example.moonsyncapp.widget.ui.MediumWidgetContent
import com.example.moonsyncapp.widget.ui.SmallWidgetContent
import kotlinx.serialization.json.Json

/**
 * Main MoonSync widget implementation.
 *
 * Handles all three widget sizes through responsive layout selection.
 * Reads state from DataStore preferences.
 */
class MoonSyncWidget : GlanceAppWidget() {

    companion object {
        private val WIDGET_STATE_KEY = stringPreferencesKey("widget_state")

        // Size breakpoints for layout selection
        val SMALL_MAX_WIDTH = 150.dp
        val MEDIUM_MAX_WIDTH = 220.dp
    }

    override val stateDefinition: GlanceStateDefinition<*> =
        PreferencesGlanceStateDefinition

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(110.dp, 110.dp),
            DpSize(180.dp, 110.dp),
            DpSize(250.dp, 110.dp)
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val stateJson = prefs[WIDGET_STATE_KEY]

            // Parse state or use empty state as fallback
            val widgetState = try {
                if (stateJson != null) {
                    Json.decodeFromString<WidgetState>(stateJson)
                } else {
                    WidgetState.empty()
                }
            } catch (e: Exception) {
                WidgetState.empty()
            }

            // Detect dark mode
            val isDarkMode = (context.resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

            // Detect size and route to appropriate layout
            val size = androidx.glance.LocalSize.current

            when {
                size.width < SMALL_MAX_WIDTH -> {
                    SmallWidgetContent(state = widgetState, isDarkMode = isDarkMode)
                }
                size.width < MEDIUM_MAX_WIDTH -> {
                    MediumWidgetContent(state = widgetState, isDarkMode = isDarkMode)
                }
                else -> {
                    LargeWidgetContent(state = widgetState, isDarkMode = isDarkMode)
                }
            }
        }
    }

    /**
     * Update all widget instances.
     */
    suspend fun updateAll(context: Context) {
        try {
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(this.javaClass)
            glanceIds.forEach { glanceId ->
                update(context, glanceId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}