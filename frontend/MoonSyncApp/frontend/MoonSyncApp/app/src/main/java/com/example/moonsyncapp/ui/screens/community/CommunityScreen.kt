package com.example.moonsyncapp.ui.screens.community

import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.moonsyncapp.data.model.*
import com.example.moonsyncapp.navigation.Routes
import com.example.moonsyncapp.ui.components.BottomNavigationBar
import com.example.moonsyncapp.ui.theme.MoonSyncTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// ==========================================
// COMMUNITY COLORS
// ==========================================

object CommunityColors {
    val TextOnPastel = Color(0xFF2D2D2D)
    val SecondaryTextOnPastel = Color(0xFF666666)

    val HeaderGradientStart = Color(0xFF7B5EA7)
    val HeaderGradientEnd = Color(0xFF9575CD)

    val MenstrualRoom = Color(0xFFEC407A)
    val FollicularRoom = Color(0xFF66BB6A)
    val OvulationRoom = Color(0xFFFFA726)
    val LutealRoom = Color(0xFFAB47BC)

    // Freeze token color (blue shield theme)
    val FreezeToken = Color(0xFF42A5F5)

    fun getPhaseRoomColor(phase: CyclePhase): Color {
        return when (phase) {
            CyclePhase.MENSTRUAL -> MenstrualRoom
            CyclePhase.FOLLICULAR -> FollicularRoom
            CyclePhase.OVULATION -> OvulationRoom
            CyclePhase.LUTEAL -> LutealRoom
        }
    }
}

// ==========================================
// MAIN SCREEN
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    navController: NavController,
    viewModel: CommunityViewModel = viewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val selectedTab by viewModel.selectedTab.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val showCreatePostSheet by viewModel.showCreatePostSheet.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val showStreakAtRiskBanner by viewModel.showStreakAtRiskBanner.collectAsState()
    val userStreaks by viewModel.userStreaks.collectAsState()
    val showMilestoneCelebration by viewModel.showMilestoneCelebration.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()

    var showMoodPicker by remember { mutableStateOf(false) }
    var showFreezeTokenInfo by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Lifecycle observer for app resume (daily login streak)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onAppResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Mood picker dialog
    if (showMoodPicker) {
        MoodPickerDialog(
            currentMood = currentUser.currentMood,
            onMoodSelected = { mood ->
                viewModel.updateMood(mood)
                showMoodPicker = false
            },
            onDismiss = { showMoodPicker = false }
        )
    }

    // Freeze token info dialog
    if (showFreezeTokenInfo) {
        FreezeTokenInfoDialog(
            onDismiss = { showFreezeTokenInfo = false }
        )
    }

    // Milestone celebration dialog
    showMilestoneCelebration?.let { celebration ->
        MilestoneCelebrationDialog(
            celebration = celebration,
            onDismiss = { viewModel.dismissMilestoneCelebration() }
        )
    }

    // Create post bottom sheet
    if (showCreatePostSheet) {
        CreatePostBottomSheet(
            currentUser = currentUser,
            onDismiss = { viewModel.hideCreatePost() },
            onPost = { content, category, ageRestriction, phaseTag, isAnonymous ->
                viewModel.createPost(content, category, ageRestriction, phaseTag, isAnonymous)
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Add gradient layer if you want one
        // Box(
        //     modifier = Modifier
        //         .fillMaxSize()
        //         .background(gradient)
        // )

        Column(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CommunitySkeletonLoading()
            } else {
                // Streak at-risk banner
                AnimatedVisibility(
                    visible = showStreakAtRiskBanner,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                ) {
                    StreakAtRiskBanner(
                        streaks = userStreaks,
                        onSaveStreak = {
                            val atRiskStreak = userStreaks.firstOrNull { it.isAtRisk }
                            when (atRiskStreak?.type) {
                                StreakType.COMMUNITY -> viewModel.selectTab(CommunityTab.FEED)
                                StreakType.CHALLENGE -> viewModel.selectTab(CommunityTab.CHALLENGES)
                                else -> {}
                            }
                        }
                    )
                }

                // Header (no back arrow, with mood picker)
                CommunityHeader(
                    currentUser = currentUser,
                    onMoodClick = { showMoodPicker = true }
                )

                // Tab Row (5 tabs)
                CommunityTabRow(
                    selectedTab = selectedTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )

                // Content based on selected tab
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    state = pullRefreshState,
                    modifier = Modifier.weight(1f)  // ← Use weight, NOT fillMaxSize
                ) {
                    when (selectedTab) {
                        CommunityTab.FEED -> FeedTabContent(
                            viewModel = viewModel,
                            navController = navController
                            // Pass padding hint to content if needed
                        )
                        CommunityTab.PHASE_ROOM -> PhaseRoomTabContent(
                            viewModel = viewModel
                        )
                        CommunityTab.SEARCH -> SearchTabContent(
                            viewModel = viewModel,
                            navController = navController
                        )
                        CommunityTab.GROUPS -> GroupsTabContent(
                            viewModel = viewModel,
                            navController = navController
                        )
                        CommunityTab.CHALLENGES -> ChallengesTabContent(
                            viewModel = viewModel,
                            onFreezeTokenInfoClick = { showFreezeTokenInfo = true }
                        )
                    }
                }
            }
        }

        // FAB for creating post
        AnimatedVisibility(
            visible = selectedTab == CommunityTab.FEED && !isLoading,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .zIndex(5f)
                .padding(end = 20.dp, bottom = 110.dp)
        ) {
            FloatingActionButton(
                onClick = { viewModel.showCreatePost() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create post"
                )
            }
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
    }
}

// ==========================================
// HEADER (No back arrow, mood picker)
// ==========================================

@Composable
private fun CommunityHeader(
    currentUser: CommunityUser,
    onMoodClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Title
        Text(
            text = "Community 🌸",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Mood avatar (tap to change mood)
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            CommunityColors.HeaderGradientStart,
                            CommunityColors.HeaderGradientEnd
                        )
                    )
                )
                .clickable { onMoodClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentUser.currentMood?.emoji ?: "🌙",
                fontSize = 22.sp
            )
        }
    }
}

