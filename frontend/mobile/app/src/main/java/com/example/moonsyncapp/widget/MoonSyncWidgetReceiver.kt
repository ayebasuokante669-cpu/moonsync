package com.example.moonsyncapp.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receiver for Small (2×2) MoonSync widget.
 *
 * This is also the "primary" receiver used by WidgetStateRepository
 * for updating all widget instances.
 *
 * Widget lifecycle:
 * - onEnabled: First widget added → schedule refresh worker + refresh data
 * - onDisabled: Last widget removed → worker auto-skips via hasActiveWidgets()
 */
class MoonSyncWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = MoonSyncWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetRefreshHelper.ensureScheduled(context)

        // Refresh widget data immediately when first widget is placed
        CoroutineScope(Dispatchers.IO).launch {
            WidgetRefreshHelper.refreshNow(context)
        }
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // Worker will auto-skip if no active widgets
        // via hasActiveWidgets() check in doWork()
    }
}