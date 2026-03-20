package com.example.moonsyncapp.ui.screens.calendar

import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.moonsyncapp.data.model.CycleData
import com.example.moonsyncapp.data.model.CyclePhase
import com.example.moonsyncapp.data.model.PhaseColors
import com.example.moonsyncapp.navigation.Routes
import com.example.moonsyncapp.ui.components.BottomNavigationBar
import com.example.moonsyncapp.ui.theme.MoonSyncTheme
import com.example.moonsyncapp.ui.viewmodels.CalendarDisplayData
import com.example.moonsyncapp.ui.viewmodels.CalendarViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

/**
 * Theme-aware calendar colors
 * Adapts backgrounds for dark mode readability
 */
object CalendarDateColors {
    // Solid colors (same in both modes)
    val Period = Color(0xFF9C7AC1)
    val PredictedPeriod = Color(0xFFB39DDB)
    val Ovulation = Color(0xFFFF5252)
    val Fertile = Color(0xFFE91E63)

    // Light mode backgrounds
    private val PeriodBackgroundLight = Color(0xFFE6D5F5)
    private val FertileBackgroundLight = Color(0xFFFCE4EC)
    private val PredictedBackgroundLight = Color(0xFFE8DEF8)

    // Dark mode backgrounds (darker, more saturated)
    private val PeriodBackgroundDark = Color(0xFF3D2E4A)
    private val FertileBackgroundDark = Color(0xFF4A2836)
    private val PredictedBackgroundDark = Color(0xFF352840)

    @Composable
    fun periodBackground(): Color {
        return if (isSystemInDarkTheme()) PeriodBackgroundDark else PeriodBackgroundLight
    }

    @Composable
    fun fertileBackground(): Color {
        return if (isSystemInDarkTheme()) FertileBackgroundDark else FertileBackgroundLight
    }

