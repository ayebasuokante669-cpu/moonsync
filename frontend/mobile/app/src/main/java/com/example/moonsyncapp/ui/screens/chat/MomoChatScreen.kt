package com.example.moonsyncapp.ui.screens.chat

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.moonsyncapp.R
import com.example.moonsyncapp.ui.theme.MoonSyncTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import com.example.moonsyncapp.data.model.ContentType
import com.example.moonsyncapp.data.model.ReportReason
import com.example.moonsyncapp.ui.screens.community.CommunityReportSheet
import com.example.moonsyncapp.ui.screens.community.ReportContext
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Check
import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.example.moonsyncapp.util.MediaHelper
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.filled.Pause
import com.example.moonsyncapp.util.AudioPlayer
import com.example.moonsyncapp.util.PlaybackState
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.Pause

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MomoChatScreen(
    navController: NavController,
    viewModel: MomoChatViewModel = viewModel()
) {
    val ui by viewModel.uiState.collectAsState()
    val reportTarget = ui.reportTargetMessage

    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current

    var showAttachSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(ui.messages.size) {
        if (ui.messages.isNotEmpty()) {
            listState.animateScrollToItem(ui.messages.lastIndex)
        }
    }

    // ADD media handling:
    val context = LocalContext.current
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

// Gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.attachImage(uri = uri, description = "Photo from gallery")
        }
    }

// Camera capture
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && pendingCameraUri != null) {
            viewModel.attachImage(uri = pendingCameraUri, description = "Camera photo")
        }
        pendingCameraUri = null
    }

// Camera permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = MediaHelper.createCameraImageUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

// Audio permission
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startRealRecording(context)
        }
    }


    val chatPlaybackState: PlaybackState by viewModel.audioPlayer.state.collectAsState()

    LaunchedEffect(chatPlaybackState.isPlaying) {
        while (chatPlaybackState.isPlaying) {
            kotlinx.coroutines.delay(200L)
            viewModel.audioPlayer.updatePosition()
        }
    }

    if (showAttachSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Share with Cyra 🌷",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(16.dp))

                // Gallery option
                AttachOptionRow(
                    icon = Icons.Outlined.AddPhotoAlternate,
                    title = "Choose from Gallery",
                    subtitle = "Select an existing photo",
                    emoji = "🖼️",
                    onClick = {
                        showAttachSheet = false
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )

                Spacer(Modifier.height(8.dp))

                // Camera option
                AttachOptionRow(
                    icon = Icons.Outlined.AddPhotoAlternate,
                    title = "Take a Photo",
                    subtitle = "Use your camera",
                    emoji = "📸",
                    onClick = {
                        showAttachSheet = false
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )

                Spacer(Modifier.height(8.dp))

                // Voice note option
                AttachOptionRow(
                    icon = Icons.Outlined.Mic,
                    title = "Voice note",
                    subtitle = "Record audio and send",
                    emoji = "🎙️",
                    onClick = {
                        showAttachSheet = false
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                )
            }
        }
    }

    // Report sheet for Momo messages
    reportTarget?.let { message ->
        CommunityReportSheet(
            contentType = ContentType.AI_MESSAGE,
            context = buildMomoReportContext(message),
            onDismissRequest = { viewModel.clearReportTarget() },
            onSubmitReport = { reason, notes ->
                viewModel.reportMomoMessage(message.id, reason, notes)
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        // Warm feminine gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                        )
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Header with warm styling
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Soft glow ring around Momo
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0f)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.momo),
                                contentDescription = "Cyra",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Cyra 🌸",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (ui.isMomoTyping) "Typing…" else "Your cycle companion",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                )
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(
                    items = ui.messages,
                    key = { _, m -> m.id }
                ) { index, msg ->
                    val prevAuthor = ui.messages.getOrNull(index - 1)?.author
                    val showMomoAvatar = (msg.author == ChatAuthor.MOMO) && (prevAuthor != ChatAuthor.MOMO)
                    val showUserBadge = (msg.author == ChatAuthor.USER) && (prevAuthor != ChatAuthor.USER)

                    ChatMessageRow(
                        message = msg,
                        showMomoAvatar = showMomoAvatar,
                        showUserBadge = showUserBadge,
                        onLongPress = if (msg.author == ChatAuthor.MOMO && !msg.hasCurrentUserReported) {
                            { viewModel.setReportTarget(msg) }
                        } else null,
                        audioPlayer = viewModel.audioPlayer,
                        playbackState = chatPlaybackState,
                        context = context
                    )
                }

                if (ui.isMomoTyping) {
                    item(key = "typing") { TypingRow() }
                }
            }

            ChatComposer(
                input = ui.input,
                onInputChange = viewModel::onInputChange,
                pendingAttachment = ui.pendingAttachment,
                onRemovePending = viewModel::clearPendingAttachment,
                isRecording = ui.isRecording,
                recordingMs = ui.recordingMs,
                onAttachClick = { showAttachSheet = true },
                onSend = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.send()
                },
                onMicToggle = {
                    if (ui.isRecording) {
                        viewModel.stopRealRecording()
                    } else {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
            )
        }
    }
}

