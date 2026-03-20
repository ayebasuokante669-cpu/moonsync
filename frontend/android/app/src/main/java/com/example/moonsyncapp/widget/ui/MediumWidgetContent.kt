package com.example.moonsyncapp.widget.ui

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.glance.unit.ColorProvider
import com.example.moonsyncapp.MainActivity
import com.example.moonsyncapp.widget.WidgetState
import com.example.moonsyncapp.widget.theme.PhaseColorScheme
import com.example.moonsyncapp.widget.theme.WidgetDimensions
import com.example.moonsyncapp.widget.theme.WidgetTheme

/**
 * Medium widget layout (3×2 grid size).
 */
@Composable
fun MediumWidgetContent(state: WidgetState, isDarkMode: Boolean) {
    if (!state.isDataFresh) {
        MediumWidgetStaleContent(state, isDarkMode)
    } else {
        MediumWidgetFreshContent(state, isDarkMode)
    }
}

@Composable
private fun MediumWidgetFreshContent(state: WidgetState, isDarkMode: Boolean) {
    val colorScheme = PhaseColorScheme.forPhase(state.phase, isDarkMode)
    val surfaceColor = if (isDarkMode) WidgetTheme.Dark.Background else WidgetTheme.Light.Background
    val textPrimary = if (isDarkMode) WidgetTheme.Dark.TextPrimary else WidgetTheme.Light.TextPrimary
    val textSecondary = if (isDarkMode) WidgetTheme.Dark.TextSecondary else WidgetTheme.Light.TextSecondary
    val dividerColor = if (isDarkMode) WidgetTheme.Dark.Divider else WidgetTheme.Light.Divider

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(WidgetDimensions.RADIUS_LG.dp)
            .background(ColorProvider(surfaceColor))
            .clickable(onClick = actionStartActivity(
                Intent().apply {
                    setClassName(
                        "com.example.moonsyncapp",
                        "com.example.moonsyncapp.MainActivity"
                    )
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            ))
    ) {
        Row(modifier = GlanceModifier.fillMaxSize()) {
            // Left accent bar
            Box(
                modifier = GlanceModifier
                    .width(WidgetDimensions.ACCENT_BAR_WIDTH.dp)
                    .fillMaxHeight()
                    .background(ColorProvider(colorScheme.accent))
            ) {
                // Empty box for accent bar
            }

            // Main content
            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(WidgetDimensions.WIDGET_PADDING_MEDIUM.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left section - Cycle day and phase
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "Day ",
                            style = TextStyle(
                                fontSize = WidgetDimensions.TEXT_SIZE_BODY.sp,
                                color = ColorProvider(textSecondary)
                            )
                        )
                        Text(
                            text = state.cycleDayNumber,
                            style = TextStyle(
                                fontSize = WidgetDimensions.TEXT_SIZE_LARGE.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(textPrimary)
                            )
                        )
                        Text(
                            text = " of ${state.cycleLength}",
                            style = TextStyle(
                                fontSize = WidgetDimensions.TEXT_SIZE_BODY.sp,
                                color = ColorProvider(textSecondary)
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(WidgetDimensions.SPACING_XS.dp))

                    Text(
                        text = state.displayPhaseLabel,
                        style = TextStyle(
                            fontSize = WidgetDimensions.TEXT_SIZE_MEDIUM.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorProvider(colorScheme.accent)
                        )
                    )
                }

                // Vertical divider
                Box(
                    modifier = GlanceModifier
                        .width(1.dp)
                        .height(48.dp)
                        .background(ColorProvider(dividerColor))
                ) {
                    // Empty box for divider
                }

                Spacer(modifier = GlanceModifier.width(WidgetDimensions.SPACING_MD.dp))

                // Right section - Countdown
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.primaryCountdown.emoji,
                        style = TextStyle(
                            fontSize = WidgetDimensions.TEXT_SIZE_MEDIUM.sp,
                            color = ColorProvider(textPrimary),
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = GlanceModifier.height(WidgetDimensions.SPACING_XS.dp))

                    Text(
                        text = state.primaryCountdown.shortText,
                        style = TextStyle(
                            fontSize = WidgetDimensions.TEXT_SIZE_MEDIUM.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorProvider(textPrimary),
                            textAlign = TextAlign.Center
                        )
                    )

                    if (state.showDetailedInfo) {
                        Text(
                            text = state.primaryCountdown.label,
                            style = TextStyle(
                                fontSize = WidgetDimensions.TEXT_SIZE_SMALL.sp,
                                color = ColorProvider(textSecondary),
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MediumWidgetStaleContent(state: WidgetState, isDarkMode: Boolean) {
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
            ))
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(WidgetDimensions.WIDGET_PADDING_MEDIUM.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = state.stalePromptEmoji,
                style = TextStyle(
                    fontSize = WidgetDimensions.TEXT_SIZE_LARGE.sp,
                    color = ColorProvider(colorScheme.textPrimary)
                )
            )

            Spacer(modifier = GlanceModifier.width(WidgetDimensions.SPACING_MD.dp))

            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = state.stalePromptTitle,
                    style = TextStyle(
                        fontSize = WidgetDimensions.TEXT_SIZE_MEDIUM.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorProvider(colorScheme.textPrimary)
                    )
                )
                Text(
                    text = state.stalePromptSubtitle,
                    style = TextStyle(
                        fontSize = WidgetDimensions.TEXT_SIZE_BODY.sp,
                        color = ColorProvider(colorScheme.textSecondary)
                    )
                )
            }

            Text(
                text = "→",
                style = TextStyle(
                    fontSize = WidgetDimensions.TEXT_SIZE_LARGE.sp,
                    color = ColorProvider(colorScheme.textSecondary)
                )
            )
        }
    }
}