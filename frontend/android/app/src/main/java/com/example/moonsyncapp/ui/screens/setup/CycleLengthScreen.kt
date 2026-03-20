package com.example.moonsyncapp.ui.screens.setup

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.moonsyncapp.navigation.Routes
import com.example.moonsyncapp.ui.components.SafeScreen
import com.example.moonsyncapp.ui.theme.MoonSyncTheme
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moonsyncapp.data.settings.SettingsManager
import com.example.moonsyncapp.data.OnboardingManager

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CycleLengthScreen(navController: NavHostController) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val onboardingManager = remember { OnboardingManager(context) }

    val viewModel: SetupViewModel = viewModel(
        factory = SetupViewModelFactory(settingsManager, onboardingManager, context)
    )
    var isNavigating by remember { mutableStateOf(false) }

    // Cycle range from 14 to 60 days
    val minCycle = 14
    val maxCycle = 60
    val cycleRange = (minCycle..maxCycle).toList()
    val defaultCycle = 28

    // Infinite pager setup
    val pageCount = Int.MAX_VALUE
    val initialPage = pageCount / 2 + (defaultCycle - minCycle)
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { pageCount }
    )

    // Calculate current cycle length
    val currentCycleLength = minCycle + (pagerState.currentPage % cycleRange.size)

    val scope = rememberCoroutineScope()

    SafeScreen(
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Main Heading
            Text(
                text = "How long is your typical\ncycle?",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle
            Text(
                text = "21 to 35 days is commonplace, yet everyone\nis remarkable!",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // Cycle Length Wheel Picker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Selection indicator background
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(56.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {}

                // Scrollable number picker
                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    pageSize = PageSize.Fixed(56.dp),
                    contentPadding = PaddingValues(vertical = 72.dp),
                    beyondViewportPageCount = 2
                ) { page ->
                    val cycleValue = minCycle + (page % cycleRange.size)
                    val isSelected = page == pagerState.currentPage
                    val distance = (page - pagerState.currentPage).absoluteValue

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable {
                                scope.launch {
                                    pagerState.animateScrollToPage(page)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$cycleValue days",
                            fontSize = when (distance) {
                                0 -> 32.sp
                                1 -> 24.sp
                                else -> 18.sp
                            },
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                distance == 1 -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                            },
                            modifier = Modifier.alpha(if (distance > 2) 0.3f else 1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Current selection display (optional, for clarity)
            Text(
                text = "Selected: $currentCycleLength days",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // NEXT Button
            Button(
                onClick = {
                    if (!isNavigating) {
                        isNavigating = true

                        // ✅ SAVE CYCLE LENGTH TO SETTINGS
                        viewModel.saveCycleLength(currentCycleLength)

                        navController.navigate(Routes.PERIOD_DURATION)
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

            Spacer(modifier = Modifier.height(12.dp))

            // NOT SURE Button
            OutlinedButton(
                onClick = {
                    if (!isNavigating) {
                        isNavigating = true
                        navController.navigate(Routes.PERIOD_DURATION)
                    }
                },
                enabled = !isNavigating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                border = ButtonDefaults.outlinedButtonBorder,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground
                )
            ) {
                Text(
                    text = "NOT SURE",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CycleLengthScreenPreview() {
    MoonSyncTheme {
        CycleLengthScreen(navController = rememberNavController())
    }
}