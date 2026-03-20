package com.example.moonsyncapp.ui.screens.healthtips

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.moonsyncapp.data.model.CyclePhase
import com.example.moonsyncapp.data.model.HealthTip
import com.example.moonsyncapp.data.model.PhaseColors
import com.example.moonsyncapp.data.model.TipCategory
import com.example.moonsyncapp.ui.theme.MoonSyncTheme
import kotlinx.coroutines.delay
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.moonsyncapp.navigation.Routes
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.statusBarsPadding

// Health Tips specific colors
object HealthTipsColors {
    // Text colors for pastel backgrounds (same approach as hexagon)
    val TextOnPastel = Color(0xFF2D2D2D)
    val SecondaryTextOnPastel = Color(0xFF666666)

    // Category colors adjusted for dark mode
    @Composable
    fun getCategoryBackgroundColor(category: TipCategory): Color {
        val isDarkTheme = isSystemInDarkTheme()
        return if (isDarkTheme) {
            category.color.copy(alpha = 0.2f)
        } else {
            category.color.copy(alpha = 0.15f)
        }
    }

    @Composable
    fun getCategoryTextColor(category: TipCategory): Color {
        val isDarkTheme = isSystemInDarkTheme()
        return if (isDarkTheme) {
            // Brighten the color slightly in dark mode for better visibility
            category.color.copy(alpha = 0.9f)
        } else {
            category.color
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthTipsScreen(
    navController: NavController,
    viewModel: HealthTipsViewModel = viewModel()
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val currentPhase by viewModel.currentPhase.collectAsState()
    val tips = remember(selectedCategory) {
        viewModel.getFilteredTips()
    }

    // States
    var isNavigating by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()

    // Animation states
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showContent = true
    }

    // Refresh logic
    suspend fun refresh() {
        isRefreshing = true
        delay(1000) // Simulate network call
        // viewModel.refreshTips() // Add this to your ViewModel
        isRefreshing = false
    }

    Scaffold(
        modifier = Modifier.statusBarsPadding(), // FIXED: Changed from systemBarsPadding
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Health Tips",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (!isNavigating) {
                                isNavigating = true
                                navController.popBackStack()
                            }
                        },
                        enabled = !isNavigating
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = if (isNavigating) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                // Launch coroutine for refresh
                kotlinx.coroutines.GlobalScope.launch {
                    refresh()
                }
            },
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Category Filter Chips
                item {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(300)) + slideInVertically(
                            animationSpec = tween(300),
                            initialOffsetY = { -it }
                        )
                    ) {
                        CategoryChips(
                            selectedCategory = selectedCategory,
                            onCategorySelected = { viewModel.selectCategory(it) }
                        )
                    }
                }

                // Phase-specific header
                item {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(400, delayMillis = 100))
                    ) {
                        PhaseRecommendationHeader(
                            currentPhase = currentPhase,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }

                // Tips list with optimized staggered animation
                itemsIndexed(
                    items = tips,
                    key = { _, tip -> tip.id }
                ) { index, tip ->
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = 300,
                                delayMillis = minOf(200 + (index * 50), 500) // Cap delay to prevent too long waits
                            )
                        ) + slideInVertically(
                            animationSpec = tween(
                                durationMillis = 300,
                                delayMillis = minOf(200 + (index * 50), 500)
                            ),
                            initialOffsetY = { it / 4 }
                        )
                    ) {
                        TipCard(
                            tip = tip,
                            isRecommended = tip.relevantPhases.contains(currentPhase),
                            onClick = {
                                navController.navigate(Routes.articleDetail(tip.id))
                            },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoryChips(
    selectedCategory: TipCategory?,
    onCategorySelected: (TipCategory?) -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        // "All" chip
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("All") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }

        // Category chips
        items(TipCategory.values().toList()) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = {
                    onCategorySelected(
                        if (selectedCategory == category) null else category
                    )
                },
                label = {
                    Text(
                        text = category.displayName,
                        fontWeight = if (selectedCategory == category) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = category.color.copy(
                        alpha = if (isDarkTheme) 0.3f else 0.8f
                    ),
                    selectedLabelColor = if (isDarkTheme) Color.White else Color.White,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun PhaseRecommendationHeader(
    currentPhase: CyclePhase,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PhaseColors.getFillColor(currentPhase)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = PhaseColors.getBorderColor(currentPhase).copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "✨",
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Recommended for ${currentPhase.displayName}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = PhaseColors.getBorderColor(currentPhase) // Keep vibrant color
                )
                Text(
                    text = "Tips specially curated for your current phase",
                    fontSize = 12.sp,
                    color = HealthTipsColors.SecondaryTextOnPastel // FIXED: Dark text on pastel
                )
            }
        }
    }
}

@Composable
private fun TipCard(
    tip: HealthTip,
    isRecommended: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()

    // Press animation
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tipCardScale"
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
                    onTap = { onClick() }
                )
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRecommended) {
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
            defaultElevation = if (isRecommended) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Emoji
                Text(
                    text = tip.emoji,
                    fontSize = 32.sp
                )

                // Category badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HealthTipsColors.getCategoryBackgroundColor(tip.category),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = tip.category.displayName,
                        fontSize = 11.sp,
                        color = HealthTipsColors.getCategoryTextColor(tip.category),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = tip.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description
            Text(
                text = tip.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Read time
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏱️",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${tip.readTime} min read",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Recommended badge
                if (isRecommended) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDarkTheme) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        }
                    ) {
                        Text(
                            text = "Recommended",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HealthTipsScreenPreview() {
    MoonSyncTheme {
        HealthTipsScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HealthTipsScreenDarkPreview() {
    MoonSyncTheme(darkTheme = true) {
        HealthTipsScreen(navController = rememberNavController())
    }
}