@Composable
private fun ChatMessageRow(
    message: ChatMessage,
    showMomoAvatar: Boolean,
    showUserBadge: Boolean,
    onLongPress: (() -> Unit)? = null,
    audioPlayer: AudioPlayer? = null,
    playbackState: PlaybackState? = null,
    context: android.content.Context? = null
) {
    val isUser = message.author == ChatAuthor.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            if (showMomoAvatar) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.momo),
                        contentDescription = "Cyra avatar",
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                Spacer(Modifier.width(38.dp))
            }
            Spacer(Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (isUser) 20.dp else 6.dp,
                            bottomEnd = if (isUser) 6.dp else 20.dp
                        )
                    )
                    .then(
                        // Add border if Momo message is reported
                        if (!isUser && message.hasCurrentUserReported) {
                            Modifier.border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(
                                    topStart = 20.dp,
                                    topEnd = 20.dp,
                                    bottomStart = 6.dp,
                                    bottomEnd = 20.dp
                                )
                            )
                        } else Modifier
                    )
                    .background(
                        if (isUser) {
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            )
                        }
                    )
                    .then(
                        if (onLongPress != null) {
                            Modifier.pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = { onLongPress() }
                                )
                            }
                        } else Modifier
                    )
                    .padding(14.dp)
            ) {
                Column {
                    message.attachments.forEachIndexed { idx, att ->
                        when (att) {
                            is ChatAttachment.Image -> ImageAttachment(att)
                            is ChatAttachment.Audio -> {
                                if (audioPlayer != null && playbackState != null && context != null) {
                                    AudioAttachment(att, audioPlayer, playbackState, context)
                                } else {
                                    AudioAttachment(att, AudioPlayer(), PlaybackState(), context ?: return@forEachIndexed)
                                }
                            }
                        }
                        if (idx != message.attachments.lastIndex) Spacer(Modifier.height(8.dp))
                    }

                    if (message.attachments.isNotEmpty() && message.text != null) {
                        Spacer(Modifier.height(8.dp))
                    }

                    message.text?.let { t ->
                        Text(
                            text = t,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            color = if (isUser) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Show "Reported" indicator for Momo messages
                    if (!isUser && message.hasCurrentUserReported) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Feedback sent",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(5.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatTime(message.createdAt),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )

                if (isUser && showUserBadge) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "✨ You",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                } else if (!isUser && message.hasCurrentUserReported) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "• Reported",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TypingRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.momo),
                contentDescription = "Cyra avatar",
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(Modifier.width(8.dp))

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cyra is thinking",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.width(8.dp))
                DotPulse()
            }
        }
    }
}

@Composable
private fun DotPulse() {
    val t = rememberInfiniteTransition(label = "dots")
    val a1 by t.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(450), RepeatMode.Reverse),
        label = "a1"
    )
    val a2 by t.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(450, delayMillis = 120), RepeatMode.Reverse),
        label = "a2"
    )
    val a3 by t.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(450, delayMillis = 240), RepeatMode.Reverse),
        label = "a3"
    )

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = a1))
        )
        Box(
            Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = a2))
        )
        Box(
            Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = a3))
        )
    }
}

