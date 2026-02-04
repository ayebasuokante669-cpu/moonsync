package com.example.moonsyncapp.ui.screens.home

import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import kotlinx.coroutines.flow.first
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Rect
import com.example.moonsyncapp.data.OnboardingManager
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.moonsyncapp.R
import com.example.moonsyncapp.data.model.CycleData
import com.example.moonsyncapp.data.model.CyclePhase
import com.example.moonsyncapp.data.model.PhaseColors
import com.example.moonsyncapp.navigation.Routes
import com.example.moonsyncapp.ui.components.BottomNavigationBar
import com.example.moonsyncapp.ui.theme.MoonSyncTheme
import kotlinx.coroutines.flow.first
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

object HomeColors {
    val AdviceCardBg = Color(0xFFF8F4FC)
    val AdviceCardGlass = Color(0xCCFFFFFF)
    val CommunityGradientStart = Color(0xFF7B5EA7)
    val CommunityGradientEnd = Color(0xFF9575CD)
    val AccentPink = Color(0xFFE91E63)
    val SoftPink = Color(0xFFFFE4EC)
    val SkeletonBase = Color(0xFFE0E0E0)
    val SkeletonHighlight = Color(0xFFF5F5F5)

    // Hexagon text color - always dark for pastel backgrounds
    val HexagonTextPrimary = Color(0xFF2D2D2D)
    val HexagonTextSecondary = Color(0xFF666666)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: CycleViewModel = viewModel(),
    onboardingManager: OnboardingManager? = null
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val scrollState = rememberScrollState()
    val cycleData by viewModel.cycleData.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasUnreadNotifications by viewModel.hasUnreadNotifications.collectAsState()
    val cycleStreak by viewModel.cycleStreak.collectAsState()
// Replace the existing intro state management with:
    val scope = rememberCoroutineScope()
    var showIntro by remember { mutableStateOf(false) }
    var introStep by remember { mutableStateOf(0) }

// Check if intro should be shown - AFTER loading completes
    LaunchedEffect(isLoading) {
        if (!isLoading && onboardingManager != null) {
            val shown = onboardingManager.isHomeIntroShown.first()
            if (!shown) {
                kotlinx.coroutines.delay(600)
                showIntro = true
            }
        }
    }
    val pagerState = rememberPagerState(pageCount = { 2 })
    val haptic = LocalHapticFeedback.current
    val pullRefreshState = rememberPullToRefreshState()

    // Calculate header collapse progress based on scroll
    val headerCollapseProgress = (scrollState.value / 200f).coerceIn(0f, 1f)

    // Haptic feedback on page change
    LaunchedEffect(pagerState.currentPage) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    // Staggered animation states
    var showHexagon by remember { mutableStateOf(false) }
    var showAdviceCard by remember { mutableStateOf(false) }
    var showCommunityCard by remember { mutableStateOf(false) }

    LaunchedEffect(isLoading) {
        if (!isLoading) {
            showHexagon = true
            kotlinx.coroutines.delay(200)
            showAdviceCard = true
            kotlinx.coroutines.delay(200)
            showCommunityCard = true
        }
    }

