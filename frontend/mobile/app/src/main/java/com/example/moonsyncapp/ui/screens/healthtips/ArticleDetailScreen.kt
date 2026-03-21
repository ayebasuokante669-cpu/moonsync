package com.example.moonsyncapp.ui.screens.healthtips

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.moonsyncapp.navigation.Routes
import com.example.moonsyncapp.ui.theme.MoonSyncTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Snackbar
import kotlinx.coroutines.launch

// ==========================================
// ARTICLE COLORS
// ==========================================

object ArticleColors {
    val ReadingTextPrimary = Color(0xFF1A1A1A)
    val ReadingTextSecondary = Color(0xFF4A4A4A)
    val QuoteBackground = Color(0xFFF5F5F5)
    val QuoteBorder = Color(0xFF9C7AC1)
    val HighlightYellow = Color(0xFFFFF9C4)

    // Dark mode variants
    val ReadingTextPrimaryDark = Color(0xFFE5E5E5)
    val ReadingTextSecondaryDark = Color(0xFFB0B0B0)
    val QuoteBackgroundDark = Color(0xFF2A2A2A)

    @Composable
    fun getReadingTextPrimary(): Color {
        return if (isSystemInDarkTheme()) ReadingTextPrimaryDark else ReadingTextPrimary
    }

    @Composable
    fun getReadingTextSecondary(): Color {
        return if (isSystemInDarkTheme()) ReadingTextSecondaryDark else ReadingTextSecondary
    }

    @Composable
    fun getQuoteBackground(): Color {
        return if (isSystemInDarkTheme()) QuoteBackgroundDark else QuoteBackground
    }
}