//@Composable
//private fun ImageAttachment(att: ChatAttachment.Image) {
//    Card(
//        shape = RoundedCornerShape(14.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
//        )
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .heightIn(min = 92.dp)
//                .padding(12.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Box(
//                modifier = Modifier
//                    .size(68.dp)
//                    .clip(RoundedCornerShape(12.dp))
//                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = Icons.Outlined.AddPhotoAlternate,
//                    contentDescription = null,
//                    tint = MaterialTheme.colorScheme.primary
//                )
//            }
//            Spacer(Modifier.width(12.dp))
//            Column(modifier = Modifier.weight(1f)) {
//                Text(
//                    text = "Image 📷",
//                    fontWeight = FontWeight.SemiBold,
//                    color = MaterialTheme.colorScheme.onSurface
//                )
//                Text(
//                    text = att.description,
//                    fontSize = 12.sp,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//        }
//    }
//}
@Composable
private fun ImageAttachment(att: ChatAttachment.Image) {
    var isFullScreen by remember { mutableStateOf(false) }

    if (isFullScreen && att.uri != null) {
        Dialog(onDismissRequest = { isFullScreen = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black).clickable { isFullScreen = false }, contentAlignment = Alignment.Center) {
                AsyncImage(model = att.uri, contentDescription = "Full screen", modifier = Modifier.fillMaxWidth().padding(16.dp), contentScale = ContentScale.Fit)
            }
        }
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier.clickable(enabled = att.uri != null) { isFullScreen = true }
    ) {
        Column {
            if (att.uri != null) {
                AsyncImage(
                    model = att.uri,
                    contentDescription = "Image",
                    modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (att.uri != null) "Tap to view" else att.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

//@Composable
//private fun AudioAttachment(att: ChatAttachment.Audio) {
//    val seconds = (att.durationMs / 1000L).toInt().coerceAtLeast(1)
//    Card(
//        shape = RoundedCornerShape(14.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
//        )
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(12.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Box(
//                modifier = Modifier
//                    .size(40.dp)
//                    .clip(CircleShape)
//                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = Icons.Filled.PlayArrow,
//                    contentDescription = "Play audio",
//                    tint = MaterialTheme.colorScheme.primary
//                )
//            }
//            Spacer(Modifier.width(12.dp))
//            Column(modifier = Modifier.weight(1f)) {
//                Text(
//                    text = "Voice note 🎙️",
//                    fontWeight = FontWeight.SemiBold,
//                    color = MaterialTheme.colorScheme.onSurface
//                )
//                Text(
//                    text = "$seconds sec",
//                    fontSize = 12.sp,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//        }
//    }
//}

@Composable
private fun AudioAttachment(
    att: ChatAttachment.Audio,
    audioPlayer: AudioPlayer,
    playbackState: PlaybackState,
    context: android.content.Context
) {
    val fileId = att.uri?.toString() ?: att.durationMs.toString()
    val isThisPlaying = playbackState.activeFileId == fileId
    val seconds = (att.durationMs / 1000L).toInt().coerceAtLeast(1)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isThisPlaying && playbackState.isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                        .clickable { att.uri?.let { audioPlayer.playOrPause(context, fileId, it.toString()) } },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isThisPlaying && playbackState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        tint = if (isThisPlaying && playbackState.isPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Voice note 🎙️", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Text("${seconds}s", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (isThisPlaying) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { playbackState.progress },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
@Composable
private fun ChatComposer(
    input: String,
    onInputChange: (String) -> Unit,
    pendingAttachment: ChatAttachment?,
    onRemovePending: () -> Unit,
    isRecording: Boolean,
    recordingMs: Long,
    onAttachClick: () -> Unit,
    onSend: () -> Unit,
    onMicToggle: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            AnimatedVisibility(visible = pendingAttachment != null) {
                PendingAttachmentRow(attachment = pendingAttachment, onRemove = onRemovePending)
            }

            AnimatedVisibility(visible = isRecording) {
                RecordingRow(recordingMs = recordingMs)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Softer attach button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .clickable(onClick = onAttachClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddPhotoAlternate,
                        contentDescription = "Attach",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.width(10.dp))

                OutlinedTextField(
                    value = input,
                    onValueChange = onInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            "Talk to Cyra… 💬",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(Modifier.width(10.dp))

                // Mic button - softer
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (isRecording)
                                MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            else
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                        )
                        .clickable { onMicToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Mic,
                        contentDescription = if (isRecording) "Stop recording" else "Start recording",
                        tint = if (isRecording)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.width(10.dp))

                val canSend = input.trim().isNotEmpty() || pendingAttachment != null

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (canSend) Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                            )
                            else Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onSend, enabled = canSend) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (canSend) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingAttachmentRow(
    attachment: ChatAttachment?,
    onRemove: () -> Unit
) {
    if (attachment == null) return

    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (label, emoji) = when (attachment) {
                is ChatAttachment.Image -> "Image attached" to "📷"
                is ChatAttachment.Audio -> "Voice note (${(attachment.durationMs / 1000L).coerceAtLeast(1)}s)" to "🎙️"
            }
            Text(
                text = "$emoji $label",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Remove attachment",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun RecordingRow(recordingMs: Long) {
    val seconds = recordingMs / 1000f
    Surface(
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Pulsing red dot
                val pulse = rememberInfiniteTransition(label = "pulse")
                val pulseAlpha by pulse.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
                    label = "pulseAlpha"
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = pulseAlpha))
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Recording… ${"%.1f".format(seconds)}s",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Release to attach 🎤",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { (seconds / 30f).coerceIn(0f, 1f) },
                color = MaterialTheme.colorScheme.error,
                trackColor = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.15f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
private fun AttachOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    emoji: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$title $emoji",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun formatTime(instant: Instant): String {
    val formatter = DateTimeFormatter.ofPattern("h:mm a")
    return formatter.format(instant.atZone(ZoneId.systemDefault()))
}

// ==========================================
// HELPER FUNCTIONS
// ==========================================

private fun buildMomoReportContext(message: ChatMessage): ReportContext {
    val title = "Message from Cyra"

    // Meta: timestamp
    val meta = formatTime(message.createdAt)

    // Snippet: message text or attachment description
    val rawSnippet = when {
        message.text != null -> message.text
        message.attachments.isNotEmpty() -> {
            message.attachments.joinToString(", ") { att ->
                when (att) {
                    is ChatAttachment.Image -> "Image: ${att.description}"
                    is ChatAttachment.Audio -> "Audio (${att.durationMs / 1000}s)"
                }
            }
        }
        else -> "Empty message"
    }

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

// ==========================================
// PREVIEWS
// ==========================================
@Preview(showBackground = true)
@Composable
private fun MomoChatScreenPreview() {
    MoonSyncTheme {
        MomoChatScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MomoChatScreenDarkPreview() {
    MoonSyncTheme(darkTheme = true) {
        MomoChatScreen(navController = rememberNavController())
    }
}