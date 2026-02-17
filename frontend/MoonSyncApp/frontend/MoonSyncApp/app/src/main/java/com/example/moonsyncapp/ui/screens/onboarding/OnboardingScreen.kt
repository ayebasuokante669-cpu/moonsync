package com.example.moonsyncapp.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moonsyncapp.R
import com.example.moonsyncapp.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onNavigateToLogin: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()
    val customColors = customColors()

    // REMOVED: AnimatedVisibility wrapper causing blank screen

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Top Section
            TopSection(
                pagerState = pagerState,
                customColors = customColors,
                onPreviousClick = {
                    if (pagerState.currentPage > 0) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                },
                onNextClick = {
                    if (pagerState.currentPage < 3) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            )

            // Content Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(
                    page = page,
                    customColors = customColors
                )
            }

            // Bottom Section - conditional based on current page
            when (pagerState.currentPage) {
                0 -> SkipButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                3 -> GetStartedButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                else -> Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TopSection(
    pagerState: PagerState,
    customColors: CustomColors,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Progress Indicator
        ProgressIndicator(
            currentPage = pagerState.currentPage,
            totalPages = 4,
            customColors = customColors,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Navigation Row
        NavigationRow(
            currentPage = pagerState.currentPage,
            onPreviousClick = onPreviousClick,
            onNextClick = onNextClick
        )
    }
}

@Composable
private fun ProgressIndicator(
    currentPage: Int,
    totalPages: Int,
    customColors: CustomColors,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(totalPages) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (index <= currentPage) {
                            customColors.progressActive
                        } else {
                            customColors.progressInactive
                        }
                    )
            )
        }
    }
}

@Composable
private fun NavigationRow(
    currentPage: Int,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous Button
        if (currentPage > 0) {
            TextButton(
                onClick = onPreviousClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "‹ Previous",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Spacer(modifier = Modifier.width(80.dp))
        }

        // Next Button - hidden on last page
        if (currentPage < 3) {
            TextButton(
                onClick = onNextClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Next ›",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Spacer(modifier = Modifier.width(80.dp))
        }
    }
}

@Composable
private fun SkipButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(
            text = "SKIP",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
private fun GetStartedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        Text(
            text = "Get Started",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun OnboardingPageContent(
    page: Int,
    customColors: CustomColors
) {
    val pageData = when (page) {
        0 -> OnboardingPageData(
            imageRes = R.drawable.onboarding_1,
            title = "Understand Your Body,\nEffortlessly",
            description = "MoonSync helps you track your cycle, moods, and symptoms — all in one calm, private space."
        )
        1 -> OnboardingPageData(
            imageRes = R.drawable.onboarding_2,
            title = "Every Cycle Is Unique —\nSo Are You",
            description = "Log symptoms and lifestyle habits to see how your body truly flows. MoonSync learns and adapts with you."
        )
        2 -> OnboardingPageData(
            imageRes = R.drawable.onboarding_3,
            title = "Get Free Health Advice\n& Consultation",
            description = "Answers to all your period-related questions from trusted health resources."
        )
        else -> OnboardingPageData(
            imageRes = R.drawable.onboarding_4,
            title = "Your Data,\nYour Control",
            description = "Everything you log stays private and secure. MoonSync is here to support, not judge."
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Uniform Image Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(customColors.imageContainer),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = pageData.imageRes),
                contentDescription = pageData.title,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Title
        Text(
            text = pageData.title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = pageData.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.fillMaxWidth(0.9f)
        )
    }
}

private data class OnboardingPageData(
    val imageRes: Int,
    val title: String,
    val description: String
)

// Preview
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    MoonSyncTheme {
        OnboardingScreen(onNavigateToLogin = {})
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun OnboardingScreenDarkPreview() {
    MoonSyncTheme(darkTheme = true) {
        OnboardingScreen(onNavigateToLogin = {})
    }
}