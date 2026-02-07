package com.example.moonsyncapp.ui.screens.setup

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.moonsyncapp.navigation.Routes
import com.example.moonsyncapp.ui.theme.MoonSyncTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PillDateScreen(navController: NavHostController) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now().minusDays(3)) }
    var isNavigating by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // Pager for swipeable months (100 months back, 100 months forward)
    val currentMonthIndex = 100
    val pagerState = rememberPagerState(
        initialPage = currentMonthIndex,
        pageCount = { 201 }
    )

    // Calculate YearMonth from pager position
    val baseMonth = YearMonth.now()
    val displayedMonth = baseMonth.plusMonths((pagerState.currentPage - currentMonthIndex).toLong())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        // Back Button
        IconButton(
            onClick = {
                if (!isNavigating) {
                    isNavigating = true
                    navController.navigateUp()
                }
            },
            enabled = !isNavigating,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .zIndex(1f)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Go back",
                tint = if (isNavigating)
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                else
                    MaterialTheme.colorScheme.onBackground
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Main Heading
            Text(
                text = "When did you take the main pill in\nyour ongoing pack?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle
            Text(
                text = "This assists us with understanding your\npersonal designs better",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

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
                    .height(280.dp)
            ) { page ->
                val monthToDisplay = baseMonth.plusMonths((page - currentMonthIndex).toLong())

                PillCalendarMonth(
                    yearMonth = monthToDisplay,
                    selectedDate = selectedDate,
                    onDateSelect = { date -> selectedDate = date }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selection display
            if (selectedDate != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Selected: ${formatPillDate(selectedDate!!)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // NEXT Button
            Button(
                onClick = {
                    if (!isNavigating) {
                        isNavigating = true
                        navController.navigate(Routes.SYMPTOMS)
                    }
                },
                enabled = !isNavigating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "NEXT",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PillCalendarMonth(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    onDateSelect: (LocalDate) -> Unit
) {
    val today = LocalDate.now()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val firstDayOfMonth = yearMonth.atDay(1)
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
        val daysInMonth = yearMonth.lengthOfMonth()
        val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7
        val rows = totalCells / 7

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
                                val date = yearMonth.atDay(dayOfMonth)
                                val isSelected = date == selectedDate
                                val isToday = date == today
                                val isFuture = date > today

                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected -> MaterialTheme.colorScheme.primary
                                                isToday -> MaterialTheme.colorScheme.surfaceVariant
                                                else -> Color.Transparent
                                            }
                                        )
                                        .then(
                                            if (!isFuture) {
                                                Modifier.clickable { onDateSelect(date) }
                                            } else {
                                                Modifier
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayOfMonth.toString(),
                                        fontSize = 14.sp,
                                        fontWeight = when {
                                            isSelected -> FontWeight.Bold
                                            isToday -> FontWeight.SemiBold
                                            else -> FontWeight.Normal
                                        },
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                            isFuture -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                            isToday -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.onBackground
                                        }
                                    )
                                }
                            }
                            dayOfMonth < 1 -> {
                                // Previous month days
                                val prevMonth = yearMonth.minusMonths(1)
                                val prevMonthDay = prevMonth.lengthOfMonth() + dayOfMonth
                                Text(
                                    text = prevMonthDay.toString(),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                            }
                            else -> {
                                // Next month days
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

private fun formatPillDate(date: LocalDate): String {
    val month = date.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    return "$month ${date.dayOfMonth}, ${date.year}"
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PillDateScreenPreview() {
    MoonSyncTheme {
        PillDateScreen(navController = rememberNavController())
    }
}