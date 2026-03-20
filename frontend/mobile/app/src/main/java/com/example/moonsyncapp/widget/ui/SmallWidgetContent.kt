package com.example.moonsyncapp.widget.ui

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
//import androidx.glance.ColorProvider
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.example.moonsyncapp.MainActivity
import com.example.moonsyncapp.widget.WidgetState
import com.example.moonsyncapp.widget.theme.PhaseColorScheme
import com.example.moonsyncapp.widget.theme.WidgetDimensions
import com.example.moonsyncapp.widget.theme.WidgetTheme
import androidx.glance.unit.ColorProvider
import androidx.glance.appwidget.action.actionStartActivity

/**
 * Small widget layout (2×2 grid size).
 */
@Composable
fun SmallWidgetContent(state: WidgetState, isDarkMode: Boolean) {
    if (!state.isDataFresh) {
        SmallWidgetStaleContent(state, isDarkMode)
    } else {
        SmallWidgetFreshContent(state, isDarkMode)
    }
}

@Composable
private fun SmallWidgetFreshContent(state: WidgetState, isDarkMode: Boolean) {
    val colorScheme = PhaseColorScheme.forPhase(state.phase, isDarkMode)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(WidgetDimensions.RADIUS_LG.dp)
            .background(ColorProvider(colorScheme.background))
            .clickable(onClick = actionStartActivity(
                Intent().apply {
                    setClassName(
                        "com.example.moonsyncapp",
                        "com.example.moonsyncapp.MainActivity"
                    )
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            )),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(WidgetDimensions.WIDGET_PADDING_SMALL.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero cycle day number
            Text(
                text = state.cycleDayNumber,
                style = TextStyle(
                    fontSize = WidgetDimensions.TEXT_SIZE_HERO.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(colorScheme.textPrimary),
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = GlanceModifier.height(WidgetDimensions.SPACING_XS.dp))

            // Phase emoji and "Day" label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.displayPhaseLabel,
                    style = TextStyle(
                        fontSize = WidgetDimensions.TEXT_SIZE_MEDIUM.sp,
                        color = ColorProvider(colorScheme.textPrimary),
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = GlanceModifier.width(WidgetDimensions.SPACING_SM.dp))

                Text(
                    text = "Day",
                    style = TextStyle(
                        fontSize = WidgetDimensions.TEXT_SIZE_MEDIUM.sp,
                        color = ColorProvider(colorScheme.textSecondary),
                        textAlign = TextAlign.Center
                    )
                )
            }

            // Subtle accent line in privacy mode
            if (!state.showDetailedInfo) {
                Spacer(modifier = GlanceModifier.height(WidgetDimensions.SPACING_SM.dp))

                Box(
                    modifier = GlanceModifier
                        .width(32.dp)
                        .height(2.dp)
                        .cornerRadius(1.dp)
                        .background(ColorProvider(colorScheme.accent))
                ) {
                    // Empty box for accent line
                }
            }
        }
    }
}

@Composable
private fun SmallWidgetStaleContent(state: WidgetState, isDarkMode: Boolean) {
    val colorScheme = PhaseColorScheme.stale(isDarkMode)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(WidgetDimensions.RADIUS_LG.dp)
            .background(ColorProvider(colorScheme.background))
            .clickable(onClick = actionStartActivity(
                Intent().apply {
                    setClassName(
                        "com.example.moonsyncapp",
                        "com.example.moonsyncapp.MainActivity"
                    )
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            )),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(WidgetDimensions.WIDGET_PADDING_SMALL.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = state.stalePromptEmoji,
                style = TextStyle(
                    fontSize = WidgetDimensions.TEXT_SIZE_LARGE.sp,
                    color = ColorProvider(colorScheme.textPrimary),
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = GlanceModifier.height(WidgetDimensions.SPACING_SM.dp))

            Text(
                text = state.stalePromptShort,
                style = TextStyle(
                    fontSize = WidgetDimensions.TEXT_SIZE_BODY.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(colorScheme.textPrimary),
                    textAlign = TextAlign.Center
                )
            )

            Text(
                text = "Tap to open",
                style = TextStyle(
                    fontSize = WidgetDimensions.TEXT_SIZE_SMALL.sp,
                    color = ColorProvider(colorScheme.textSecondary),
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}