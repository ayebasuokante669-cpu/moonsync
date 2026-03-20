package com.example.moonsyncapp.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receiver for Large (4×2) MoonSync widget.
 *
 * Separate receiver required by Android to register
 * as a distinct widget option in the widget picker.
 *
 * Uses the same MoonSyncWidget implementation — the widget
 * internally detects its size and renders the appropriate layout.
 */
class MoonSyncLargeWidgetReceiver : GlanceAppWidgetReceiver() {

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
        // Only cancel if NO widget instances remain (any size)
    }
}