package com.example.moonsyncapp.ui.screens.logging

import androidx.navigation.compose.currentBackStackEntryAsState
import java.time.LocalDate
import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.moonsyncapp.data.model.*
import com.example.moonsyncapp.navigation.Routes
import com.example.moonsyncapp.ui.components.BottomNavigationBar
import com.example.moonsyncapp.ui.theme.MoonSyncTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoggingScreen(
    navController: NavController,
    viewModel: LoggingViewModel = viewModel(
        factory = LoggingViewModelFactory(androidx.compose.ui.platform.LocalContext.current)
    )
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val ui by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showAllLogsSheet by remember { mutableStateOf(false) }
    var showCustomItemSheet by remember { mutableStateOf(false) }

    // Show snackbar for today's log message
    LaunchedEffect(ui.showTodayLogMessage) {
        if (ui.showTodayLogMessage) {
            snackbarHostState.showSnackbar(
                message = "Use the form above to edit today's log 🌸",
                duration = SnackbarDuration.Short
            )
            viewModel.dismissTodayLogMessage()
        }
    }

    // Custom item sheet
    if (showCustomItemSheet || ui.isAddingCustomItem) {
        AddCustomItemSheet(
            input = ui.customItemInput,
            category = ui.customItemCategory,
            onInputChange = viewModel::updateCustomItemInput,
            onCategoryChange = viewModel::updateCustomItemCategory,
            onSave = {
                viewModel.saveCustomItem()
                showCustomItemSheet = false
            },
            onDismiss = {
                viewModel.hideAddCustomItem()
                showCustomItemSheet = false
            }
        )
    }

    // View all logs sheet
    if (showAllLogsSheet) {
        ViewAllLogsSheet(
            logs = ui.recentLogs,
            onDismiss = { showAllLogsSheet = false },
            onEditLog = { log ->
                viewModel.startEditingLog(log)
                showAllLogsSheet = false
            },
            onDeleteLog = viewModel::deleteLog,
            formatDate = viewModel::formatDate
        )
    }

    // Edit log sheet
    if (ui.editingLog != null) {
        EditLogSheet(
            log = ui.editingLog!!,
            selectedItems = ui.editSelectedItems,
            formText = ui.editFormText,
            attachments = ui.editAttachments,
            isRecording = ui.isEditRecordingVoice,
            recordingMs = ui.editRecordingDurationMs,
            allItems = ui.allQuickTapItems + ui.customItems,
            onDismiss = viewModel::cancelEditingLog,
            onSave = viewModel::saveEditedLog,
            onTextChange = viewModel::updateEditFormText,
            onToggleItem = viewModel::toggleEditItem,
            isItemSelected = viewModel::isEditItemSelected,
            onRemoveAttachment = viewModel::removeEditAttachment,
            onVoiceStart = viewModel::startEditVoiceRecording,
            onVoiceStop = viewModel::stopEditVoiceRecording,
            onPhotoClick = viewModel::attachEditPhoto,
            formatDate = viewModel::formatDate
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Warm gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.04f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f)
                        )
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Scrollable content (no header bar)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 100.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                // Title + Streak row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Log 📝",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // Streak badge
                    if (ui.streak.currentStreak > 0) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔥",
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "${ui.streak.currentStreak}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Today's status indicator
                TodayStatusCard(todayLog = ui.todayLog)

                Spacer(Modifier.height(20.dp))

                // Quick-tap section header
                Text(
                    text = "How are you feeling? 🌸",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Tap all that apply • auto-saves at midnight",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                Spacer(Modifier.height(16.dp))

                // Physical symptoms
                QuickTapCategorySection(
                    title = "Physical",
                    emoji = "💪",
                    items = viewModel.getItemsByCategory(QuickTapCategory.PHYSICAL),
                    isItemSelected = viewModel::isItemSelected,
                    onItemClick = viewModel::toggleQuickTapItem,
                    onAddCustom = {
                        viewModel.showAddCustomItem(QuickTapCategory.PHYSICAL)
                        showCustomItemSheet = true
                    }
                )

                Spacer(Modifier.height(16.dp))

                // Emotional
                QuickTapCategorySection(
                    title = "Emotional",
                    emoji = "💜",
                    items = viewModel.getItemsByCategory(QuickTapCategory.EMOTIONAL),
                    isItemSelected = viewModel::isItemSelected,
                    onItemClick = viewModel::toggleQuickTapItem,
                    onAddCustom = {
                        viewModel.showAddCustomItem(QuickTapCategory.EMOTIONAL)
                        showCustomItemSheet = true
                    }
                )

                Spacer(Modifier.height(16.dp))

                // Lifestyle
                QuickTapCategorySection(
                    title = "Lifestyle",
                    emoji = "🌿",
                    items = viewModel.getItemsByCategory(QuickTapCategory.LIFESTYLE),
                    isItemSelected = viewModel::isItemSelected,
                    onItemClick = viewModel::toggleQuickTapItem,
                    onAddCustom = {
                        viewModel.showAddCustomItem(QuickTapCategory.LIFESTYLE)
                        showCustomItemSheet = true
                    }
                )

                Spacer(Modifier.height(24.dp))

                // Free-form entry section
                FreeFormEntryCard(
                    text = ui.freeFormInput,
                    onTextChange = viewModel::updateFreeFormInput,
                    attachments = ui.todayLog.attachments,
                    onRemoveAttachment = viewModel::removeAttachment,
                    isRecording = ui.isRecordingVoice,
                    recordingMs = ui.recordingDurationMs,
                    onVoiceStart = viewModel::startVoiceRecording,
                    onVoiceStop = viewModel::stopVoiceRecording,
                    onPhotoClick = viewModel::attachPhoto,
                    onSave = viewModel::saveFreeFormEntry
                )

                Spacer(Modifier.height(24.dp))

                // Recent logs section
                RecentLogsSection(
                    logs = ui.recentLogs,
                    expandedLogId = ui.expandedLogId,
                    onToggleExpand = viewModel::toggleLogExpanded,
                    onEditLog = viewModel::startEditingLog,
                    formatDate = viewModel::formatDate,
                    showAllLogs = ui.showAllLogs,
                    onToggleShowAll = viewModel::toggleShowAllLogs,
                    onViewAllClick = { showAllLogsSheet = true }
                )

                Spacer(Modifier.height(24.dp))
            }
        }

        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Bottom navigation
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