    @Composable
    fun predictedBackground(): Color {
        return if (isSystemInDarkTheme()) PredictedBackgroundDark else PredictedBackgroundLight
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: CalendarViewModel = viewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // Collect state from ViewModel
    val cycleData by viewModel.cycleData.collectAsState()
    val calendarDisplayData by viewModel.calendarDisplayData.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    // Pager for swipeable months
    val currentMonthIndex = 100
    val pagerState = rememberPagerState(
        initialPage = currentMonthIndex,
        pageCount = { 201 }
    )

    val baseMonth = YearMonth.now()
    val displayedMonth = baseMonth.plusMonths((pagerState.currentPage - currentMonthIndex).toLong())
    val today = LocalDate.now()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Add gradient layer if needed (Calendar doesn't have one, but keeping pattern)
        // Box(
        //     modifier = Modifier
        //         .fillMaxSize()
        //         .background(gradient)
        // )

        // ===== ADD OUTER COLUMN WRAPPER =====
        Column(modifier = Modifier.fillMaxSize()) {
            // ===== SCROLLABLE CONTENT WITH WEIGHT =====
            Column(
                modifier = Modifier
                    .weight(1f)  // ← Changed from fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 100.dp),  // ← Moved padding here
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Calendar",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Cycle Phase Card
                CyclePhaseCard(cycleData = cycleData)

                Spacer(modifier = Modifier.height(24.dp))

                // Month Navigation Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous month",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "${displayedMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${displayedMonth.year}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    IconButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next month",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Weekday Headers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                        Text(
                            text = day,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Swipeable Calendar Pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) { page ->
                    val monthToDisplay = baseMonth.plusMonths((page - currentMonthIndex).toLong())

                    CalendarGrid(
                        currentMonth = monthToDisplay,
                        today = today,
                        calendarDisplayData = calendarDisplayData,
                        selectedDate = selectedDate,
                        onDateClick = { date ->
                            viewModel.selectDate(date)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Legend Card
                LegendCard()

                Spacer(modifier = Modifier.height(16.dp))

                // Upcoming Info Card
                UpcomingInfoCard(cycleData = cycleData)

                Spacer(modifier = Modifier.height(24.dp))
            }
        } // ← Closing brace for outer Column

        // Bottom Navigation Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(10f)
        ) {
            BottomNavigationBar(
                currentRoute = currentRoute,
                navController = navController
            )
        }
    } // ← Closing brace for root Box
}

@Composable
private fun CyclePhaseCard(cycleData: CycleData) {
    val phase = cycleData.currentPhase
    val fillColor = PhaseColors.getFillColor(phase)
    val borderColor = PhaseColors.getBorderColor(phase)
    val progressColor = PhaseColors.getProgressColor(phase)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = fillColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Phase Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(borderColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (phase) {
                            CyclePhase.MENSTRUAL -> "🩸"
                            CyclePhase.FOLLICULAR -> "🌱"
                            CyclePhase.OVULATION -> "🥚"
                            CyclePhase.LUTEAL -> "🌙"
                        },
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = phase.displayName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = borderColor  // Keep vibrant border color
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = phase.description,
                        fontSize = 14.sp,
                        color = Color(0xFF666666)  // FIXED: Dark gray on pastel
                    )
                }

                // Cycle Day Badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(borderColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${cycleData.cycleDay}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Phase Progress
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Phase progress",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)  // FIXED: Dark gray
                    )
                    Text(
                        text = "${cycleData.phaseDaysRemaining} days left",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = borderColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { cycleData.phaseProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = progressColor,
                    trackColor = borderColor.copy(alpha = 0.2f)
                )
            }
        }
    }
}
@Composable
private fun CalendarGrid(
    currentMonth: YearMonth,
    today: LocalDate,
    calendarDisplayData: CalendarDisplayData,
    selectedDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val daysInMonth = currentMonth.lengthOfMonth()
    val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7
    val rows = totalCells / 7

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0..6) {
                    val cellIndex = row * 7 + col
                    val dayOfMonth = cellIndex - firstDayOfWeek + 1

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            dayOfMonth in 1..daysInMonth -> {
                                val date = currentMonth.atDay(dayOfMonth)
                                val isToday = date == today
                                val isPeriod = date in calendarDisplayData.periodDates
                                val isPredictedPeriod = date in calendarDisplayData.predictedPeriodDates
                                val isFertile = date in calendarDisplayData.fertileDates
                                val isOvulation = date == calendarDisplayData.ovulationDate
                                val isSelected = date == selectedDate

                                CalendarDateCell(
                                    day = dayOfMonth,
                                    isToday = isToday,
                                    isPeriod = isPeriod,
                                    isPredictedPeriod = isPredictedPeriod,
                                    isFertile = isFertile,
                                    isOvulation = isOvulation,
                                    isSelected = isSelected,
                                    onClick = { onDateClick(date) }
                                )
                            }
                            dayOfMonth < 1 -> {
                                val prevMonth = currentMonth.minusMonths(1)
                                val prevMonthDay = prevMonth.lengthOfMonth() + dayOfMonth
                                Text(
                                    text = prevMonthDay.toString(),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                            }
                            else -> {
                                val nextMonthDay = dayOfMonth - daysInMonth
                                Text(
                                    text = nextMonthDay.toString(),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDateCell(
    day: Int,
    isToday: Boolean,
    isPeriod: Boolean,
    isPredictedPeriod: Boolean,
    isFertile: Boolean,
    isOvulation: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()

    // Background color for range indicators (THEME-AWARE) — MORE SUBTLE
    val subtleAlpha = if (isDarkTheme) 0.5f else 0.35f  // Reduced opacity

    // Background color for range indicators (THEME-AWARE)
    val backgroundColor = when {
        isFertile -> CalendarDateColors.fertileBackground()
        isPeriod -> CalendarDateColors.periodBackground()
        isPredictedPeriod -> CalendarDateColors.predictedBackground()
        else -> Color.Transparent
    }

    // Circle color for specific dates
    val circleColor = when {
        isOvulation -> CalendarDateColors.Ovulation
        isPeriod -> CalendarDateColors.Period
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        else -> Color.Transparent
    }

    // Border for expected period or today
    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isPredictedPeriod && !isPeriod -> CalendarDateColors.PredictedPeriod
        isToday && !isPeriod && !isOvulation && !isSelected -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }

    // Check if this cell has a filled circle (not transparent or very light)
    val hasFilledCircle = isOvulation || isPeriod || isSelected

    // Has a colored background range?
    val hasColoredBackground = isFertile || isPeriod || isPredictedPeriod

    // Determine text color based on context
    val textColor = when {
        // Filled circles always get white text
        hasFilledCircle -> Color.White

        // Today indicator (light circle)
        isToday -> MaterialTheme.colorScheme.primary

        // On colored background in LIGHT mode → dark text
        hasColoredBackground && !isDarkTheme -> Color.Black.copy(alpha = 0.85f)

        // On colored background in DARK mode → light text (backgrounds are now dark)
        hasColoredBackground && isDarkTheme -> Color.White.copy(alpha = 0.9f)

        // Default
        else -> MaterialTheme.colorScheme.onBackground
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(25.dp))
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ){
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(circleColor)
                .then(
                    if (borderColor != Color.Transparent) {
                        Modifier.border(2.dp, borderColor, CircleShape)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.toString(),
                fontSize = 14.sp,
                fontWeight = when {
                    isOvulation || isPeriod || isToday || isSelected -> FontWeight.Bold
                    else -> FontWeight.Normal
                },
                color = textColor
            )
        }
    }
}

//@Composable
//private fun LegendCard() {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surface
//        ),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            Text(
//                text = "Legend",
//                fontSize = 14.sp,
//                fontWeight = FontWeight.SemiBold,
//                color = MaterialTheme.colorScheme.onSurface
//            )
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceEvenly
//            ) {
//                LegendItem(
//                    color = CalendarDateColors.Period,
//                    label = "Period",
//                    modifier = Modifier.weight(1f)
//                )
//                LegendItem(
//                    color = CalendarDateColors.PredictedPeriod,
//                    label = "Predicted",
//                    isBorder = true,
//                    modifier = Modifier.weight(1f)
//                )
//            }
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceEvenly
//            ) {
//                LegendItem(
//                    color = CalendarDateColors.Ovulation,
//                    label = "Ovulation",
//                    modifier = Modifier.weight(1f)
//                )
//                LegendItem(
//                    color = CalendarDateColors.Fertile,
//                    label = "Fertile",
//                    modifier = Modifier.weight(1f)
//                )
//            }
//        }
//    }
//}

@Composable
private fun LegendCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Legend",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Date indicators (circles)
            Text(
                text = "Date Indicators",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(
                    color = CalendarDateColors.Period,
                    label = "Period",
                    modifier = Modifier.weight(1f)
                )
                LegendItem(
                    color = CalendarDateColors.PredictedPeriod,
                    label = "Predicted",
                    isBorder = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(
                    color = CalendarDateColors.Ovulation,
                    label = "Ovulation",
                    modifier = Modifier.weight(1f)
                )
                LegendItem(
                    color = MaterialTheme.colorScheme.primary,
                    label = "Today",
                    isBorder = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Range backgrounds
            Text(
                text = "Date Ranges",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RangeLegendItem(
                    color = CalendarDateColors.fertileBackground(),
                    label = "Fertile Window",
                    modifier = Modifier.weight(1f)
                )
                RangeLegendItem(
                    color = CalendarDateColors.periodBackground(),
                    label = "Period Days",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    isBorder: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .then(
                    if (isBorder) {
                        Modifier
                            .background(Color.Transparent)
                            .border(2.dp, color, CircleShape)
                    } else {
                        Modifier.background(color)
                    }
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RangeLegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Rounded rectangle to show it's a range/background
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.6f))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun UpcomingInfoCard(cycleData: CycleData) {
    val isDarkTheme = isSystemInDarkTheme()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Upcoming",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Next Period
                UpcomingItem(
                    emoji = "🩸",
                    label = "Next Period",
                    value = cycleData.getNextPeriodFormatted(),
                    subValue = "in ${cycleData.daysUntilNextPeriod} days",
                    color = CalendarDateColors.Period,
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Ovulation
                UpcomingItem(
                    emoji = "🥚",
                    label = "Ovulation",
                    value = cycleData.getOvulationFormatted(),
                    subValue = if (cycleData.daysUntilOvulation > 0) {
                        "in ${cycleData.daysUntilOvulation} days"
                    } else {
                        "Today!"
                    },
                    color = CalendarDateColors.Ovulation,
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun UpcomingItem(
    emoji: String,
    label: String,
    value: String,
    subValue: String,
    color: Color,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    // Adjust background for dark mode
    val backgroundColor = if (isDarkTheme) {
        color.copy(alpha = 0.2f)
    } else {
        color.copy(alpha = 0.1f)
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 24.sp)

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = subValue,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CalendarScreenPreview() {
    MoonSyncTheme {
        CalendarScreen(navController = rememberNavController())
    }
}