package com.example.moonsyncapp.ui.screens.community

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.moonsyncapp.data.model.*
import com.example.moonsyncapp.navigation.Routes
import com.example.moonsyncapp.ui.theme.MoonSyncTheme
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import com.example.moonsyncapp.data.model.ContentType
import com.example.moonsyncapp.data.model.ReportReason

// Comment sorting options enum
enum class CommentSortOption(val displayName: String) {
    MOST_HELPFUL("Most helpful"),
    PROFESSIONALS_FIRST("Professionals first"),
    NEWEST("Newest"),
    OLDEST("Oldest")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    navController: NavController
) {
    // Get ViewModel from CommunityScreen's back stack entry (shared instance)
    val parentEntry = remember(navController) {
        navController.getBackStackEntry(Routes.COMMUNITY)
    }
    val viewModel: CommunityViewModel = viewModel(parentEntry)

    val posts by viewModel.posts.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Find the post by ID
    val post = posts.find { it.id == postId }

    // Better handling: show loading instead of immediately popping back
    if (post == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Loading post...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Only pop back after a reasonable timeout if still not found
        LaunchedEffect(postId) {
            delay(3000) // Wait 3 seconds
            if (posts.isNotEmpty() && posts.none { it.id == postId }) {
                // Posts loaded but this post doesn't exist
                navController.popBackStack()
            }
        }
        return
    }

    var commentText by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<PostComment?>(null) }
    var showReactionPicker by remember { mutableStateOf(false) }
    var postAnonymously by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf(CommentSortOption.MOST_HELPFUL) }
    var showSortMenu by remember { mutableStateOf(false) }
    var reportCommentTarget by remember { mutableStateOf<PostComment?>(null) }
    val focusManager = LocalFocusManager.current

    // Mock comments (in real app, fetch from ViewModel)
    val comments = remember { getMockComments(postId) }

    // Sort comments based on selected option
    val sortedComments = remember(comments, sortOption) {
        when (sortOption) {
            CommentSortOption.MOST_HELPFUL ->
                comments.sortedByDescending { comment ->
                    comment.reactions.find { it.type == ReactionType.HELPFUL }?.count ?: 0
                }
            CommentSortOption.PROFESSIONALS_FIRST ->
                comments.sortedWith(
                    compareByDescending<PostComment> { it.isVerifiedProfessional }
                        .thenByDescending { it.createdAt }
                )
            CommentSortOption.NEWEST ->
                comments.sortedByDescending { it.createdAt }
            CommentSortOption.OLDEST ->
                comments.sortedBy { it.createdAt }
        }
    }

    // Report sheet for comments
    reportCommentTarget?.let { comment ->
        CommunityReportSheet(
            contentType = ContentType.COMMENT,
            context = buildCommentReportContext(post, comment),
            onDismissRequest = { reportCommentTarget = null },
            onSubmitReport = { reason, notes ->
                // TODO: Wire to ViewModel when reportComment() is implemented
                // viewModel.reportComment(comment.id, reason, notes)
                reportCommentTarget = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Comments",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            CommentInputBar(
                value = commentText,
                onValueChange = { commentText = it },
                replyingTo = replyingTo,
                onCancelReply = { replyingTo = null },
                postAnonymously = postAnonymously,
                onAnonymousToggle = { postAnonymously = it },
                onSend = {
                    if (commentText.isNotBlank()) {
                        // TODO: Send comment via ViewModel
                        commentText = ""
                        replyingTo = null
                        focusManager.clearFocus()
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Original post (condensed version)
            item {
                PostDetailHeader(
                    post = post,
                    onReact = { reactionType ->
                        viewModel.reactToPost(post.id, reactionType)
                    },
                    onShare = {
                        // TODO: Implement share
                    },
                    onShowReactions = {
                        showReactionPicker = !showReactionPicker
                    }
                )
            }

            // Reaction picker
            if (showReactionPicker) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        ReactionPicker(
                            reactions = post.reactions,
                            onReact = { reactionType ->
                                viewModel.reactToPost(post.id, reactionType)
                                showReactionPicker = false
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Comments header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Comments (${comments.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // Sort dropdown
                    Box {
                        TextButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = sortOption.displayName,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            CommentSortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = option.displayName,
                                                    fontSize = 14.sp,
                                                    fontWeight = if (option == sortOption) FontWeight.Medium else FontWeight.Normal,
                                                    color = if (option == sortOption) {
                                                        MaterialTheme.colorScheme.primary
                                                    } else {
                                                        MaterialTheme.colorScheme.onSurface
                                                    }
                                                )
                                                // Add description for each option
                                                Text(
                                                    text = when (option) {
                                                        CommentSortOption.MOST_HELPFUL -> "Most helpful reactions first"
                                                        CommentSortOption.PROFESSIONALS_FIRST -> "Verified professionals at top"
                                                        CommentSortOption.NEWEST -> "Recent comments first"
                                                        CommentSortOption.OLDEST -> "Oldest comments first"
                                                    },
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                )
                                            }
                                            if (option == sortOption) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        sortOption = option
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Comments list
            items(sortedComments) { comment ->
                CommentItem(
                    comment = comment,
                    currentUserId = currentUser.id,
                    onReply = { replyingTo = comment },
                    onReact = { reactionType ->
                        // TODO: React to comment
                    },
                    onReport = {
                        reportCommentTarget = comment
                    }
                )
            }

            // Empty state
            if (sortedComments.isEmpty()) {
                item {
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
                                text = "💬",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No comments yet",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Be the first to share your thoughts!",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PostDetailHeader(
    post: CommunityPost,
    onReact: (ReactionType) -> Unit,
    onShare: () -> Unit,
    onShowReactions: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Author row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                UserAvatar(user = post.author, size = 44.dp)

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
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

            Spacer(modifier = Modifier.height(16.dp))

            // Reactions summary
            ReactionsSummary(
                reactions = post.reactions,
                onReactionClick = onShowReactions
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // React button
                TextButton(onClick = onShowReactions) {
                    Icon(
                        imageVector = Icons.Outlined.AddReaction,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "React",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Share button
                TextButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Share",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentItem(
    comment: PostComment,
    currentUserId: String,
    onReply: () -> Unit,
    onReact: (ReactionType) -> Unit,
    onReport: () -> Unit
) {
    var showReactionPicker by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (comment.isVerifiedProfessional) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .then(
                                if (comment.isAnonymous) {
                                    Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                                } else {
                                    Modifier.background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                                MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    )
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (comment.isAnonymous) "🌙" else comment.author.currentMood?.emoji ?: comment.author.wisdomLevel.emoji,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (comment.isAnonymous) "Anonymous Sister" else comment.author.displayName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            if (comment.isVerifiedProfessional) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            if (comment.isVerifiedProfessional && comment.author.professionalTitle != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                ) {
                                    Text(
                                        text = comment.author.professionalTitle,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = formatTimeAgo(comment.createdAt),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Comment content
                        Text(
                            text = comment.content,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Reaction summary (if any)
                        if (comment.reactions.any { it.count > 0 }) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                comment.reactions
                                    .filter { it.count > 0 }
                                    .sortedByDescending { it.count }
                                    .take(3)
                                    .forEach { reaction ->
                                        Text(
                                            text = reaction.type.emoji,
                                            fontSize = 14.sp
                                        )
                                    }

                                Spacer(modifier = Modifier.width(6.dp))

                                Text(
                                    text = comment.reactions.sumOf { it.count }.toString(),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Action buttons
                        Row(
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            TextButton(
                                onClick = { showReactionPicker = !showReactionPicker },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AddReaction,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "React",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            TextButton(
                                onClick = onReply,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Reply,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Reply",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Options menu
                Box {
                    IconButton(
                        onClick = { showOptionsMenu = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false }
                    ) {
                        if (comment.author.id == currentUserId && !comment.isAnonymous) {
                            DropdownMenuItem(
                                text = { Text("Edit", fontSize = 14.sp) },
                                onClick = {
                                    showOptionsMenu = false
                                    // TODO: Edit comment
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Edit, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", fontSize = 14.sp) },
                                onClick = {
                                    showOptionsMenu = false
                                    // TODO: Delete comment
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Delete, contentDescription = null)
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Report", fontSize = 14.sp) },
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

            // Mini reaction picker
            AnimatedVisibility(visible = showReactionPicker) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(ReactionType.values().toList()) { reactionType ->
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .size(36.dp)
                                .clickable {
                                    onReact(reactionType)
                                    showReactionPicker = false
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = reactionType.emoji,
                                    fontSize = 18.sp
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
private fun CommentInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    replyingTo: PostComment?,
    onCancelReply: () -> Unit,
    postAnonymously: Boolean,
    onAnonymousToggle: (Boolean) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 8.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // Reply indicator
            AnimatedVisibility(visible = replyingTo != null) {
                replyingTo?.let { comment ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Reply,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Replying to ${if (comment.isAnonymous) "Anonymous Sister" else comment.author.displayName}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = onCancelReply,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel reply",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Anonymous toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (postAnonymously) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        }
                    )
                    .clickable { onAnonymousToggle(!postAnonymously) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (postAnonymously) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null,
                    tint = if (postAnonymously) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (postAnonymously) "Commenting anonymously" else "Commenting as you",
                    fontSize = 12.sp,
                    color = if (postAnonymously) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = postAnonymously,
                    onCheckedChange = onAnonymousToggle,
                    modifier = Modifier.height(20.dp)
                )
            }

            // Input field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            if (replyingTo != null) "Write a reply..." else "Add a comment...",
                            fontSize = 14.sp
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSend() })
                )

                Spacer(modifier = Modifier.width(12.dp))

                FloatingActionButton(
                    onClick = onSend,
                    modifier = Modifier.size(48.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
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

// Re-use existing components from CommunityScreen
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
                modifier = Modifier.clickable { onReact(reactionType) }
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

// Helper functions
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

// Build report context for comments
private fun buildCommentReportContext(post: CommunityPost, comment: PostComment): ReportContext {
    val isAnonymous = comment.isAnonymous

    val authorName = if (isAnonymous) {
        "Anonymous Sister"
    } else {
        comment.author.displayName
    }

    val title = "Comment from $authorName"

    // Meta: show it's on a specific post + professional badge if applicable
    val metaParts = mutableListOf<String>()
    metaParts += "on post by ${post.author.displayName}"
    if (comment.isVerifiedProfessional && comment.author.professionalTitle != null) {
        metaParts += comment.author.professionalTitle
    }
    metaParts += formatTimeAgo(comment.createdAt)

    val meta = metaParts.joinToString(" • ")

    val rawSnippet = comment.content.trim()
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

// Mock data for comments
private fun getMockComments(postId: String): List<PostComment> {
    return listOf(
        PostComment(
            id = "comment_1",
            postId = postId,
            author = CommunityUser(
                id = "user_c1",
                displayName = "SupportSister",
                identityMode = IdentityMode.PSEUDONYM,
                currentPhase = CyclePhase.LUTEAL,
                currentMood = UserMood.PEACEFUL,
                wisdomLevel = WisdomLevel.FLOURISHING,
                isVerifiedProfessional = false,
                professionalTitle = null,
                joinedDate = LocalDateTime.now().minusMonths(5),
                localCircle = "Lagos"
            ),
            content = "This is so helpful! I've been struggling with the same thing and it's nice to know I'm not alone. Thank you for sharing 💜",
            createdAt = LocalDateTime.now().minusHours(3),
            reactions = listOf(
                PostReaction(ReactionType.HUG, 12, false),
                PostReaction(ReactionType.LOVE, 8, false),
                PostReaction(ReactionType.RELATE, 15, false)
            ),
            isAnonymous = false
        ),
        PostComment(
            id = "comment_2",
            postId = postId,
            author = CommunityUser(
                id = "user_doc",
                displayName = "Dr. Amara O.",
                identityMode = IdentityMode.REAL_NAME,
                currentPhase = null,
                currentMood = null,
                wisdomLevel = WisdomLevel.WISE_TREE,
                isVerifiedProfessional = true,
                professionalTitle = "Gynecologist",
                joinedDate = LocalDateTime.now().minusYears(1),
                localCircle = "Lagos"
            ),
            content = "Great question! From a medical perspective, this is completely normal. Many women experience this during their cycle. I recommend tracking your symptoms and discussing them with your healthcare provider if they become severe.",
            createdAt = LocalDateTime.now().minusHours(2),
            reactions = listOf(
                PostReaction(ReactionType.HELPFUL, 45, false),
                PostReaction(ReactionType.WISDOM, 32, false),
                PostReaction(ReactionType.LOVE, 18, false)
            ),
            isAnonymous = false,
            isVerifiedProfessional = true
        ),
        PostComment(
            id = "comment_3",
            postId = postId,
            author = CommunityUser(
                id = "user_c3",
                displayName = "Luna #2847",
                identityMode = IdentityMode.ANONYMOUS,
                currentPhase = CyclePhase.MENSTRUAL,
                currentMood = UserMood.ANXIOUS,
                wisdomLevel = WisdomLevel.SEEDLING,
                isVerifiedProfessional = false,
                professionalTitle = null,
                joinedDate = LocalDateTime.now().minusWeeks(3),
                localCircle = null
            ),
            content = "I started using a heating pad and it's been a game changer! Also drinking lots of ginger tea helps me.",
            createdAt = LocalDateTime.now().minusMinutes(30),
            reactions = listOf(
                PostReaction(ReactionType.HELPFUL, 6, false),
                PostReaction(ReactionType.HUG, 3, false)
            ),
            isAnonymous = true
        )
    )
}

// Previews
@Preview(showBackground = true)
@Composable
fun PostDetailScreenPreview() {
    MoonSyncTheme {
        // For preview, we can't properly share ViewModel, so just show a placeholder
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("PostDetailScreen Preview")
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PostDetailScreenDarkPreview() {
    MoonSyncTheme(darkTheme = true) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("PostDetailScreen Preview (Dark)")
        }
    }
}