// ==========================================
// MOOD PICKER DIALOG
// ==========================================

@Composable
private fun MoodPickerDialog(
    currentMood: UserMood?,
    onMoodSelected: (UserMood) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "How are you feeling?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Your mood shows on your posts",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(UserMood.values().toList()) { mood ->
                        val isSelected = currentMood == mood

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            },
                            border = if (isSelected) {
                                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                            } else null,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable { onMoodSelected(mood) }
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = mood.emoji,
                                    fontSize = 28.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = mood.label.split(" ").last(),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    }
}

// ==========================================
// FREEZE TOKEN INFO DIALOG
// ==========================================

@Composable
private fun FreezeTokenInfoDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🛡️",
                    fontSize = 48.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Streak Freeze Tokens",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                val infoItems = listOf(
                    "🛡️ Protects your streak if you miss a day",
                    "⚡ Earned at 7, 30, 100 & 365 day milestones",
                    "🔄 Auto-used when you miss a day",
                    "📦 Stack up to save for emergencies",
                    "💎 Keep building your streak worry-free!"
                )

                infoItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = item,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Got it!")
                }
            }
        }
    }
}

// ==========================================
// PROFESSIONAL CATEGORY INFO DIALOG (NEW!)
// ==========================================

@Composable
private fun ProfessionalCategoryInfoDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "Professional Advice",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "This category is reserved for verified healthcare professionals to ensure the safety and accuracy of medical advice.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Are you a healthcare professional?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🩺", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Apply for verification in Settings → Professional Verification",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it")
            }
        }
    )
}

// ==========================================
// TAB ROW (5 tabs)
// ==========================================