    val backgroundTint = PhaseColors.getBackgroundTint(cycleData.currentPhase)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Warm gradient background (separate layer)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundTint)
        )

        // ===== ADD THIS OUTER COLUMN =====
        Column(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                // Skeleton Loading State
                SkeletonLoadingContent()
            } else {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    state = pullRefreshState,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(bottom = 100.dp)
                    ) {
                        // Collapsing Header
                        CollapsingHeaderSection(
                            greeting = viewModel.getGreeting(),
                            greetingEmoji = viewModel.getGreetingEmoji(),
                            userName = cycleData.userName,
                            currentPhase = cycleData.currentPhase,
                            cycleStreak = cycleStreak,
                            hasUnreadNotifications = hasUnreadNotifications,
                            collapseProgress = headerCollapseProgress,
                            navController = navController,
                            onNotificationClick = {
                                viewModel.markNotificationsRead()
                                navController.navigate(Routes.NOTIFICATIONS)
                            },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                        )

                        // Animated Hexagon Section
                        AnimatedVisibility(
                            visible = showHexagon,
                            enter = fadeIn(tween(500)) + slideInVertically(
                                animationSpec = tween(500),
                                initialOffsetY = { it / 4 }
                            )
                        ) {
                            CycleOverviewSection(
                                cycleData = cycleData,
                                pagerState = pagerState,
                                viewModel = viewModel,
                                navController = navController,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Animated Advice Card
                        AnimatedVisibility(
                            visible = showAdviceCard,
                            enter = fadeIn(tween(500)) + slideInVertically(
                                animationSpec = tween(500),
                                initialOffsetY = { it / 3 }
                            )
                        ) {
                            AdviceCard(
                                currentPhase = cycleData.currentPhase,
                                onReadMoreClick = { navController.navigate(Routes.HEALTH_TIPS) },
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Animated Community Card
                        AnimatedVisibility(
                            visible = showCommunityCard,
                            enter = fadeIn(tween(500)) + slideInVertically(
                                animationSpec = tween(500),
                                initialOffsetY = { it / 3 }
                            )
                        ) {
                            CommunityCard(
                                onJoinClick = {
                                    navController.navigate(Routes.COMMUNITY) {
                                        popUpTo(Routes.HOME) {
                                            inclusive = false
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        } // ← Closing brace for outer Column

        // Momo FAB
        if (!isLoading) {
            MomoChatFAB(
                onClick = { navController.navigate(Routes.MOMO_CHAT) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 110.dp)
            )
        }

        // Bottom Navigation
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

        // Home Screen Intro Overlay
        if (showIntro && !isLoading) {
            HomeIntroOverlay(
                currentStep = introStep,
                onNextStep = {
                    if (introStep < 2) {
                        introStep++
                    } else {
                        showIntro = false
                        scope.launch {
                            onboardingManager?.markHomeIntroShown()
                        }
                    }
                },
                onSkip = {
                    showIntro = false
                    scope.launch {
                        onboardingManager?.markHomeIntroShown()
                    }
                }
            )
        }
    }
}

@Composable
private fun CollapsingHeaderSection(
    greeting: String,
    greetingEmoji: String,
    userName: String,
    currentPhase: CyclePhase,
    cycleStreak: Int,
    hasUnreadNotifications: Boolean,
    collapseProgress: Float,
    navController: NavController,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    // Interpolate text sizes
    val greetingSize = lerp(24.sp, 18.sp, collapseProgress)
    val phaseAlpha = 1f - collapseProgress

    Row(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            // Main greeting
            Text(
                text = if (collapseProgress < 0.5f) {
                    "$greeting, $userName $greetingEmoji"
                } else {
                    "$userName $greetingEmoji • 🔥 $cycleStreak"
                },
                fontSize = greetingSize,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Phase and streak
            AnimatedVisibility(
                visible = collapseProgress < 0.8f
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.alpha(phaseAlpha)
                ) {
                    Text(
                        text = currentPhase.displayName,
                        fontSize = 14.sp,
                        color = PhaseColors.getBorderColor(currentPhase),
                        fontWeight = FontWeight.Medium
                    )
                    if (collapseProgress < 0.3f) {
                        Text(
                            text = " • 🔥 $cycleStreak month${if (cycleStreak != 1) "s" else ""}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        IconButton(
            onClick = onNotificationClick
        ) {
            Box {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(26.dp)
                )
                if (hasUnreadNotifications) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(HomeColors.AccentPink)
                            .align(Alignment.TopEnd)
                    )
                }
            }
        }
    }
}

@Composable
private fun SkeletonLoadingContent() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerTranslateAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val shimmerGradient = Brush.horizontalGradient(
        colors = listOf(
            HomeColors.SkeletonBase,
            HomeColors.SkeletonHighlight,
            HomeColors.SkeletonBase
        ),
        startX = shimmerTranslateAnim - 500f,
        endX = shimmerTranslateAnim
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .height(24.dp)
                .width(200.dp)
                .background(shimmerGradient, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .height(16.dp)
                .width(150.dp)
                .background(shimmerGradient, RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.CenterHorizontally)
                .background(shimmerGradient, CircleShape)
        )

        Spacer(modifier = Modifier.height(32.dp))

        repeat(2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(shimmerGradient, RoundedCornerShape(20.dp))
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CycleOverviewSection(
    cycleData: CycleData,
    pagerState: androidx.compose.foundation.pager.PagerState,
    viewModel: CycleViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cycle Overview",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            IconButton(
                onClick = {
                    navController.navigate(Routes.CALENDAR) {
                        popUpTo(Routes.HOME) {
                            saveState = true
                            inclusive = false
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = "Calendar",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) { page ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when (page) {
                    0 -> CurrentPhaseHexagon(cycleData = cycleData)
                    1 -> NextEventHexagon(
                        nextEventInfo = viewModel.getNextEventInfo(cycleData),
                        cycleData = cycleData
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(2) { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (pagerState.currentPage == index) {
                                PhaseColors.getBorderColor(cycleData.currentPhase)
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            }
                        )
                )
                if (index < 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}

@Composable
private fun CurrentPhaseHexagon(
    cycleData: CycleData,
    modifier: Modifier = Modifier
) {
    val phase = cycleData.currentPhase
    val fillColor = PhaseColors.getFillColor(phase)
    val borderColor = PhaseColors.getBorderColor(phase)
    val progressColor = PhaseColors.getProgressColor(phase)

    var animatedProgress by remember { mutableStateOf(0f) }
    LaunchedEffect(cycleData.phaseProgress) {
        animatedProgress = cycleData.phaseProgress
    }
    val progress by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "progressAnimation"
    )

    SoftHexagonWithProgress(
        progress = progress,
        fillColor = fillColor,
        borderColor = borderColor,
        progressColor = progressColor,
        modifier = modifier.size(260.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = phase.displayName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = borderColor // Keep border color (vibrant)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Day ${cycleData.cycleDay}",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = HomeColors.HexagonTextPrimary // FIXED: Dark text on pastel
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "of ${cycleData.cycleLength}-day cycle",
                fontSize = 14.sp,
                color = HomeColors.HexagonTextSecondary // FIXED: Dark secondary text
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = phase.description,
                fontSize = 13.sp,
                color = HomeColors.HexagonTextSecondary, // FIXED: Dark text
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NextEventHexagon(
    nextEventInfo: NextEventInfo,
    cycleData: CycleData,
    modifier: Modifier = Modifier
) {
    val isNextPeriod = nextEventInfo.title == "Next Period"
    val fillColor = if (isNextPeriod) PhaseColors.MenstrualFill else PhaseColors.OvulationFill
    val borderColor = if (isNextPeriod) PhaseColors.MenstrualBorder else PhaseColors.OvulationBorder
    val progressColor = if (isNextPeriod) PhaseColors.MenstrualProgress else PhaseColors.OvulationProgress

    var animatedProgress by remember { mutableStateOf(0f) }
    LaunchedEffect(nextEventInfo.progress) {
        animatedProgress = nextEventInfo.progress.coerceIn(0f, 1f)
    }
    val progress by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "nextEventProgress"
    )

    SoftHexagonWithProgress(
        progress = progress,
        fillColor = fillColor,
        borderColor = borderColor,
        progressColor = progressColor,
        modifier = modifier.size(260.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = nextEventInfo.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = borderColor // Keep border color (vibrant)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = nextEventInfo.countdown,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = HomeColors.HexagonTextPrimary // FIXED: Dark text
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = nextEventInfo.date,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = borderColor // Keep border color (vibrant)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = nextEventInfo.subtitle,
                fontSize = 11.sp,
                color = HomeColors.HexagonTextSecondary, // FIXED: Dark text
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun SoftHexagonWithProgress(
    progress: Float,
    fillColor: Color,
    borderColor: Color,
    progressColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val padding = 8.dp.toPx()
            val hexSize = Size(size.width - padding * 2, size.height - padding * 2)
            val center = Offset(size.width / 2, size.height / 2)

            val hexPath = createSoftHexagonPath(hexSize, center)
            drawPath(
                path = hexPath,
                color = fillColor
            )

            drawPath(
                path = hexPath,
                color = borderColor.copy(alpha = 0.3f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            drawHexagonProgress(
                progress = progress,
                hexSize = hexSize,
                center = center,
                color = progressColor,
                strokeWidth = 4.dp.toPx()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

private fun lerp(start: Offset, end: Offset, fraction: Float): Offset {
    return Offset(
        start.x + (end.x - start.x) * fraction,
        start.y + (end.y - start.y) * fraction
    )
}

private fun createSoftHexagonPath(size: Size, center: Offset): Path {
    val path = Path()
    val radius = min(size.width, size.height) / 2 * 0.88f
    val cornerRounding = 0.14f

    val vertices = mutableListOf<Offset>()
    for (i in 0 until 6) {
        val angle = (PI / 3 * i - PI / 2).toFloat()
        vertices.add(
            Offset(
                center.x + radius * cos(angle),
                center.y + radius * sin(angle)
            )
        )
    }

    val startPoint = lerp(vertices[0], vertices[1], cornerRounding)
    path.moveTo(startPoint.x, startPoint.y)

    for (i in 0 until 6) {
        val currentVertex = vertices[(i + 1) % 6]
        val nextVertex = vertices[(i + 2) % 6]
        val prevVertex = vertices[i]

        val beforeCorner = lerp(prevVertex, currentVertex, 1f - cornerRounding)
        val afterCorner = lerp(currentVertex, nextVertex, cornerRounding)

        path.lineTo(beforeCorner.x, beforeCorner.y)
        path.quadraticBezierTo(
            currentVertex.x, currentVertex.y,
            afterCorner.x, afterCorner.y
        )
    }

    path.close()
    return path
}

private fun DrawScope.drawHexagonProgress(
    progress: Float,
    hexSize: Size,
    center: Offset,
    color: Color,
    strokeWidth: Float
) {
    if (progress <= 0f) return

    val radius = min(hexSize.width, hexSize.height) / 2 * 0.88f
    val cornerRounding = 0.14f

    val vertices = mutableListOf<Offset>()
    for (i in 0 until 6) {
        val angle = (PI / 3 * i - PI / 2).toFloat()
        vertices.add(
            Offset(
                center.x + radius * cos(angle),
                center.y + radius * sin(angle)
            )
        )
    }

    data class PathSegment(
        val start: Offset,
        val end: Offset,
        val isCurve: Boolean,
        val controlPoint: Offset? = null
    )

    val segments = mutableListOf<PathSegment>()

    for (i in 0 until 6) {
        val currentVertex = vertices[i]
        val nextVertex = vertices[(i + 1) % 6]
        val afterNextVertex = vertices[(i + 2) % 6]

        val afterCorner = lerp(currentVertex, nextVertex, cornerRounding)
        val beforeCorner = lerp(currentVertex, nextVertex, 1f - cornerRounding)
        val afterNextCorner = lerp(nextVertex, afterNextVertex, cornerRounding)

        segments.add(PathSegment(afterCorner, beforeCorner, isCurve = false))
        segments.add(PathSegment(beforeCorner, afterNextCorner, isCurve = true, controlPoint = nextVertex))
    }

    var totalLength = 0f
    val segmentLengths = mutableListOf<Float>()

    for (segment in segments) {
        val length = if (segment.isCurve) {
            val control = segment.controlPoint!!
            val len1 = kotlin.math.sqrt(
                (control.x - segment.start.x).let { it * it } +
                        (control.y - segment.start.y).let { it * it }
            )
            val len2 = kotlin.math.sqrt(
                (segment.end.x - control.x).let { it * it } +
                        (segment.end.y - control.y).let { it * it }
            )
            (len1 + len2) * 0.85f
        } else {
            kotlin.math.sqrt(
                (segment.end.x - segment.start.x).let { it * it } +
                        (segment.end.y - segment.start.y).let { it * it }
            )
        }
        segmentLengths.add(length)
        totalLength += length
    }

    val targetLength = totalLength * progress.coerceIn(0f, 1f)
    var drawnLength = 0f

    val progressPath = Path()
    val firstAfterCorner = lerp(vertices[0], vertices[1], cornerRounding)
    progressPath.moveTo(firstAfterCorner.x, firstAfterCorner.y)

    var endPoint = firstAfterCorner

    for ((index, segment) in segments.withIndex()) {
        if (drawnLength >= targetLength) break

        val segmentLength = segmentLengths[index]
        val remainingLength = targetLength - drawnLength

        if (remainingLength >= segmentLength) {
            if (segment.isCurve) {
                progressPath.quadraticBezierTo(
                    segment.controlPoint!!.x, segment.controlPoint.y,
                    segment.end.x, segment.end.y
                )
            } else {
                progressPath.lineTo(segment.end.x, segment.end.y)
            }
            endPoint = segment.end
            drawnLength += segmentLength
        } else {
            val ratio = remainingLength / segmentLength

            if (segment.isCurve) {
                val control = segment.controlPoint!!
                val t = ratio
                val oneMinusT = 1 - t
                endPoint = Offset(
                    oneMinusT * oneMinusT * segment.start.x +
                            2 * oneMinusT * t * control.x +
                            t * t * segment.end.x,
                    oneMinusT * oneMinusT * segment.start.y +
                            2 * oneMinusT * t * control.y +
                            t * t * segment.end.y
                )
                progressPath.lineTo(endPoint.x, endPoint.y)
            } else {
                endPoint = lerp(segment.start, segment.end, ratio)
                progressPath.lineTo(endPoint.x, endPoint.y)
            }
            drawnLength = targetLength
        }
    }

    drawPath(
        path = progressPath,
        color = color,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )

    val circleRadius = strokeWidth * 2f
    drawCircle(
        color = color,
        radius = circleRadius,
        center = endPoint
    )

    drawCircle(
        color = Color.White,
        radius = circleRadius * 0.55f,
        center = endPoint
    )
}

@Composable
private fun AdviceCard(
    currentPhase: CyclePhase,
    onReadMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()

    val (emoji, title, subtitle) = when (currentPhase) {
        CyclePhase.MENSTRUAL -> Triple(
            "🌸",
            "Rest and be gentle with yourself",
            "Warm drinks and light stretching can help ease discomfort"
        )
        CyclePhase.FOLLICULAR -> Triple(
            "💧",
            "Stay hydrated and energized",
            "Great time for trying new activities and social plans"
        )
        CyclePhase.OVULATION -> Triple(
            "✨",
            "You're at your peak energy",
            "Perfect time for important conversations and challenges"
        )
        CyclePhase.LUTEAL -> Triple(
            "🍵",
            "Practice self-care and slow down",
            "Focus on nourishing foods and quality sleep"
        )
    }

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onReadMoreClick() }
                )
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = HomeColors.AdviceCardGlass
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            //Theme-aware gradient
                            if (isDarkTheme) {
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                            } else {
                                Color.White.copy(alpha = 0.9f)
                            },
                            HomeColors.AdviceCardBg.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        )  {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(HomeColors.SoftPink),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Read more →",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CommunityCard(
    onJoinClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "communityCardScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onJoinClick() }
                )
            },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            HomeColors.CommunityGradientStart,
                            HomeColors.CommunityGradientEnd
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Community Activity",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "• Managing PMS naturally\n• Sarah shared her insights\n• Join wellness discussion",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onJoinClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HomeColors.AccentPink
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Join Community",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MomoChatFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fabFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffset"
    )

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .size(64.dp)
            .graphicsLayer { translationY = -floatOffset },
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = Color.White,
        shape = CircleShape,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        Image(
            painter = painterResource(id = R.drawable.momo),
            contentDescription = "Chat with Momo",
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(1f),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun HomeIntroOverlay(
    currentStep: Int,
    onNextStep: () -> Unit,
    onSkip: () -> Unit
) {
    // Get colors OUTSIDE Canvas
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { /* Block touches */ }
            }
    ) {
        // Dim overlay with spotlight cutouts
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spotlightRadius = when (currentStep) {
                0 -> 140.dp.toPx() // Hexagon
                1 -> 80.dp.toPx()  // Header
                2 -> 40.dp.toPx()  // FAB
                else -> 0f
            }

            val spotlightCenter = when (currentStep) {
                0 -> Offset(size.width / 2, size.height * 0.4f) // Center hexagon
                1 -> Offset(size.width * 0.3f, 60.dp.toPx()) // Top left header
                2 -> Offset(size.width - 60.dp.toPx(), size.height - 140.dp.toPx()) // FAB
                else -> Offset.Zero
            }

            // Draw dim overlay
            drawRect(
                color = Color.Black.copy(alpha = 0.85f),
                size = size
            )

            // Cut out spotlight circle (draw with BlendMode.Clear)
            drawCircle(
                color = Color.Transparent,
                radius = spotlightRadius,
                center = spotlightCenter,
                blendMode = BlendMode.Clear
            )

            // Add subtle glow ring
            drawCircle(
                color = primaryColor.copy(alpha = 0.4f),
                radius = spotlightRadius + 4.dp.toPx(),
                center = spotlightCenter,
                style = Stroke(width = 3.dp.toPx())
            )
        }

        // Tooltip with arrow
        when (currentStep) {
            0 -> SpotlightTooltip(
                title = "Your Cycle Tracker ✨",
                description = "This hexagon shows your current cycle day and phase. Swipe to see upcoming events!",
                position = TooltipPosition.BELOW,
                targetY = 0.55f,
                onNext = onNextStep,
                onSkip = onSkip
            )
            1 -> SpotlightTooltip(
                title = "Track Your Streak 🔥",
                description = "Keep logging daily to maintain your streak and get better insights!",
                position = TooltipPosition.BELOW,
                targetY = 0.2f,
                onNext = onNextStep,
                onSkip = onSkip
            )
            2 -> SpotlightTooltip(
                title = "Chat with Momo 💜",
                description = "Tap here anytime to ask questions or get personalized cycle advice!",
                position = TooltipPosition.ABOVE,
                targetY = 0.65f,
                onNext = onNextStep,
                onSkip = onSkip
            )
        }
    }
}

enum class TooltipPosition { ABOVE, BELOW }

@Composable
private fun SpotlightTooltip(
    title: String,
    description: String,
    position: TooltipPosition,
    targetY: Float,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    val isLastStep = title.contains("Momo")

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = BiasAlignment(
            horizontalBias = 0f,
            verticalBias = (targetY * 2) - 1 // Convert 0-1 to -1 to 1
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    TextButton(
                        onClick = onSkip,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "Skip",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Step indicator dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(3) { index ->
                        val stepIndex = when (title) {
                            "Your Cycle Tracker ✨" -> 0
                            "Track Your Streak 🔥" -> 1
                            else -> 2
                        }
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == stepIndex) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    }
                                )
                        )
                        if (index < 2) Spacer(modifier = Modifier.width(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (isLastStep) "Got it! 🎉" else "Next →",
                        modifier = Modifier.padding(vertical = 4.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MoonSyncTheme {
        HomeScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenDarkPreview() {
    MoonSyncTheme(darkTheme = true) {
        HomeScreen(navController = rememberNavController())
    }
}