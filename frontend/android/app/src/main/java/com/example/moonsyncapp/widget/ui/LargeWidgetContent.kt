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
 * Large widget layout (4×2 grid size).
 */
@Composable
fun LargeWidgetContent(state: WidgetState, isDarkMode: Boolean) {
    if (!state.isDataFresh) {
        LargeWidgetStaleContent(state, isDarkMode)
    } else {
        LargeWidgetFreshContent(state, isDarkMode)
    }
}

@Composable
private fun LargeWidgetFreshContent(state: WidgetState, isDarkMode: Boolean) {
    val colorScheme = PhaseColorScheme.forPhase(state.phase, isDarkMode)
    val surfaceColor = if (isDarkMode) WidgetTheme.Dark.Background else WidgetTheme.Light.Background
    val textPrimary = if (isDarkMode) WidgetTheme.Dark.TextPrimary else WidgetTheme.Light.TextPrimary
    val textSecondary = if (isDarkMode) WidgetTheme.Dark.TextSecondary else WidgetTheme.Light.TextSecondary
    val buttonBg = if (isDarkMode) WidgetTheme.Dark.Primary else WidgetTheme.Light.Primary

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
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(WidgetDimensions.WIDGET_PADDING_LARGE.dp)
        ) {
            // Top section - Cycle info and countdown
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Left: Cycle day and phase
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = state.cycleDayNumber,
                            style = TextStyle(
                                fontSize = WidgetDimensions.TEXT_SIZE_HERO.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(textPrimary)
                            )
                        )
                        Spacer(modifier = GlanceModifier.width(WidgetDimensions.SPACING_SM.dp))
                        Column {
                            Text(
                                text = "Day of ${state.cycleLength}",
                                style = TextStyle(
                                    fontSize = WidgetDimensions.TEXT_SIZE_BODY.sp,
                                    color = ColorProvider(textSecondary)
                                )
                            )
                            Text(
                                text = state.displayPhaseLabel,
                                style = TextStyle(
                                    fontSize = WidgetDimensions.TEXT_SIZE_MEDIUM.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ColorProvider(colorScheme.accent)
                                )
                            )
                        }
                    }
                }

                // Right: Countdown
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = state.primaryCountdown.emoji,
                        style = TextStyle(
                            fontSize = WidgetDimensions.TEXT_SIZE_MEDIUM.sp,
                            color = ColorProvider(textPrimary)
                        )
                    )
                    Text(
                        text = state.primaryCountdown.shortText,
                        style = TextStyle(
                            fontSize = WidgetDimensions.TEXT_SIZE_MEDIUM.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorProvider(textPrimary)
                        )
                    )
                    if (state.showDetailedInfo) {
                        Text(
                            text = state.primaryCountdown.label,
                            style = TextStyle(
                                fontSize = WidgetDimensions.TEXT_SIZE_SMALL.sp,
                                color = ColorProvider(textSecondary)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = GlanceModifier.height(WidgetDimensions.SPACING_MD.dp))

            // Progress bar
            PhaseProgressBar(
                progress = state.phaseProgress,
                colorScheme = colorScheme
            )

            Spacer(modifier = GlanceModifier.height(WidgetDimensions.SPACING_MD.dp))

            // Bottom section - Advice and action
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (state.showDetailedInfo) state.phaseAdvice else "Cycle tracking",
                    style = TextStyle(
                        fontSize = WidgetDimensions.TEXT_SIZE_BODY.sp,
                        color = ColorProvider(textSecondary)
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )

                Spacer(modifier = GlanceModifier.width(WidgetDimensions.SPACING_MD.dp))

                // Log button — goes directly to Logging screen
                Box(
                    modifier = GlanceModifier
                        .cornerRadius(WidgetDimensions.RADIUS_SM.dp)
                        .background(ColorProvider(buttonBg))
                        .padding(
                            horizontal = WidgetDimensions.SPACING_MD.dp,
                            vertical = WidgetDimensions.SPACING_SM.dp
                        )
                        .clickable(onClick = actionStartActivity(
                            Intent().apply {
                                setClassName(
                                    "com.example.moonsyncapp",
                                    "com.example.moonsyncapp.MainActivity"
                                )
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                putExtra("route", "logging")
                            }
                        ))
                ) {
                    Text(
                        text = "Log →",
                        style = TextStyle(
                            fontSize = WidgetDimensions.TEXT_SIZE_BODY.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorProvider(WidgetTheme.ButtonText)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PhaseProgressBar(
    progress: Float,
    colorScheme: PhaseColorScheme
) {
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(WidgetDimensions.PROGRESS_HEIGHT.dp)
            .cornerRadius(WidgetDimensions.PROGRESS_RADIUS.dp)
            .background(ColorProvider(colorScheme.progressTrack))
    ) {
        Row(modifier = GlanceModifier.fillMaxSize()) {
            if (progress > 0f) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxHeight()
                        .width((progress * 200).toInt().dp)
                        .cornerRadius(WidgetDimensions.PROGRESS_RADIUS.dp)
                        .background(ColorProvider(colorScheme.progress))
                ) {
                    // Empty box for progress fill
                }
            }
        }
    }
}

@Composable
private fun LargeWidgetStaleContent(state: WidgetState, isDarkMode: Boolean) {
    val colorScheme = PhaseColorScheme.stale(isDarkMode)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(WidgetDimensions.RADIUS_LG.dp)
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
                .padding(WidgetDimensions.WIDGET_PADDING_LARGE.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = state.stalePromptEmoji,
                style = TextStyle(
                    fontSize = WidgetDimensions.TEXT_SIZE_HERO.sp,
                    color = ColorProvider(colorScheme.textPrimary),
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = GlanceModifier.height(WidgetDimensions.SPACING_MD.dp))

            Text(
                text = state.stalePromptFull,
                style = TextStyle(
                    fontSize = WidgetDimensions.TEXT_SIZE_MEDIUM.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(colorScheme.textPrimary),
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = GlanceModifier.height(WidgetDimensions.SPACING_SM.dp))

            Text(
                text = "Tap anywhere to open",
                style = TextStyle(
                    fontSize = WidgetDimensions.TEXT_SIZE_BODY.sp,
                    color = ColorProvider(colorScheme.textSecondary),
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}