@Composable
private fun CommunityTabRow(
    selectedTab: CommunityTab,
    onTabSelected: (CommunityTab) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = CommunityTab.values().indexOf(selectedTab),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 16.dp,
        divider = {}
    ) {
        CommunityTab.values().forEach { tab ->
            val isSelected = selectedTab == tab

            Tab(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                Color.Transparent
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tab.emoji,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tab.title,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

// ==========================================
// STREAK AT-RISK BANNER
// ==========================================

@Composable
private fun StreakAtRiskBanner(
    streaks: List<UserStreak>,
    onSaveStreak: () -> Unit
) {
    val atRiskStreaks = streaks.filter { it.isAtRisk }
    if (atRiskStreaks.isEmpty()) return

    val mostImportantStreak = atRiskStreaks.maxByOrNull { it.currentCount } ?: return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFC107).copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "warningPulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "warningScale"
            )

            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFFF9800),
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${mostImportantStreak.currentCount}-day streak at risk!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${mostImportantStreak.hoursUntilBreak}h left • ${mostImportantStreak.type.label}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (mostImportantStreak.freezeTokensRemaining > 0) {
                    Text(
                        text = "🛡️ ${mostImportantStreak.freezeTokensRemaining} freeze available",
                        fontSize = 12.sp,
                        color = CommunityColors.FreezeToken,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            TextButton(
                onClick = onSaveStreak,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFFFF9800)
                )
            ) {
                Text(
                    text = "Save it",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==========================================
// MILESTONE CELEBRATION DIALOG
// ==========================================

@Composable
private fun MilestoneCelebrationDialog(
    celebration: MilestoneCelebration,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    celebration.milestone.badgeColor.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "celebration")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 0.8f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(500),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "emojiScale"
                    )

                    Text(
                        text = celebration.milestone.emoji,
                        fontSize = 72.sp,
                        modifier = Modifier.graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = celebration.milestone.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = celebration.milestone.badgeColor,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "${celebration.newCount} days strong!",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFD700).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "+${celebration.milestone.pointsReward} points",
                            fontSize = 16.sp,
                            color = Color(0xFFFFB300),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    if (celebration.milestone.earnsFreezeToken) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CommunityColors.FreezeToken.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🛡️", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Earned 1 Streak Freeze!",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = CommunityColors.FreezeToken
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = celebration.milestone.badgeColor
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Amazing! 🎉",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// FEED TAB
// ==========================================

@Composable
private fun FeedTabContent(
    viewModel: CommunityViewModel,
    navController: NavController
) {
    val posts by viewModel.filteredPosts.collectAsState()
    val selectedCategory by viewModel.selectedPostCategory.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var reportPostTarget by remember { mutableStateOf<CommunityPost?>(null) }
    var showShareSheet by remember { mutableStateOf<CommunityPost?>(null) }

    // New report sheet for posts
    reportPostTarget?.let { post ->
        CommunityReportSheet(
            contentType = ContentType.POST,
            context = buildPostReportContext(post),
            onDismissRequest = { reportPostTarget = null },
            onSubmitReport = { reason, notes ->
                viewModel.reportPost(post.id, reason, notes)
                reportPostTarget = null
            }
        )
    }

    // Existing share sheet
    showShareSheet?.let { post ->
        ShareBottomSheet(
            post = post,
            onDismiss = { showShareSheet = null },
            onShareToGroup = { /* TODO: Implement */ },
            onCopyLink = { /* TODO: Implement */ },
            onShareExternal = { /* TODO: Implement */ }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CategoryFilterChips(
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.filterByCategory(it) }
            )
        }

        items(
            items = posts,
            key = { it.id }
        ) { post ->
            PostCard(
                post = post,
                onReact = { reactionType ->
                    viewModel.reactToPost(post.id, reactionType)
                },
                onComment = {
                    navController.navigate(Routes.postDetail(post.id))
                },
                onShare = {
                    showShareSheet = post
                },
                onReport = {
                    reportPostTarget = post
                },
                currentUserId = currentUser.id,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (posts.isEmpty()) {
            item {
                EmptyStateMessage(
                    emoji = "📭",
                    title = "No posts found",
                    subtitle = "Try adjusting your filters or be the first to post!"
                )
            }
        }
    }
}

@Composable
private fun CategoryFilterChips(
    selectedCategory: PostCategory?,
    onCategorySelected: (PostCategory?) -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("All") },
                leadingIcon = { Text("✨", fontSize = 14.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }

        items(PostCategory.values().toList()) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = {
                    onCategorySelected(if (selectedCategory == category) null else category)
                },
                label = { Text(category.displayName) },
                leadingIcon = { Text(category.emoji, fontSize = 14.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = category.color.copy(
                        alpha = if (isDarkTheme) 0.7f else 0.9f
                    ),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

// ==========================================
// POST CARD
// ==========================================

@Composable
private fun PostCard(
    post: CommunityPost,
    onReact: (ReactionType) -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit,
    onReport: () -> Unit,
    currentUserId: String,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    var showReactionPicker by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "postCardScale"
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
                    }
                )
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (post.isPromotedToArticle) {
                if (isDarkTheme) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                }
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (post.isPromotedToArticle) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Author row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    UserAvatar(user = post.author, size = 44.dp)

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = post.author.displayName,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            if (post.author.isVerifiedProfessional) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (post.author.professionalTitle != null) {
                                Text(
                                    text = post.author.professionalTitle,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = " • ",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = formatTimeAgo(post.createdAt),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (post.phaseTag != null) {
                                Text(
                                    text = " • ",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = post.phaseTag.displayName,
                                    fontSize = 12.sp,
                                    color = CommunityColors.getPhaseRoomColor(post.phaseTag)
                                )
                            }
                        }
                    }
                }

                Box {
                    IconButton(
                        onClick = { showOptionsMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false }
                    ) {
                        // Only show report option if not own post
                        if (post.author.id != currentUserId) {
                            DropdownMenuItem(
                                text = { Text("Report") },
                                onClick = {
                                    showOptionsMenu = false
                                    onReport()
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Flag, contentDescription = null)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = post.category.color.copy(alpha = if (isDarkTheme) 0.2f else 0.15f)
            ) {
                Text(
                    text = "${post.category.emoji} ${post.category.displayName}",
                    fontSize = 11.sp,
                    color = if (isDarkTheme) {
                        post.category.color.copy(alpha = 0.9f)
                    } else {
                        post.category.color
                    },
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content
            Text(
                text = post.content,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 22.sp
            )

            // Promoted badge
            if (post.isPromotedToArticle) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Featured in Health Tips",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reactions summary
            ReactionsSummary(
                reactions = post.reactions,
                onReactionClick = { showReactionPicker = !showReactionPicker }
            )

            // Reaction picker
            AnimatedVisibility(visible = showReactionPicker) {
                ReactionPicker(
                    reactions = post.reactions,
                    onReact = {
                        onReact(it)
                        showReactionPicker = false
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PostActionButton(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    label = if (post.commentCount > 0) "${post.commentCount}" else "Comment",
                    onClick = onComment
                )

                PostActionButton(
                    icon = Icons.Outlined.Share,
                    label = "Share",
                    onClick = onShare
                )
            }
        }
    }
}

@Composable
private fun UserAvatar(
    user: CommunityUser,
    size: androidx.compose.ui.unit.Dp
) {
    val phaseColor = user.currentPhase?.let {
        CommunityColors.getPhaseRoomColor(it)
    } ?: MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        phaseColor.copy(alpha = 0.7f),
                        phaseColor
                    )
                )
            )
            .then(
                if (user.isVerifiedProfessional) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = user.currentMood?.emoji ?: user.wisdomLevel.emoji,
            fontSize = (size.value * 0.45f).sp
        )
    }
}

private fun buildPostReportContext(post: CommunityPost): ReportContext {
    val isAnonymous = post.author.identityMode == IdentityMode.ANONYMOUS ||
            post.author.displayName.contains("Anonymous", ignoreCase = true)

    val authorName = if (isAnonymous) {
        "Anonymous Sister"
    } else {
        post.author.displayName
    }

    val title = "Post from $authorName"

    val metaParts = mutableListOf<String>()
    metaParts += post.category.displayName
    post.phaseTag?.let { phase ->
        metaParts += phase.displayName
    }
    // You already have formatTimeAgo() in CommunityScreen.kt
    metaParts += formatTimeAgo(post.createdAt)

    val meta = metaParts.joinToString(" • ")

    val rawSnippet = post.content.trim()
    val snippet = if (rawSnippet.length > 160) {
        rawSnippet.take(157) + "…"
    } else {
        rawSnippet
    }

    return ReportContext(
        title = title,
        snippet = snippet,
        meta = meta
    )
}

private fun buildPhaseMessageReportContext(
    room: PhaseRoom,
    message: PhaseRoomMessage
): ReportContext {
    val isAnonymous = message.isAnonymous

    val authorName = if (isAnonymous) {
        "Anonymous Sister"
    } else {
        message.author.displayName
    }

    val roomName = room.phase.displayName.split(" ").first()

    val title = "Message from $authorName"
    val meta = "Phase Room • $roomName"

    val rawSnippet = message.content.trim()
    val snippet = if (rawSnippet.length > 160) {
        rawSnippet.take(157) + "…"
    } else {
        rawSnippet
    }

    return ReportContext(
        title = title,
        snippet = snippet,
        meta = meta
    )
}

@Composable
private fun ReactionsSummary(
    reactions: List<PostReaction>,
    onReactionClick: () -> Unit
) {
    val userReaction = reactions.find { it.hasUserReacted }
    val topReactions = reactions
        .filter { it.count > 0 }
        .sortedByDescending { it.count }
        .take(3)
    val totalCount = reactions.sumOf { it.count }

    if (totalCount > 0 || userReaction != null) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (userReaction != null) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                Color.Transparent
            },
            modifier = Modifier.clip(RoundedCornerShape(20.dp))
        ) {
            Row(
                modifier = Modifier
                    .clickable { onReactionClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (userReaction != null) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = userReaction.type.emoji, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                topReactions
                    .filter { it.type != userReaction?.type }
                    .take(if (userReaction != null) 2 else 3)
                    .forEach { reaction ->
                        Text(text = reaction.type.emoji, fontSize = 18.sp)
                    }

                if (totalCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatCount(totalCount),
                        fontSize = 14.sp,
                        fontWeight = if (userReaction != null) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (userReaction != null) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    } else {
        TextButton(
            onClick = onReactionClick,
            modifier = Modifier.heightIn(min = 36.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.AddReaction,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "React",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ReactionPicker(
    reactions: List<PostReaction>,
    onReact: (ReactionType) -> Unit
) {
    val userReaction = reactions.find { it.hasUserReacted }?.type

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(ReactionType.values().toList()) { reactionType ->
            val reaction = reactions.find { it.type == reactionType }
            val isSelected = userReaction == reactionType
            val count = reaction?.count ?: 0

            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.1f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "reactionScale"
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    count > 0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                },
                border = if (isSelected) {
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                } else null,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clickable { onReact(reactionType) }
            ) {
                Row(
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = reactionType.emoji, fontSize = 20.sp)
                    if (count > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$count",
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PostActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ==========================================
// SHARE BOTTOM SHEET
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareBottomSheet(
    post: CommunityPost,
    onDismiss: () -> Unit,
    onShareToGroup: () -> Unit,
    onCopyLink: () -> Unit,
    onShareExternal: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Share Post",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(20.dp))

            ShareOption.values().forEach { option ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            when (option) {
                                ShareOption.SHARE_TO_GROUP -> onShareToGroup()
                                ShareOption.COPY_LINK -> onCopyLink()
                                ShareOption.SHARE_EXTERNAL -> onShareExternal()
                            }
                            onDismiss()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option.emoji,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = option.displayName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ==========================================
// PHASE ROOM TAB (Enhanced with anonymous toggle & report)
// ==========================================

@Composable
private fun PhaseRoomTabContent(
    viewModel: CommunityViewModel
) {
    val phaseRooms by viewModel.phaseRooms.collectAsState()
    val currentPhaseRoom by viewModel.currentPhaseRoom.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var messageText by remember { mutableStateOf("") }
    var postAnonymously by remember { mutableStateOf(true) }
    var reportMessageTarget by remember { mutableStateOf<PhaseRoomMessage?>(null) }

    val hapticFeedback = LocalHapticFeedback.current

    // New report sheet for phase room messages
    val roomForContext = currentPhaseRoom
    reportMessageTarget?.let { message ->
        if (roomForContext != null) {
            CommunityReportSheet(
                contentType = ContentType.PHASE_ROOM_MESSAGE,
                context = buildPhaseMessageReportContext(roomForContext, message),
                onDismissRequest = { reportMessageTarget = null },
                onSubmitReport = { reason, notes ->
                    viewModel.reportPhaseRoomMessage(message.id, reason, notes)
                    reportMessageTarget = null
                }
            )
        } else {
            // If for some reason there's no current room, just dismiss
            reportMessageTarget = null
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Phase room selector
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(phaseRooms) { room ->
                PhaseRoomChip(
                    room = room,
                    isSelected = currentPhaseRoom?.phase == room.phase,
                    onClick = { viewModel.joinPhaseRoom(room.phase) }
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        currentPhaseRoom?.let { room ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                PhaseRoomHeader(room = room)

                // Messages
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    reverseLayout = true,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = room.recentMessages.filter { !it.isHidden },
                        key = { it.id }
                    ) { message ->
                        PhaseRoomMessageItem(
                            message = message,
                            isCurrentUser = message.author.id == currentUser.id,
                            onLongPress = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                reportMessageTarget = message
                            }
                        )
                    }

                    if (room.recentMessages.isEmpty()) {
                        item {
                            EmptyStateMessage(
                                emoji = "🌙",
                                title = "Be the first to say hello!",
                                subtitle = "Connect with others in your phase"
                            )
                        }
                    }
                }

                // Anonymous toggle + message input
                Box(modifier = Modifier.padding(bottom = 100.dp)) {
                    PhaseRoomInputSection(
                        value = messageText,
                        onValueChange = { messageText = it },
                        postAnonymously = postAnonymously,
                        onAnonymousToggle = { postAnonymously = it },
                        onSend = {
                            if (messageText.isNotBlank()) {
                                viewModel.sendPhaseRoomMessage(messageText, postAnonymously)
                                messageText = ""
                            }
                        },
                        phaseColor = CommunityColors.getPhaseRoomColor(room.phase)
                    )
                }
            }
        }
    }
}
@Composable
private fun PhaseRoomChip(
    room: PhaseRoom,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = CommunityColors.getPhaseRoomColor(room.phase)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) color else color.copy(alpha = 0.15f),
        modifier = Modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when (room.phase) {
                    CyclePhase.MENSTRUAL -> "🩸"
                    CyclePhase.FOLLICULAR -> "🌱"
                    CyclePhase.OVULATION -> "🥚"
                    CyclePhase.LUTEAL -> "🌙"
                },
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = room.phase.displayName.split(" ").first(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color.White else color
            )
            Text(
                text = "${room.activeUsers} sisters here",
                fontSize = 10.sp,
                color = if (isSelected) Color.White.copy(alpha = 0.8f) else color.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun PhaseRoomHeader(room: PhaseRoom) {
    val color = CommunityColors.getPhaseRoomColor(room.phase)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (room.phase) {
                    CyclePhase.MENSTRUAL -> "🩸"
                    CyclePhase.FOLLICULAR -> "🌱"
                    CyclePhase.OVULATION -> "🥚"
                    CyclePhase.LUTEAL -> "🌙"
                },
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = room.phase.displayName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = room.phase.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Online indicator
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF4CAF50).copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${room.activeUsers}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

@Composable
private fun PhaseRoomMessageItem(
    message: PhaseRoomMessage,
    isCurrentUser: Boolean,
    onLongPress: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                // Only allow reporting other users' messages, not your own
                if (!isCurrentUser) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { onLongPress() }
                        )
                    }
                } else {
                    Modifier
                }
            ),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isCurrentUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (message.isAnonymous) "🌙" else message.author.currentMood?.emoji ?: "🌙",
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
        ) {
            if (!isCurrentUser) {
                Text(
                    text = if (message.isAnonymous) "Anonymous Sister" else message.author.displayName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isCurrentUser) 16.dp else 4.dp,
                    bottomEnd = if (isCurrentUser) 4.dp else 16.dp
                ),
                color = if (isCurrentUser) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    fontSize = 14.sp,
                    color = if (isCurrentUser) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Text(
                text = formatTimeAgo(message.timestamp),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun PhaseRoomInputSection(
    value: String,
    onValueChange: (String) -> Unit,
    postAnonymously: Boolean,
    onAnonymousToggle: (Boolean) -> Unit,
    onSend: () -> Unit,
    phaseColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Anonymous toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (postAnonymously) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                    )
                    .clickable { onAnonymousToggle(!postAnonymously) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (postAnonymously) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null,
                    tint = if (postAnonymously) phaseColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (postAnonymously) "Posting anonymously" else "Posting as you",
                    fontSize = 13.sp,
                    color = if (postAnonymously) phaseColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = postAnonymously,
                    onCheckedChange = onAnonymousToggle,
                    modifier = Modifier.height(24.dp),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = phaseColor,
                        checkedTrackColor = phaseColor.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Message input
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text("Share with your phase sisters...")
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = phaseColor,
                        cursorColor = phaseColor
                    ),
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSend() })
                )

                Spacer(modifier = Modifier.width(12.dp))

                FloatingActionButton(
                    onClick = onSend,
                    modifier = Modifier.size(48.dp),
                    containerColor = phaseColor,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// SEARCH TAB (New)
// ==========================================

@Composable
private fun SearchTabContent(
    viewModel: CommunityViewModel,
    navController: NavController
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(SearchFilter.ALL) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            placeholder = { Text("Search posts, groups, users...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )

        // Filter chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(SearchFilter.values().toList()) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter.displayName) },
                    leadingIcon = { Text(filter.emoji, fontSize = 14.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search results or empty state
        if (searchQuery.isEmpty()) {
            EmptyStateMessage(
                emoji = "🔍",
                title = "Search the community",
                subtitle = "Find posts, groups, and users"
            )
        } else {
            // TODO: Implement actual search results
            EmptyStateMessage(
                emoji = "🔎",
                title = "No results for \"$searchQuery\"",
                subtitle = "Try different keywords"
            )
        }
    }
}

// ==========================================
// GROUPS TAB (Replaces Explore)
// ==========================================

@Composable
private fun GroupsTabContent(
    viewModel: CommunityViewModel,
    navController: NavController
) {
    val groups by viewModel.groups.collectAsState()
    val localCircles by viewModel.localCircles.collectAsState()
    val verifiedProfessionals by viewModel.verifiedProfessionals.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showCreateGroupSheet by remember { mutableStateOf(false) }

    val canCreateGroup = currentUser.wisdomLevel.minPoints >= WisdomLevel.BLOOMING.minPoints

    if (showCreateGroupSheet) {
        CreateGroupBottomSheet(
            onDismiss = { showCreateGroupSheet = false },
            onCreate = { name, description, emoji, category, privacy, passcode ->
                viewModel.createGroup(name, description, emoji, category, privacy, passcode)
                showCreateGroupSheet = false
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Verified Professionals
            item {
                SectionHeader(
                    title = "Verified Professionals",
                    emoji = "🩺",
                    subtitle = "Expert advice you can trust"
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(verifiedProfessionals) { professional ->
                        ProfessionalCard(professional = professional)
                    }
                }
            }

            // Local Circles
            item {
                SectionHeader(
                    title = "Local Circles",
                    emoji = "📍",
                    subtitle = "Connect with nearby sisters"
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(localCircles) { circle ->
                        LocalCircleCard(group = circle)
                    }
                }
            }

            // Community Groups
            item {
                SectionHeader(
                    title = "Community Groups",
                    emoji = "💜",
                    subtitle = "Find your tribe"
                )
            }

            items(groups) { group ->
                GroupCard(
                    group = group,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Create Group FAB
        if (canCreateGroup) {
            ExtendedFloatingActionButton(
                onClick = { showCreateGroupSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 150.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Group")
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    emoji: String,
    subtitle: String
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfessionalCard(professional: CommunityUser) {
    Card(
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🩺", fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = professional.displayName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Verified",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }

            Text(
                text = professional.professionalTitle ?: "",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            if (professional.localCircle != null) {
                Text(
                    text = "📍 ${professional.localCircle}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LocalCircleCard(group: CommunityGroup) {
    Card(
        modifier = Modifier.width(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(group.coverColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = group.emoji, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = group.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${formatCount(group.memberCount)} members",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = group.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { /* TODO: Join group */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = group.coverColor
                )
            ) {
                Text("Join", fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun GroupCard(
    group: CommunityGroup,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(group.coverColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = group.emoji, fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = group.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (group.privacy != GroupPrivacy.PUBLIC) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = if (group.privacy == GroupPrivacy.PRIVATE_PASSCODE)
                                    Icons.Default.Key else Icons.Default.Lock,
                                contentDescription = group.privacy.displayName,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "${formatCount(group.memberCount)} members",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = { /* TODO: Join group */ },
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (group.isJoined) "Joined" else "Join",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Description
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = group.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ==========================================
// CREATE GROUP BOTTOM SHEET
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateGroupBottomSheet(
    onDismiss: () -> Unit,
    onCreate: (name: String, description: String, emoji: String, category: GroupCategory, privacy: GroupPrivacy, passcode: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("💜") }
    var selectedCategory by remember { mutableStateOf(GroupCategory.CUSTOM) }
    var selectedPrivacy by remember { mutableStateOf(GroupPrivacy.PUBLIC) }
    var passcode by remember { mutableStateOf("") }
    var showEmojiPicker by remember { mutableStateOf(false) }

    val commonEmojis = listOf(
        "💜", "💪", "🌸", "🌙", "✨", "🦋", "🌺", "💫",
        "🩺", "🧘", "🌱", "🌈", "💖", "🔥", "🌻", "🦄"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Create Group",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Emoji picker
            Text(
                text = "Group Icon",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(commonEmojis) { emoji ->
                    Surface(
                        shape = CircleShape,
                        color = if (selectedEmoji == emoji) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        },
                        border = if (selectedEmoji == emoji) {
                            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        } else null,
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { selectedEmoji = emoji }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = emoji, fontSize = 24.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Group name
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 30) name = it },
                label = { Text("Group Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                supportingText = { Text("${name.length}/30") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { if (it.length <= 150) description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                shape = RoundedCornerShape(12.dp),
                supportingText = { Text("${description.length}/150") }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Category
            Text(
                text = "Category",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(GroupCategory.values().filter { it != GroupCategory.PHASE_ROOM }) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category.displayName) },
                        leadingIcon = { Text(category.emoji, fontSize = 14.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Privacy
            Text(
                text = "Privacy",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            GroupPrivacy.values().forEach { privacy ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedPrivacy == privacy) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    } else {
                        Color.Transparent
                    },
                    border = if (selectedPrivacy == privacy) {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    } else {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedPrivacy = privacy }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = privacy.emoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = privacy.displayName,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = privacy.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        RadioButton(
                            selected = selectedPrivacy == privacy,
                            onClick = { selectedPrivacy = privacy }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Passcode (if private with passcode)
            AnimatedVisibility(visible = selectedPrivacy == GroupPrivacy.PRIVATE_PASSCODE) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = passcode,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) passcode = it },
                        label = { Text("6-Digit Passcode") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Create button
            Button(
                onClick = {
                    onCreate(
                        name,
                        description,
                        selectedEmoji,
                        selectedCategory,
                        selectedPrivacy,
                        if (selectedPrivacy == GroupPrivacy.PRIVATE_PASSCODE) passcode else null
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = name.isNotBlank() && description.isNotBlank() &&
                        (selectedPrivacy != GroupPrivacy.PRIVATE_PASSCODE || passcode.length == 6),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Create Group",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==========================================
// CHALLENGES TAB (With freeze token info)
// ==========================================

@Composable
private fun ChallengesTabContent(
    viewModel: CommunityViewModel,
    onFreezeTokenInfoClick: () -> Unit
) {
    val challenges by viewModel.activeChallenges.collectAsState()
    val joinedChallenges by viewModel.joinedChallenges.collectAsState()
    val userStreaks by viewModel.userStreaks.collectAsState()
    val userPoints by viewModel.userPoints.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            EnhancedUserStatsCard(
                user = currentUser,
                points = userPoints,
                streaks = userStreaks,
                onFreezeTokenInfoClick = onFreezeTokenInfoClick
            )
        }

        item {
            SectionHeader(
                title = "Active Challenges",
                emoji = "🏆",
                subtitle = "Join and earn points!"
            )
        }

        items(
            items = challenges,
            key = { it.id }
        ) { challenge ->
            ChallengeCard(
                challenge = challenge,
                isJoined = challenge.id in joinedChallenges,
                onJoin = { viewModel.joinChallenge(challenge.id) },
                onLeave = { viewModel.leaveChallenge(challenge.id) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun EnhancedUserStatsCard(
    user: CommunityUser,
    points: Int,
    streaks: List<UserStreak>,
    onFreezeTokenInfoClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Points & Level
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = user.wisdomLevel.emoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = user.wisdomLevel.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = user.wisdomLevel.color
                        )
                    }
                    Text(
                        text = "$points points",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Progress to next level
                val nextLevel = WisdomLevel.values()
                    .firstOrNull { it.minPoints > points }

                if (nextLevel != null) {
                    val currentLevel = user.wisdomLevel
                    val progress = (points - currentLevel.minPoints).toFloat() /
                            (nextLevel.minPoints - currentLevel.minPoints)

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Next: ${nextLevel.title}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .width(100.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = nextLevel.color,
                            trackColor = nextLevel.color.copy(alpha = 0.2f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Freeze token info button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CommunityColors.FreezeToken.copy(alpha = 0.1f))
                    .clickable { onFreezeTokenInfoClick() }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🛡️", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Streak Freeze Tokens",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = CommunityColors.FreezeToken
                    )
                    val totalFreezeTokens = streaks.sumOf { it.freezeTokensRemaining }
                    Text(
                        text = "$totalFreezeTokens token${if (totalFreezeTokens != 1) "s" else ""} available",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = "Info",
                    tint = CommunityColors.FreezeToken,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Streaks
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                streaks.forEach { streak ->
                    EnhancedStreakItem(streak = streak)
                }
            }
        }
    }
}

@Composable
private fun EnhancedStreakItem(streak: UserStreak) {
    val state = streak.state

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon with state indicator
        Box(contentAlignment = Alignment.Center) {
            // Background circle showing progress to next milestone
            if (streak.nextMilestone != null && streak.currentCount > 0) {
                CircularProgressIndicator(
                    progress = { streak.progressToNextMilestone },
                    modifier = Modifier.size(56.dp),
                    strokeWidth = 3.dp,
                    color = state.color.copy(alpha = 0.3f),
                    trackColor = Color.Transparent
                )
            }

            // Main icon
            Text(
                text = state.icon,
                fontSize = 24.sp
            )

            // Freeze token indicator (blue shield theme)
            if (streak.freezeTokensRemaining > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(CommunityColors.FreezeToken),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = streak.freezeTokensRemaining.toString(),
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Count with state color
        Text(
            text = if (streak.currentCount > 0) "${streak.currentCount}" else "—",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = state.color
        )

        // Label
        Text(
            text = streak.type.label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // Status message (if at risk or broken)
        if (streak.isAtRisk || streak.isBroken) {
            Text(
                text = state.message,
                fontSize = 9.sp,
                color = state.color,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ChallengeCard(
    challenge: Challenge,
    isJoined: Boolean,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isJoined) {
                challenge.type.color.copy(alpha = if (isDarkTheme) 0.15f else 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(challenge.type.color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = challenge.emoji, fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = challenge.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = challenge.type.color.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = challenge.type.displayName,
                                fontSize = 11.sp,
                                color = challenge.type.color,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Points badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFD700).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "+${challenge.pointsReward} pts",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFB300),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = challenge.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Progress (if joined)
            if (isJoined) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Progress",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(challenge.currentProgress * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = challenge.type.color
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { challenge.currentProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = challenge.type.color,
                        trackColor = challenge.type.color.copy(alpha = 0.2f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.People,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${formatCount(challenge.participantCount)} joined",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${challenge.durationDays} days",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isJoined) {
                    OutlinedButton(
                        onClick = onLeave,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Leave",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Button(
                        onClick = onJoin,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = challenge.type.color
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Join",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// CREATE POST BOTTOM SHEET
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatePostBottomSheet(
    currentUser: CommunityUser,
    onDismiss: () -> Unit,
    onPost: (content: String, category: PostCategory, ageRestriction: AgeRestriction, phaseTag: CyclePhase?, isAnonymous: Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var content by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(PostCategory.DISCUSSION) }
    var selectedAgeRestriction by remember { mutableStateOf(AgeRestriction.ALL_AGES) }
    // Auto-select user's current phase
    var selectedPhase by remember { mutableStateOf(currentUser.currentPhase) }
    // Anonymous toggle - default to false (post as yourself)
    var postAnonymously by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showAgePicker by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showProfessionalInfoDialog by remember { mutableStateOf(false) }

    val maxCharacters = 1000
    val hasContent = content.isNotBlank()

    // Discard confirmation dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            icon = {
                Text("📝", fontSize = 32.sp)
            },
            title = {
                Text(
                    text = "Discard post?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("You have unsaved content. Are you sure you want to discard it?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep editing")
                }
            }
        )
    }

    if (showProfessionalInfoDialog) {
        ProfessionalCategoryInfoDialog(
            onDismiss = { showProfessionalInfoDialog = false }
        )
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (hasContent) {
                showDiscardDialog = true
            } else {
                onDismiss()
            }
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Create Post ✨",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                TextButton(
                    onClick = {
                        if (content.isNotBlank()) {
                            onPost(content, selectedCategory, selectedAgeRestriction, selectedPhase, postAnonymously)
                        }
                    },
                    enabled = content.isNotBlank()
                ) {
                    Text(
                        text = "Post",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ============================================
            // ANONYMOUS TOGGLE (New!)
            // ============================================
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (postAnonymously) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                border = if (postAnonymously) {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { postAnonymously = !postAnonymously }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar preview
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .then(
                                if (postAnonymously) {
                                    Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                                } else {
                                    val phaseColor = currentUser.currentPhase?.let {
                                        CommunityColors.getPhaseRoomColor(it)
                                    } ?: MaterialTheme.colorScheme.primary

                                    Modifier.background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                phaseColor.copy(alpha = 0.7f),
                                                phaseColor
                                            )
                                        )
                                    )
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (postAnonymously) "🌙" else (currentUser.currentMood?.emoji ?: currentUser.wisdomLevel.emoji),
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (postAnonymously) "Anonymous Sister" else currentUser.displayName,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (postAnonymously) {
                                "Your identity will be hidden"
                            } else {
                                "Posting as yourself"
                            },
                            fontSize = 12.sp,
                            color = if (postAnonymously) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    Icon(
                        imageVector = if (postAnonymously) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = if (postAnonymously) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Switch(
                        checked = postAnonymously,
                        onCheckedChange = { postAnonymously = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ============================================
            // CONTENT INPUT
            // ============================================
            OutlinedTextField(
                value = content,
                onValueChange = {
                    if (it.length <= maxCharacters) content = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                placeholder = {
                    Text(
                        text = if (postAnonymously) {
                            "Share anonymously... your identity is safe 🤍"
                        } else {
                            "Share your thoughts, questions, or experiences..."
                        }
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp),
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Community guidelines hint
                        Text(
                            text = "💜 Be kind & supportive",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        // Character count
                        Text(
                            text = "${content.length}/$maxCharacters",
                            fontSize = 11.sp,
                            color = if (content.length > maxCharacters * 0.9f) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            }
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ============================================
            // OPTIONS ROW
            // ============================================
            Text(
                text = "Post options",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category selector
                item {
                    FilterChip(
                        selected = true,
                        onClick = { showCategoryPicker = true },
                        label = { Text("${selectedCategory.emoji} ${selectedCategory.displayName}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = selectedCategory.color.copy(alpha = 0.2f),
                            selectedLabelColor = selectedCategory.color
                        )
                    )
                }

                // Age restriction
                item {
                    FilterChip(
                        selected = selectedAgeRestriction != AgeRestriction.ALL_AGES,
                        onClick = { showAgePicker = true },
                        label = {
                            Text(
                                if (selectedAgeRestriction == AgeRestriction.ALL_AGES) {
                                    "🔓 All Ages"
                                } else {
                                    "🔒 ${selectedAgeRestriction.label}"
                                }
                            )
                        }
                    )
                }

                // Phase tag (auto-selected, can remove)
                item {
                    FilterChip(
                        selected = selectedPhase != null,
                        onClick = {
                            // Toggle: if has phase, remove it; if null, add current phase
                            selectedPhase = if (selectedPhase != null) null else currentUser.currentPhase
                        },
                        label = {
                            if (selectedPhase != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = when (selectedPhase) {
                                            CyclePhase.MENSTRUAL -> "🩸"
                                            CyclePhase.FOLLICULAR -> "🌱"
                                            CyclePhase.OVULATION -> "🥚"
                                            CyclePhase.LUTEAL -> "🌙"
                                            null -> ""
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(selectedPhase!!.displayName)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove phase tag",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else {
                                Text("+ Add phase")
                            }
                        },
                        colors = if (selectedPhase != null) {
                            FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CommunityColors.getPhaseRoomColor(selectedPhase!!).copy(alpha = 0.2f),
                                selectedLabelColor = CommunityColors.getPhaseRoomColor(selectedPhase!!)
                            )
                        } else {
                            FilterChipDefaults.filterChipColors()
                        }
                    )
                }
            }

// Category picker dropdown
            DropdownMenu(
                expanded = showCategoryPicker,
                onDismissRequest = { showCategoryPicker = false }
            ) {
                PostCategory.values()
                    .filter { category ->
                        //Only show PROFESSIONAL if user is verified
                        if (category == PostCategory.PROFESSIONAL) {
                            currentUser.isVerifiedProfessional
                        } else {
                            true
                        }
                    }
                    .forEach { category ->
                        DropdownMenuItem(
                            text = { Text("${category.emoji} ${category.displayName}") },
                            onClick = {
                                selectedCategory = category
                                showCategoryPicker = false
                            }
                        )
                    }

                // Show locked option for non-professionals
                if (!currentUser.isVerifiedProfessional) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "🩺 Professional Advice",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Verified professionals only",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        },
                        onClick = {
                            showCategoryPicker = false
                            showProfessionalInfoDialog = true
                        },
                        enabled = false
                    )
                }
            }
            // Age picker dropdown
            DropdownMenu(
                expanded = showAgePicker,
                onDismissRequest = { showAgePicker = false }
            ) {
                AgeRestriction.values().forEach { age ->
                    DropdownMenuItem(
                        text = { Text(age.label) },
                        onClick = {
                            selectedAgeRestriction = age
                            showAgePicker = false
                        }
                    )
                }
            }
        }
    }
}
// ==========================================
// REPORT DIALOG
// ==========================================
//
//@Composable
//private fun ReportDialog(
//    contentType: ContentType,
//    onDismiss: () -> Unit,
//    onReport: (reason: ReportReason, notes: String?) -> Unit
//) {
//    var selectedReason by remember { mutableStateOf<ReportReason?>(null) }
//    var additionalNotes by remember { mutableStateOf("") }
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = {
//            Text(
//                text = "Report ${contentType.name.lowercase().replace("_", " ")}",
//                fontWeight = FontWeight.Bold
//            )
//        },
//        text = {
//            Column(
//                modifier = Modifier.verticalScroll(rememberScrollState())
//            ) {
//                Text(
//                    text = "Why are you reporting this?",
//                    fontSize = 14.sp,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                ReportReason.values().forEach { reason ->
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clip(RoundedCornerShape(8.dp))
//                            .clickable { selectedReason = reason }
//                            .padding(12.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        RadioButton(
//                            selected = selectedReason == reason,
//                            onClick = { selectedReason = reason }
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            text = reason.displayName,
//                            fontSize = 14.sp
//                        )
//                    }
//                }
//
//                if (selectedReason == ReportReason.OTHER) {
//                    Spacer(modifier = Modifier.height(12.dp))
//                    OutlinedTextField(
//                        value = additionalNotes,
//                        onValueChange = { additionalNotes = it },
//                        placeholder = { Text("Please describe...") },
//                        modifier = Modifier.fillMaxWidth(),
//                        maxLines = 3,
//                        shape = RoundedCornerShape(12.dp)
//                    )
//                }
//            }
//        },
//        confirmButton = {
//            Button(
//                onClick = {
//                    selectedReason?.let { onReport(it, additionalNotes.ifBlank { null }) }
//                },
//                enabled = selectedReason != null
//            ) {
//                Text("Submit Report")
//            }
//        },
//        dismissButton = {
//            TextButton(onClick = onDismiss) {
//                Text("Cancel")
//            }
//        }
//    )
//}

// ==========================================
// SKELETON LOADING
// ==========================================

@Composable
private fun CommunitySkeletonLoading() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerGradient = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant
        ),
        startX = shimmerTranslate - 500f,
        endX = shimmerTranslate
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header skeleton
        Box(
            modifier = Modifier
                .height(32.dp)
                .width(180.dp)
                .background(shimmerGradient, RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tab skeleton
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(5) {
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(70.dp)
                        .background(shimmerGradient, RoundedCornerShape(18.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Post skeletons
        repeat(3) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(shimmerGradient, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Box(
                                modifier = Modifier
                                    .height(16.dp)
                                    .width(120.dp)
                                    .background(shimmerGradient, RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .height(12.dp)
                                    .width(80.dp)
                                    .background(shimmerGradient, RoundedCornerShape(4.dp))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .height(80.dp)
                            .fillMaxWidth()
                            .background(shimmerGradient, RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .width(80.dp)
                                .background(shimmerGradient, RoundedCornerShape(16.dp))
                        )
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .width(80.dp)
                                .background(shimmerGradient, RoundedCornerShape(16.dp))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ==========================================
// EMPTY STATE MESSAGE
// ==========================================

@Composable
private fun EmptyStateMessage(
    emoji: String,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==========================================
// HELPER FUNCTIONS
// ==========================================

private fun formatTimeAgo(dateTime: LocalDateTime): String {
    val now = LocalDateTime.now()
    val minutes = ChronoUnit.MINUTES.between(dateTime, now)
    val hours = ChronoUnit.HOURS.between(dateTime, now)
    val days = ChronoUnit.DAYS.between(dateTime, now)

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> dateTime.format(DateTimeFormatter.ofPattern("MMM d"))
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> "${count / 1_000_000}M"
        count >= 1_000 -> "${count / 1_000}K"
        else -> count.toString()
    }
}

// ==========================================
// PREVIEWS
// ==========================================

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun CommunityScreenPreview() {
    MoonSyncTheme {
        CommunityScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, name = "Dark Mode", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CommunityScreenDarkPreview() {
    MoonSyncTheme(darkTheme = true) {
        CommunityScreen(navController = rememberNavController())
    }
}