@Composable
private fun TodayStatusCard(todayLog: DailyLog) {
    val hasContent = todayLog.hasContent

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (hasContent)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (hasContent)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (hasContent) "✨" else "🌙",
                    fontSize = 20.sp
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (hasContent) "Today's log in progress" else "No entries yet today",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (hasContent)
                        "${todayLog.selectedItems.size} items selected"
                    else
                        "Tap symptoms below to start logging",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            if (hasContent) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickTapCategorySection(
    title: String,
    emoji: String,
    items: List<QuickTapItem>,
    isItemSelected: (QuickTapItem) -> Boolean,
    onItemClick: (QuickTapItem) -> Unit,
    onAddCustom: () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            Text(
                text = emoji,
                fontSize = 16.sp
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(end = 8.dp)
        ) {
            items(items, key = { it.id }) { item ->
                QuickTapChip(
                    item = item,
                    isSelected = isItemSelected(item),
                    onClick = { onItemClick(item) }
                )
            }

            // Add custom button
            item(key = "add_custom") {
                AddCustomChip(onClick = onAddCustom)
            }
        }
    }
}

@Composable
private fun QuickTapChip(
    item: QuickTapItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected)
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    else
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    val borderColor = if (isSelected)
        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    else
        Color.Transparent

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(
                    width = 1.5.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(20.dp)
                ) else Modifier
            ),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.emoji,
                fontSize = 16.sp
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = item.label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface
            )
            if (isSelected) {
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun AddCustomChip(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add custom",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Custom",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun FreeFormEntryCard(
    text: String,
    onTextChange: (String) -> Unit,
    attachments: List<LogAttachment>,
    onRemoveAttachment: (LogAttachment) -> Unit,
    isRecording: Boolean,
    recordingMs: Long,
    onVoiceStart: () -> Unit,
    onVoiceStop: () -> Unit,
    onPhotoClick: () -> Unit,
    onSave: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = "📝",
                    fontSize = 18.sp
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Write something...",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Text input
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                placeholder = {
                    Text(
                        "How was your day? Any thoughts to capture? 💭",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.02f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                )
            )

            // Attachments preview
            if (attachments.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    attachments.forEach { attachment ->
                        AttachmentPreviewChip(
                            attachment = attachment,
                            onRemove = { onRemoveAttachment(attachment) }
                        )
                    }
                }
            }

            // Recording indicator
            AnimatedVisibility(visible = isRecording) {
                RecordingIndicator(recordingMs = recordingMs)
            }

            Spacer(Modifier.height(14.dp))

            // Action buttons row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isRecording)
                                MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            else
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    onVoiceStart()
                                    tryAwaitRelease()
                                    onVoiceStop()
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Mic,
                        contentDescription = "Hold to record",
                        tint = if (isRecording)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Photo button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                        .clickable(onClick = onPhotoClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddPhotoAlternate,
                        contentDescription = "Add photo",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.weight(1f))

                // Save button
                Button(
                    onClick = onSave,
                    enabled = text.trim().isNotBlank() || attachments.isNotEmpty(),
                    modifier = Modifier.height(44.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Save",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun AttachmentPreviewChip(
    attachment: LogAttachment,
    onRemove: () -> Unit
) {
    val (emoji, label) = when (attachment) {
        is LogAttachment.VoiceNote -> "🎙️" to "Voice note (${(attachment.durationMs / 1000).coerceAtLeast(1)}s)"
        is LogAttachment.Photo -> "📷" to "Photo attached"
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 14.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun RecordingIndicator(recordingMs: Long) {
    val seconds = recordingMs / 1000f
    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulse.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "pulseAlpha"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                text = "Release to save 🎤",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun RecentLogsSection(
    logs: List<DailyLog>,
    expandedLogId: String?,
    onToggleExpand: (String) -> Unit,
    onEditLog: (DailyLog) -> Unit,
    formatDate: (java.time.LocalDate) -> String,
    showAllLogs: Boolean,
    onToggleShowAll: () -> Unit,
    onViewAllClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Logs 📚",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(onClick = onViewAllClick) {
                Text("View all")
            }
        }

        Spacer(Modifier.height(12.dp))

        val displayLogs = if (showAllLogs) logs else logs.take(3)

        if (displayLogs.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🌙", fontSize = 32.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "No past logs yet",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Start logging today!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                displayLogs.forEach { log ->
                    LogEntryCard(
                        log = log,
                        isExpanded = expandedLogId == log.id,
                        onToggleExpand = { onToggleExpand(log.id) },
                        onEditLog = { onEditLog(log) },
                        formatDate = formatDate
                    )
                }
            }
        }
    }
}

@Composable
private fun LogEntryCard(
    log: DailyLog,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onEditLog: () -> Unit,
    formatDate: (java.time.LocalDate) -> String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onToggleExpand),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = formatDate(log.date),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (log.isEdited) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "(edited)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }

                // Edit button (visible when expanded)
                AnimatedVisibility(visible = isExpanded) {
                    IconButton(
                        onClick = onEditLog,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit log",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            // Quick-tap items preview (always visible)
            if (log.selectedItems.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    log.selectedItems.take(if (isExpanded) log.selectedItems.size else 5).forEach { item ->
                        Text(
                            text = "${item.emoji} ${item.label}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (!isExpanded && log.selectedItems.size > 5) {
                        Text(
                            text = "+${log.selectedItems.size - 5}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Free-form text preview
            if (log.freeFormText.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = if (isExpanded) log.freeFormText else log.freeFormText.take(80) + if (log.freeFormText.length > 80) "..." else "",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            // Expanded content
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Attachments
                    if (log.attachments.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        log.attachments.forEach { attachment ->
                            val (emoji, label) = when (attachment) {
                                is LogAttachment.VoiceNote -> "🎙️" to "Voice note (${(attachment.durationMs / 1000).coerceAtLeast(1)}s)"
                                is LogAttachment.Photo -> "📷" to "Photo"
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = emoji, fontSize = 14.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.weight(1f))
                                if (attachment is LogAttachment.VoiceNote) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = "Play",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCustomItemSheet(
    input: String,
    category: QuickTapCategory,
    onInputChange: (String) -> Unit,
    onCategoryChange: (QuickTapCategory) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Add Custom Item ✨",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Create your own quick-tap item (max ${LoggingViewModel.MAX_CUSTOM_LABEL_LENGTH} characters)",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(20.dp))

            // Input field
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., Took vitamins") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                supportingText = {
                    Text(
                        text = "${input.length}/${LoggingViewModel.MAX_CUSTOM_LABEL_LENGTH}",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        color = if (input.length >= LoggingViewModel.MAX_CUSTOM_LABEL_LENGTH)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            Spacer(Modifier.height(20.dp))

            // Category selection
            Text(
                text = "Category",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CategoryChip(
                    label = "Physical",
                    emoji = "💪",
                    isSelected = category == QuickTapCategory.PHYSICAL,
                    onClick = { onCategoryChange(QuickTapCategory.PHYSICAL) },
                    modifier = Modifier.weight(1f)
                )
                CategoryChip(
                    label = "Emotional",
                    emoji = "💜",
                    isSelected = category == QuickTapCategory.EMOTIONAL,
                    onClick = { onCategoryChange(QuickTapCategory.EMOTIONAL) },
                    modifier = Modifier.weight(1f)
                )
                CategoryChip(
                    label = "Lifestyle",
                    emoji = "🌿",
                    isSelected = category == QuickTapCategory.LIFESTYLE,
                    onClick = { onCategoryChange(QuickTapCategory.LIFESTYLE) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(28.dp))

            // Save button
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = input.trim().isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Add Item ✨",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(14.dp)
                ) else Modifier
            ),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewAllLogsSheet(
    logs: List<DailyLog>,
    onDismiss: () -> Unit,
    onEditLog: (DailyLog) -> Unit,
    onDeleteLog: (String) -> Unit,
    formatDate: (LocalDate) -> String
) {
    var searchQuery by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxHeight(0.9f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            // Header
            Text(
                text = "All Logs 📚",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(16.dp))

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search logs...") },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null)
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            Spacer(Modifier.height(16.dp))

            // Filter chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    FilterChip(
                        selected = false,
                        onClick = { /* TODO: Date filter */ },
                        label = { Text("Last 7 days") }
                    )
                }
                item {
                    FilterChip(
                        selected = false,
                        onClick = { /* TODO: Has attachments filter */ },
                        label = { Text("With photos") }
                    )
                }
                item {
                    FilterChip(
                        selected = false,
                        onClick = { /* TODO: Has voice filter */ },
                        label = { Text("Voice notes") }
                    )
                }
            }

            // Logs list
            val filteredLogs = logs.filter { log ->
                searchQuery.isEmpty() ||
                        log.freeFormText.contains(searchQuery, ignoreCase = true) ||
                        log.selectedItems.any { it.label.contains(searchQuery, ignoreCase = true) }
            }

            if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🔍", fontSize = 32.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) "No logs yet" else "No logs found",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(
                        items = filteredLogs,
                        key = { it.id }
                    ) { log ->
                        SwipeableLogCard(
                            log = log,
                            formatDate = formatDate,
                            onEdit = { onEditLog(log) },
                            onDelete = { onDeleteLog(log.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableLogCard(
    log: DailyLog,
    formatDate: (LocalDate) -> String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                        RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        }
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatDate(log.date),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (log.isEdited) {
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "(edited)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                    if (log.freeFormText.isNotBlank()) {
                        Text(
                            text = log.freeFormText.take(60) + if (log.freeFormText.length > 60) "..." else "",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Text(
                        text = "${log.selectedItems.size} items logged",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditLogSheet(
    log: DailyLog,
    selectedItems: List<QuickTapItem>,
    formText: String,
    attachments: List<LogAttachment>,
    isRecording: Boolean,
    recordingMs: Long,
    allItems: List<QuickTapItem>,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onTextChange: (String) -> Unit,
    onToggleItem: (QuickTapItem) -> Unit,
    isItemSelected: (QuickTapItem) -> Boolean,
    onRemoveAttachment: (LogAttachment) -> Unit,
    onVoiceStart: () -> Unit,
    onVoiceStop: () -> Unit,
    onPhotoClick: () -> Unit,
    formatDate: (LocalDate) -> String
) {
    val scrollState = rememberScrollState()
    val isValid = selectedItems.isNotEmpty() || formText.isNotBlank() || attachments.isNotEmpty()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxHeight(0.92f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Edit Log ✏️",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = formatDate(log.date),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                // Quick-tap section header
                Text(
                    text = "How were you feeling? 🌸",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(14.dp))

                // Physical symptoms
                EditQuickTapCategorySection(
                    title = "Physical",
                    emoji = "💪",
                    items = allItems.filter { it.category == QuickTapCategory.PHYSICAL },
                    isItemSelected = isItemSelected,
                    onItemClick = onToggleItem
                )

                Spacer(Modifier.height(14.dp))

                // Emotional
                EditQuickTapCategorySection(
                    title = "Emotional",
                    emoji = "💜",
                    items = allItems.filter { it.category == QuickTapCategory.EMOTIONAL },
                    isItemSelected = isItemSelected,
                    onItemClick = onToggleItem
                )

                Spacer(Modifier.height(14.dp))

                // Lifestyle
                EditQuickTapCategorySection(
                    title = "Lifestyle",
                    emoji = "🌿",
                    items = allItems.filter { it.category == QuickTapCategory.LIFESTYLE },
                    isItemSelected = isItemSelected,
                    onItemClick = onToggleItem
                )

                Spacer(Modifier.height(24.dp))

                // Free-form text
                Text(
                    text = "Notes 📝",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = formText,
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    placeholder = {
                        Text(
                            "Any thoughts to capture? 💭",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.02f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    )
                )

                // Attachments preview
                if (attachments.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Attachments",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        attachments.forEach { attachment ->
                            AttachmentPreviewChip(
                                attachment = attachment,
                                onRemove = { onRemoveAttachment(attachment) }
                            )
                        }
                    }
                }

                // Recording indicator
                AnimatedVisibility(visible = isRecording) {
                    RecordingIndicator(recordingMs = recordingMs)
                }

                Spacer(Modifier.height(16.dp))

                // Add attachments row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Voice button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isRecording)
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                else
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                            )
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        onVoiceStart()
                                        tryAwaitRelease()
                                        onVoiceStop()
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Mic,
                            contentDescription = "Hold to record",
                            tint = if (isRecording)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Photo button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                            .clickable(onClick = onPhotoClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddPhotoAlternate,
                            contentDescription = "Add photo",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            // Validation message
            AnimatedVisibility(visible = !isValid) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "💡", fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Add at least one item, note, or attachment",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Bottom buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cancel button
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Save button
                Button(
                    onClick = onSave,
                    enabled = isValid,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Save Changes",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun EditQuickTapCategorySection(
    title: String,
    emoji: String,
    items: List<QuickTapItem>,
    isItemSelected: (QuickTapItem) -> Boolean,
    onItemClick: (QuickTapItem) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            Text(
                text = emoji,
                fontSize = 14.sp
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 8.dp)
        ) {
            items(items, key = { it.id }) { item ->
                QuickTapChip(
                    item = item,
                    isSelected = isItemSelected(item),
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoggingScreenPreview() {
    MoonSyncTheme {
        LoggingScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoggingScreenDarkPreview() {
    MoonSyncTheme(darkTheme = true) {
        LoggingScreen(navController = rememberNavController())
    }
}