// ==========================================
// MAIN SCREEN
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    articleId: String,
    navController: NavController,
    viewModel: ArticleViewModel = viewModel()
) {
    // Load article by ID
    LaunchedEffect(articleId) {
        viewModel.loadArticle(articleId)
    }

    val article by viewModel.currentArticle.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()
    val relatedArticles by viewModel.relatedArticles.collectAsState()

    val listState = rememberLazyListState()
    var isNavigating by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Calculate reading progress
    val scrollProgress = remember {
        derivedStateOf {
            if (listState.layoutInfo.totalItemsCount == 0) {
                0f
            } else {
                val firstVisibleItem = listState.firstVisibleItemIndex
                val totalItems = listState.layoutInfo.totalItemsCount
                (firstVisibleItem.toFloat() / totalItems.coerceAtLeast(1)).coerceIn(0f, 1f)
            }
        }
    }

    // Share bottom sheet
//    if (showShareSheet && article != null) {
//        ShareBottomSheet(
//            article = article!!,
//            onDismiss = { showShareSheet = false }
//        )
//    }
    if (showShareSheet && article != null) {
        ShareBottomSheet(
            article = article!!,
            onDismiss = { showShareSheet = false },
            onShowComingSoon = { message ->
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        if (isLoading || article == null) {
            ArticleSkeletonLoading()
        } else {
            article?.let { tip ->
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // Hero section
                    item {
                        ArticleHero(
                            article = tip,
                            scrollProgress = scrollProgress.value
                        )
                    }

                    // Article metadata
                    item {
                        ArticleMetadata(
                            article = tip,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Article content
                    item {
                        ArticleContent(
                            content = tip.content,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // Category tags
                    item {
                        CategoryTags(
                            category = tip.category,
                            phases = tip.relevantPhases,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                    }

                    // Related articles
                    if (relatedArticles.isNotEmpty()) {
                        item {
                            Text(
                                text = "Related Articles",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp)
                            ) {
                                items(relatedArticles) { relatedTip ->
                                    RelatedArticleCard(
                                        article = relatedTip,
                                        onClick = {
                                            // Navigate to this article
                                            navController.navigate(Routes.articleDetail(relatedTip.id))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Top bar (fades in on scroll)
        AnimatedVisibility(
            visible = scrollProgress.value > 0.1f,
            enter = fadeIn() + slideInVertically(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            ArticleTopBar(
                title = article?.title ?: "",
                onBackClick = {
                    if (!isNavigating) {
                        isNavigating = true
                        navController.popBackStack()
                    }
                },
                onShareClick = { showShareSheet = true },
                isSaved = isSaved,
                onSaveClick = { viewModel.toggleSave() }
            )
        }

        // Floating action buttons (always visible)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Save button
            FloatingActionButton(
                onClick = { viewModel.toggleSave() },
                containerColor = if (isSaved) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                contentColor = if (isSaved) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = if (isSaved) "Remove bookmark" else "Bookmark"
                )
            }

            // Share button
            FloatingActionButton(
                onClick = { showShareSheet = true },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = "Share"
                )
            }

            // Back button (when top bar is hidden)
            AnimatedVisibility(
                visible = scrollProgress.value <= 0.1f,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        if (!isNavigating) {
                            isNavigating = true
                            navController.popBackStack()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        }

        // Reading progress bar
        LinearProgressIndicator(
            progress = { scrollProgress.value },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .align(Alignment.TopCenter),
            color = MaterialTheme.colorScheme.primary,
            trackColor = Color.Transparent
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        ) { snackbarData ->
            Snackbar(
                snackbarData = snackbarData,
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface
            )
        }
    }
}

// ==========================================
// HERO SECTION
// ==========================================

@Composable
private fun ArticleHero(
    article: HealthTip,
    scrollProgress: Float
) {
    val isDarkTheme = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .graphicsLayer {
                // Parallax effect
                translationY = scrollProgress * 200f
                alpha = 1f - (scrollProgress * 1.5f).coerceIn(0f, 1f)
            }
    ) {
        // Gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            article.category.color.copy(alpha = if (isDarkTheme) 0.4f else 0.7f),
                            article.category.color.copy(alpha = if (isDarkTheme) 0.2f else 0.4f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        // Emoji hero
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            // Floating emoji with animation
            val infiniteTransition = rememberInfiniteTransition(label = "emoji_float")
            val floatOffset by infiniteTransition.animateFloat(
                initialValue = -10f,
                targetValue = 10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "floatOffset"
            )

            Text(
                text = article.emoji,
                fontSize = 120.sp,
                modifier = Modifier.graphicsLayer {
                    translationY = floatOffset
                }
            )
        }

        // Category badge
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp),
            shape = RoundedCornerShape(20.dp),
            color = article.category.color.copy(alpha = 0.9f)
        ) {
            Text(
                text = article.category.displayName,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

// ==========================================
// TOP BAR
// ==========================================

@Composable
private fun ArticleTopBar(
    title: String,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    isSaved: Boolean,
    onSaveClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Row {
                IconButton(onClick = onSaveClick) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share"
                    )
                }
            }
        }
    }
}

// ==========================================
// ARTICLE METADATA
// ==========================================

@Composable
private fun ArticleMetadata(
    article: HealthTip,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Title
        Text(
            text = article.title,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 40.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Read time and date
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Read time
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${article.readTime} min read",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Divider dot
            Text(
                text = "•",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Date (mock - using current date)
            Text(
                text = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Description
        Text(
            text = article.description,
            fontSize = 16.sp,
            fontStyle = FontStyle.Italic,
//            color = ArticleColors.getReadingTextSecondary(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    }
}

// ==========================================
// ARTICLE CONTENT
// ==========================================

@Composable
private fun ArticleContent(
    content: String,
    modifier: Modifier = Modifier
) {
    // Parse content into paragraphs (simple implementation)
    // In real app, you'd use rich text parsing
    val paragraphs = content.split("\n\n")

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        paragraphs.forEach { paragraph ->
            when {
                paragraph.startsWith("##") -> {
                    // Subheading
                    Text(
                        text = paragraph.removePrefix("##").trim(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 32.sp
                    )
                }
                paragraph.startsWith(">") -> {
                    // Quote
                    QuoteBlock(text = paragraph.removePrefix(">").trim())
                }
                paragraph.startsWith("•") || paragraph.startsWith("-") -> {
                    // Bullet point
                    BulletPoint(text = paragraph.substring(1).trim())
                }
                paragraph.startsWith("1.") || paragraph.matches(Regex("^\\d+\\..*")) -> {
                    // Numbered list
                    NumberedPoint(text = paragraph)
                }
                else -> {
                    // Regular paragraph
                    Text(
                        text = paragraph,
                        fontSize = 17.sp,
//                        color = ArticleColors.getReadingTextPrimary(),
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 28.sp,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun QuoteBlock(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = ArticleColors.getQuoteBackground()
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            // Quote bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(60.dp)
                    .background(ArticleColors.QuoteBorder, RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = text,
                fontSize = 16.sp,
                fontStyle = FontStyle.Italic,
//                color = ArticleColors.getReadingTextPrimary(),
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 26.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "•",
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            text = text,
            fontSize = 17.sp,
//            color = ArticleColors.getReadingTextPrimary(),
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 28.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun NumberedPoint(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        val parts = text.split(".", limit = 2)
        if (parts.size == 2) {
            Text(
                text = "${parts[0]}.",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp)
            )
            Text(
                text = parts[1].trim(),
                fontSize = 17.sp,
//                color = ArticleColors.getReadingTextPrimary(),
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 28.sp,
                modifier = Modifier.weight(1f)
            )
        } else {
            Text(
                text = text,
                fontSize = 17.sp,
//                color = ArticleColors.getReadingTextPrimary(),
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 28.sp
            )
        }
    }
}

// ==========================================
// CATEGORY TAGS
// ==========================================

@Composable
private fun CategoryTags(
    category: TipCategory,
    phases: List<CyclePhase>,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Relevant for:",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category tag
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = category.color.copy(alpha = if (isDarkTheme) 0.2f else 0.15f)
                ) {
                    Text(
                        text = "${category.emoji} ${category.displayName}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDarkTheme) {
                            category.color.copy(alpha = 0.9f)
                        } else {
                            category.color
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Phase tags
            items(phases) { phase ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PhaseColors.getFillColor(phase)
                ) {
                    Text(
                        text = when (phase) {
                            CyclePhase.MENSTRUAL -> "🩸 ${phase.displayName}"
                            CyclePhase.FOLLICULAR -> "🌱 ${phase.displayName}"
                            CyclePhase.OVULATION -> "🥚 ${phase.displayName}"
                            CyclePhase.LUTEAL -> "🌙 ${phase.displayName}"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = PhaseColors.getBorderColor(phase),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// RELATED ARTICLES
// ==========================================

@Composable
private fun RelatedArticleCard(
    article: HealthTip,
    onClick: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()

    // Press animation
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "relatedCardScale"
    )

    Card(
        modifier = Modifier
            .width(280.dp)
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Header with emoji and category
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                article.category.color.copy(alpha = if (isDarkTheme) 0.3f else 0.5f),
                                article.category.color.copy(alpha = if (isDarkTheme) 0.1f else 0.2f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = article.emoji,
                    fontSize = 56.sp
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = article.category.color.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = article.category.displayName,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = article.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = article.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Read time
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${article.readTime} min read",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ==========================================
// SHARE BOTTOM SHEET
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareBottomSheet(
    article: HealthTip,
    onDismiss: () -> Unit,
    onShowComingSoon: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Share Article",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Share options
//            ShareOption(
//                icon = Icons.Outlined.Link,
//                label = "Copy Link",
//                onClick = {
//                    // Copy link to clipboard
//                    onDismiss()
//                }
//            )
//
//            ShareOption(
//                icon = Icons.Outlined.Message,
//                label = "Share to Community",
//                onClick = {
//                    // Share to community feed
//                    onDismiss()
//                }
//            )
//
//            ShareOption(
//                icon = Icons.Outlined.MoreHoriz,
//                label = "More Options",
//                onClick = {
//                    // System share sheet
//                    onDismiss()
//                }
//            )
            ShareOption(
                icon = Icons.Outlined.Link,
                label = "Copy Link",
                comingSoon = true,  // ADD
                onClick = {
                    onDismiss()
                    onShowComingSoon("Link copying — Coming Soon ✨")
                }
            )

            ShareOption(
                icon = Icons.Outlined.Message,
                label = "Share to Community",
                comingSoon = true,  // ADD
                onClick = {
                    onDismiss()
                    onShowComingSoon("Community sharing — Coming Soon ✨")
                }
            )

            ShareOption(
                icon = Icons.Outlined.MoreHoriz,
                label = "More Options",
                comingSoon = true,  // ADD
                onClick = {
                    onDismiss()
                    onShowComingSoon("More sharing options — Coming Soon ✨")
                }
            )
        }
    }
}

@Composable
private fun ShareOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    comingSoon: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
//        Row(
//            modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Icon(
//                imageVector = icon,
//                contentDescription = null,
//                tint = MaterialTheme.colorScheme.primary,
//                modifier = Modifier.size(24.dp)
//            )
//
//            Spacer(modifier = Modifier.width(16.dp))
//
//            Text(
//                text = label,
//                fontSize = 16.sp,
//                color = MaterialTheme.colorScheme.onBackground
//            )
//        }
        Row(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (comingSoon) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = label,
                fontSize = 16.sp,
                color = if (comingSoon) {
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.onBackground
                },
                modifier = Modifier.weight(1f)
            )

            // Coming Soon badge
            if (comingSoon) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "Soon",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// SKELETON LOADING
// ==========================================

@Composable
private fun ArticleSkeletonLoading() {
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
            .padding(20.dp)
    ) {
        // Hero skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(shimmerGradient, RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Title skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(shimmerGradient, RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(40.dp)
                .background(shimmerGradient, RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Metadata skeleton
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(20.dp)
                    .background(shimmerGradient, RoundedCornerShape(4.dp))
            )
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(20.dp)
                    .background(shimmerGradient, RoundedCornerShape(4.dp))
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Content skeleton
        repeat(4) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(shimmerGradient, RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

// ==========================================
// PREVIEWS
// ==========================================

@Preview(showBackground = true)
@Composable
fun ArticleDetailScreenPreview() {
    MoonSyncTheme {
        ArticleDetailScreen(
            articleId = "1",
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ArticleDetailScreenDarkPreview() {
    MoonSyncTheme(darkTheme = true) {
        ArticleDetailScreen(
            articleId = "1",
            navController = rememberNavController()
        )
    }
}