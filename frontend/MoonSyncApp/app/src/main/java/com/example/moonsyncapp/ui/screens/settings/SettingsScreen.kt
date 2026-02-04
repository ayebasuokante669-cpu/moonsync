@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.moonsyncapp.ui.screens.settings

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.moonsyncapp.data.auth.AuthManager
import com.example.moonsyncapp.data.model.*
import com.example.moonsyncapp.data.settings.SettingsManager
import com.example.moonsyncapp.navigation.Routes
import com.example.moonsyncapp.notifications.NotificationHelper
import com.example.moonsyncapp.ui.components.BottomNavigationBar
import com.example.moonsyncapp.ui.theme.LocalThemeManager
import com.example.moonsyncapp.ui.theme.MoonSyncTheme
import com.example.moonsyncapp.ui.theme.ThemeManager
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(
    navController: NavController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val context = LocalContext.current

    // Check if we're in preview mode
    val isPreview = LocalInspectionMode.current

    // Only create real managers when NOT in preview
    val themeManager = if (isPreview) null else (LocalThemeManager.current ?: ThemeManager(context))
    val authManager = if (isPreview) null else remember { AuthManager(context) }
    val settingsManager = if (isPreview) null else remember { SettingsManager(context) }
    val notificationHelper = if (isPreview) null else remember { NotificationHelper(context) }

    // Use preview-safe ViewModel creation
    val viewModel: SettingsViewModel = if (isPreview) {
        // Create a simple ViewModel for preview
        remember { SettingsViewModel() }
    } else {
        viewModel(
            factory = SettingsViewModelFactory(
                context = context,
                themeManager = themeManager,
                authManager = authManager,
                settingsManager = settingsManager,
                notificationHelper = notificationHelper
            )
        )
    }

    val ui by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Detect system theme
    val isSystemDarkTheme = isSystemInDarkTheme()

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateProfilePhoto(uri)
        }
    }

    // Handle navigation events (skip in preview)
    if (!isPreview) {
        LaunchedEffect(Unit) {
            viewModel.navigationEvent.collect { event ->
                when (event) {
                    is SettingsNavigationEvent.NavigateToLogin -> {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                    is SettingsNavigationEvent.NavigateToSetup -> {
                        navController.navigate(Routes.WELCOME) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                    is SettingsNavigationEvent.OpenUrl -> {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    is SettingsNavigationEvent.OpenEmail -> {
                        try {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:")
                                putExtra(Intent.EXTRA_EMAIL, arrayOf(event.email))
                                putExtra(Intent.EXTRA_SUBJECT, event.subject)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    is SettingsNavigationEvent.ShowMessage -> {
                        android.widget.Toast.makeText(
                            context,
                            event.message,
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    // ==================== DIALOGS ====================

    if (ui.showEditNameDialog) {
        EditNameDialog(
            currentName = ui.editNameInput,
            onNameChange = viewModel::updateNameInput,
            onDismiss = viewModel::hideEditNameDialog,
            onSave = viewModel::saveName
        )
    }

    if (ui.showPhotoOptions) {
        PhotoOptionsSheet(
            hasPhoto = ui.settings.profile.photoUri != null,
            onSelectPhoto = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onRemovePhoto = viewModel::removeProfilePhoto,
            onDismiss = viewModel::hidePhotoOptions
        )
    }

    if (ui.showResetDataDialog) {
        ConfirmationDialog(
            title = "Reset All Data?",
            message = "This will clear all your logs and cycle tracking. You'll need to set up again.",
            confirmText = "Reset",
            onConfirm = viewModel::resetAllData,
            onDismiss = viewModel::hideResetDataDialog,
            isDestructive = true
        )
    }

    if (ui.showLogoutDialog) {
        ConfirmationDialog(
            title = "Logout?",
            message = "You'll need to sign in again to access your data.",
            confirmText = "Logout",
            onConfirm = viewModel::logout,
            onDismiss = viewModel::hideLogoutDialog
        )
    }

    if (ui.showDeleteAccountDialog) {
        ConfirmationDialog(
            title = "Delete Account?",
            message = "This will permanently delete all your data. This cannot be undone.",
            confirmText = "Delete Forever",
            onConfirm = viewModel::deleteAccount,
            onDismiss = viewModel::hideDeleteAccountDialog,
            isDestructive = true
        )
    }

    if (ui.showPeriodTimePicker) {
        TimePickerDialog(
            currentTime = ui.settings.notifications.periodReminderTime,
            onTimeSelected = viewModel::setPeriodReminderTime,
            onDismiss = viewModel::hidePeriodTimePicker
        )
    }

    if (ui.showOvulationTimePicker) {
        TimePickerDialog(
            currentTime = ui.settings.notifications.ovulationReminderTime,
            onTimeSelected = viewModel::setOvulationReminderTime,
            onDismiss = viewModel::hideOvulationTimePicker
        )
    }

    if (ui.showPeriodEndTimePicker) {
        TimePickerDialog(
            currentTime = ui.settings.notifications.periodEndReminderTime,
            onTimeSelected = viewModel::setPeriodEndReminderTime,
            onDismiss = viewModel::hidePeriodEndTimePicker
        )
    }

    if (ui.showMedicationTimePicker) {
        TimePickerDialog(
            currentTime = ui.settings.notifications.medicationReminderTime,
            onTimeSelected = viewModel::setMedicationReminderTime,
            onDismiss = viewModel::hideMedicationTimePicker
        )
    }

    if (ui.showDailyLogTimePicker) {
        TimePickerDialog(
            currentTime = ui.settings.notifications.dailyLogReminderTime,
            onTimeSelected = viewModel::setDailyLogReminderTime,
            onDismiss = viewModel::hideDailyLogTimePicker
        )
    }

    if (ui.showCycleLengthDialog) {
        NumberPickerDialog(
            title = "Cycle Length",
            currentValue = ui.editCycleLengthInput,
            minValue = 21,
            maxValue = 45,
            suffix = "days",
            onValueChange = viewModel::updateCycleLengthInput,
            onDismiss = viewModel::hideCycleLengthDialog,
            onSave = viewModel::saveCycleLength
        )
    }

    if (ui.showPeriodDurationDialog) {
        NumberPickerDialog(
            title = "Period Duration",
            currentValue = ui.editPeriodDurationInput,
            minValue = 2,
            maxValue = 10,
            suffix = "days",
            onValueChange = viewModel::updatePeriodDurationInput,
            onDismiss = viewModel::hidePeriodDurationDialog,
            onSave = viewModel::savePeriodDuration
        )
    }

    if (ui.showLastPeriodDatePicker) {
        DatePickerDialog(
            currentDate = ui.settings.cycleSettings.lastPeriodStartDate,
            onDateSelected = viewModel::saveLastPeriodDate,
            onDismiss = viewModel::hideLastPeriodDatePicker
        )
    }

    // ==================== MAIN UI ====================

    SettingsContent(
        ui = ui,
        currentRoute = currentRoute,
        navController = navController,
        isSystemDarkTheme = isSystemDarkTheme,
        scrollState = scrollState,
        onEditNameClick = viewModel::showEditNameDialog,
        onAvatarClick = viewModel::showPhotoOptions,
        onToggleSystemTheme = viewModel::toggleSystemTheme,
        onToggleDarkMode = viewModel::toggleDarkMode,
        onTogglePeriodReminder = viewModel::togglePeriodReminder,
        onShowPeriodTimePicker = viewModel::showPeriodTimePicker,
        onToggleOvulationReminder = viewModel::toggleOvulationReminder,
        onShowOvulationTimePicker = viewModel::showOvulationTimePicker,
        onTogglePeriodEndReminder = viewModel::togglePeriodEndReminder,
        onShowPeriodEndTimePicker = viewModel::showPeriodEndTimePicker,
        onToggleMedicationReminder = viewModel::toggleMedicationReminder,
        onShowMedicationTimePicker = viewModel::showMedicationTimePicker,
        onToggleDailyLogReminder = viewModel::toggleDailyLogReminder,
        onShowDailyLogTimePicker = viewModel::showDailyLogTimePicker,
        onShowCycleLengthDialog = viewModel::showCycleLengthDialog,
        onShowPeriodDurationDialog = viewModel::showPeriodDurationDialog,
        onShowLastPeriodDatePicker = viewModel::showLastPeriodDatePicker,
        onShowResetDataDialog = viewModel::showResetDataDialog,
        onOpenHelpSupport = viewModel::openHelpSupport,
        onOpenPrivacyPolicy = viewModel::openPrivacyPolicy,
        onOpenTermsOfService = viewModel::openTermsOfService,
        onOpenAbout = viewModel::openAbout,
        onOpenRateApp = viewModel::openRateApp,
        onShowLogoutDialog = viewModel::showLogoutDialog,
        onShowDeleteAccountDialog = viewModel::showDeleteAccountDialog
    )
}

@Composable
private fun SettingsContent(
    ui: SettingsUiState,
    currentRoute: String?,
    navController: NavController,
    isSystemDarkTheme: Boolean,
    scrollState: androidx.compose.foundation.ScrollState,
    onEditNameClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onToggleSystemTheme: (Boolean) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onTogglePeriodReminder: (Boolean) -> Unit,
    onShowPeriodTimePicker: () -> Unit,
    onToggleOvulationReminder: (Boolean) -> Unit,
    onShowOvulationTimePicker: () -> Unit,
    onTogglePeriodEndReminder: (Boolean) -> Unit,
    onShowPeriodEndTimePicker: () -> Unit,
    onToggleMedicationReminder: (Boolean) -> Unit,
    onShowMedicationTimePicker: () -> Unit,
    onToggleDailyLogReminder: (Boolean) -> Unit,
    onShowDailyLogTimePicker: () -> Unit,
    onShowCycleLengthDialog: () -> Unit,
    onShowPeriodDurationDialog: () -> Unit,
    onShowLastPeriodDatePicker: () -> Unit,
    onShowResetDataDialog: () -> Unit,
    onOpenHelpSupport: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenTermsOfService: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenRateApp: () -> Unit,
    onShowLogoutDialog: () -> Unit,
    onShowDeleteAccountDialog: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Gradient background
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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 100.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Settings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(20.dp))

                // Profile card
                ProfileCard(
                    profile = ui.settings.profile,
                    onEditClick = onEditNameClick,
                    onAvatarClick = onAvatarClick
                )

                Spacer(Modifier.height(20.dp))

                // Appearance section
                SectionCard(title = "Appearance", emoji = "🎨") {
                    ToggleSettingRow(
                        icon = Icons.Outlined.Brightness6,
                        label = "Use system theme",
                        description = "Match device settings",
                        isEnabled = ui.settings.appearance.useSystemTheme,
                        onToggle = onToggleSystemTheme
                    )

                    if (!ui.settings.appearance.useSystemTheme) {
                        Spacer(Modifier.height(14.dp))
                        ToggleSettingRow(
                            icon = Icons.Outlined.DarkMode,
                            label = "Dark mode",
                            description = "Use dark theme",
                            isEnabled = ui.settings.appearance.useDarkMode,
                            onToggle = onToggleDarkMode
                        )
                    } else {
                        Spacer(Modifier.height(12.dp))
                        SystemThemeIndicator(isSystemDarkTheme = isSystemDarkTheme)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Notifications section
                SectionCard(title = "Notifications", emoji = "🔔") {
                    NotificationRow(
                        label = "Period reminder",
                        description = "Notify ${ui.settings.notifications.periodReminderDaysBefore} days before",
                        isEnabled = ui.settings.notifications.periodReminderEnabled,
                        time = ui.settings.notifications.periodReminderTime,
                        onToggle = onTogglePeriodReminder,
                        onTimeClick = onShowPeriodTimePicker
                    )

                    Spacer(Modifier.height(14.dp))

                    NotificationRow(
                        label = "Ovulation reminder",
                        description = "Fertile window alert",
                        isEnabled = ui.settings.notifications.ovulationReminderEnabled,
                        time = ui.settings.notifications.ovulationReminderTime,
                        onToggle = onToggleOvulationReminder,
                        onTimeClick = onShowOvulationTimePicker
                    )

                    Spacer(Modifier.height(14.dp))

                    NotificationRow(
                        label = "Period end reminder",
                        description = "Day before expected end",
                        isEnabled = ui.settings.notifications.periodEndReminderEnabled,
                        time = ui.settings.notifications.periodEndReminderTime,
                        onToggle = onTogglePeriodEndReminder,
                        onTimeClick = onShowPeriodEndTimePicker
                    )

                    Spacer(Modifier.height(14.dp))

                    NotificationRow(
                        label = "Medication reminder",
                        description = "Daily pill/contraceptive",
                        isEnabled = ui.settings.notifications.medicationReminderEnabled,
                        time = ui.settings.notifications.medicationReminderTime,
                        onToggle = onToggleMedicationReminder,
                        onTimeClick = onShowMedicationTimePicker
                    )

                    Spacer(Modifier.height(14.dp))

                    NotificationRow(
                        label = "Daily log reminder",
                        description = "Evening check-in",
                        isEnabled = ui.settings.notifications.dailyLogReminderEnabled,
                        time = ui.settings.notifications.dailyLogReminderTime,
                        onToggle = onToggleDailyLogReminder,
                        onTimeClick = onShowDailyLogTimePicker
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Cycle settings section
                SectionCard(title = "Cycle Settings", emoji = "📊") {
                    NavigationRow(
                        icon = Icons.Outlined.CalendarMonth,
                        label = "Edit cycle length",
                        value = "${ui.settings.cycleSettings.cycleLength} days",
                        onClick = onShowCycleLengthDialog
                    )

                    Spacer(Modifier.height(14.dp))

                    NavigationRow(
                        icon = Icons.Outlined.WaterDrop,
                        label = "Edit period duration",
                        value = "${ui.settings.cycleSettings.periodDuration} days",
                        onClick = onShowPeriodDurationDialog
                    )

                    Spacer(Modifier.height(14.dp))

                    NavigationRow(
                        icon = Icons.Outlined.Event,
                        label = "Edit last period date",
                        value = ui.settings.cycleSettings.lastPeriodStartDate.format(
                            java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy")
                        ),
                        onClick = onShowLastPeriodDatePicker
                    )

                    Spacer(Modifier.height(14.dp))

                    NavigationRow(
                        icon = Icons.Outlined.RestartAlt,
                        label = "Reset all data",
                        value = null,
                        onClick = onShowResetDataDialog,
                        isDestructive = true
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Support & Info section
                SectionCard(title = "Support & Info", emoji = "ℹ️") {
                    NavigationRow(
                        icon = Icons.Outlined.Help,
                        label = "Help & Support",
                        value = null,
                        onClick = onOpenHelpSupport
                    )

                    Spacer(Modifier.height(14.dp))

                    NavigationRow(
                        icon = Icons.Outlined.PrivacyTip,
                        label = "Privacy Policy",
                        value = null,
                        onClick = onOpenPrivacyPolicy
                    )

                    Spacer(Modifier.height(14.dp))

                    NavigationRow(
                        icon = Icons.Outlined.Description,
                        label = "Terms of Service",
                        value = null,
                        onClick = onOpenTermsOfService
                    )

                    Spacer(Modifier.height(14.dp))

                    NavigationRow(
                        icon = Icons.Outlined.Info,
                        label = "About MoonSync",
                        value = "v1.0.0",
                        onClick = onOpenAbout
                    )

                    Spacer(Modifier.height(14.dp))

                    NavigationRow(
                        icon = Icons.Outlined.Star,
                        label = "Rate app",
                        value = null,
                        onClick = onOpenRateApp
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Account section
                SectionCard(title = "Account", emoji = "🔓") {
                    NavigationRow(
                        icon = Icons.Outlined.Logout,
                        label = "Logout",
                        value = null,
                        onClick = onShowLogoutDialog
                    )

                    Spacer(Modifier.height(14.dp))

                    NavigationRow(
                        icon = Icons.Outlined.DeleteForever,
                        label = "Delete account",
                        value = null,
                        onClick = onShowDeleteAccountDialog,
                        isDestructive = true
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
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

        // Loading overlay
        if (ui.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(enabled = false) { },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun SystemThemeIndicator(isSystemDarkTheme: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSystemDarkTheme)
                    Icons.Outlined.DarkMode
                else
                    Icons.Outlined.LightMode,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = "Currently using ${if (isSystemDarkTheme) "dark" else "light"} mode",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Following your device settings",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ==================== PROFILE CARD ====================

@Composable
private fun ProfileCard(
    profile: UserProfile,
    onEditClick: () -> Unit,
    onAvatarClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primary,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onAvatarClick)
                    .background(
                        if (profile.photoUri == null) {
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
                                )
                            )
                        } else {
                            Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (profile.photoUri != null) {
                    AsyncImage(
                        model = profile.photoUri,
                        contentDescription = "Profile photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = profile.name.firstOrNull()?.uppercase() ?: "?",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = profile.email,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                    thickness = 1.dp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Day ${profile.currentCycleDay} • ${profile.currentPhaseName} ${profile.currentPhaseEmoji}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
                Text(
                    text = "Next period in ${profile.daysUntilNextPeriod} days",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
            }

            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit profile",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

// ==================== SECTION COMPONENTS ====================

@Composable
private fun SectionCard(
    title: String,
    emoji: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun ToggleSettingRow(
    icon: ImageVector,
    label: String,
    description: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.size(22.dp)
        )

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
private fun NotificationRow(
    label: String,
    description: String,
    isEnabled: Boolean,
    time: LocalTime,
    onToggle: (Boolean) -> Unit,
    onTimeClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            )
        }

        if (isEnabled) {
            Spacer(Modifier.height(10.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onTimeClick),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = time.format(DateTimeFormatter.ofPattern("h:mm a")),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = "Change time",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NavigationRow(
    icon: ImageVector,
    label: String,
    value: String?,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive)
                    MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                else
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp)
            )

            Spacer(Modifier.width(14.dp))

            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDestructive)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            if (value != null) {
                Text(
                    text = value,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(Modifier.width(8.dp))
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ==================== DIALOG COMPONENTS ====================

@Composable
private fun EditNameDialog(
    currentName: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Edit Name",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = currentName,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Your name") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onSave,
                        enabled = currentName.trim().isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoOptionsSheet(
    hasPhoto: Boolean,
    onSelectPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
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
                text = "Profile Photo",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(16.dp))

            PhotoOptionRow(
                icon = Icons.Outlined.AddPhotoAlternate,
                label = if (hasPhoto) "Change photo" else "Add photo",
                onClick = {
                    onSelectPhoto()
                    onDismiss()
                }
            )

            if (hasPhoto) {
                Spacer(Modifier.height(12.dp))
                PhotoOptionRow(
                    icon = Icons.Outlined.Delete,
                    label = "Remove photo",
                    isDestructive = true,
                    onClick = {
                        onRemovePhoto()
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun PhotoOptionRow(
    icon: ImageVector,
    label: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = if (isDestructive)
            MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
        else
            MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(14.dp))
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDestructive)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "⚠️", fontSize = 32.sp)

                Spacer(Modifier.height(12.dp))

                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDestructive)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimePickerDialog(
    currentTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.hour,
        initialMinute = currentTime.minute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Time",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(16.dp))

                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        selectorColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onTimeSelected(
                                LocalTime.of(timePickerState.hour, timePickerState.minute)
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Set")
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberPickerDialog(
    title: String,
    currentValue: Int,
    minValue: Int,
    maxValue: Int,
    suffix: String,
    onValueChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = { onValueChange(currentValue - 1) },
                        enabled = currentValue > minValue
                    ) {
                        Icon(Icons.Default.Remove, "Decrease")
                    }

                    Text(
                        text = "$currentValue $suffix",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    IconButton(
                        onClick = { onValueChange(currentValue + 1) },
                        enabled = currentValue < maxValue
                    ) {
                        Icon(Icons.Default.Add, "Increase")
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onSave) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun DatePickerDialog(
    currentDate: java.time.LocalDate,
    onDateSelected: (java.time.LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentDate
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(date)
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    MoonSyncTheme {
        SettingsContent(
            ui = SettingsUiState(),
            currentRoute = "settings",
            navController = rememberNavController(),
            isSystemDarkTheme = false,
            scrollState = rememberScrollState(),
            onEditNameClick = {},
            onAvatarClick = {},
            onToggleSystemTheme = {},
            onToggleDarkMode = {},
            onTogglePeriodReminder = {},
            onShowPeriodTimePicker = {},
            onToggleOvulationReminder = {},
            onShowOvulationTimePicker = {},
            onTogglePeriodEndReminder = {},
            onShowPeriodEndTimePicker = {},
            onToggleMedicationReminder = {},
            onShowMedicationTimePicker = {},
            onToggleDailyLogReminder = {},
            onShowDailyLogTimePicker = {},
            onShowCycleLengthDialog = {},
            onShowPeriodDurationDialog = {},
            onShowLastPeriodDatePicker = {},
            onShowResetDataDialog = {},
            onOpenHelpSupport = {},
            onOpenPrivacyPolicy = {},
            onOpenTermsOfService = {},
            onOpenAbout = {},
            onOpenRateApp = {},
            onShowLogoutDialog = {},
            onShowDeleteAccountDialog = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsScreenDarkPreview() {
    MoonSyncTheme(darkTheme = true) {
        SettingsContent(
            ui = SettingsUiState(),
            currentRoute = "settings",
            navController = rememberNavController(),
            isSystemDarkTheme = true,
            scrollState = rememberScrollState(),
            onEditNameClick = {},
            onAvatarClick = {},
            onToggleSystemTheme = {},
            onToggleDarkMode = {},
            onTogglePeriodReminder = {},
            onShowPeriodTimePicker = {},
            onToggleOvulationReminder = {},
            onShowOvulationTimePicker = {},
            onTogglePeriodEndReminder = {},
            onShowPeriodEndTimePicker = {},
            onToggleMedicationReminder = {},
            onShowMedicationTimePicker = {},
            onToggleDailyLogReminder = {},
            onShowDailyLogTimePicker = {},
            onShowCycleLengthDialog = {},
            onShowPeriodDurationDialog = {},
            onShowLastPeriodDatePicker = {},
            onShowResetDataDialog = {},
            onOpenHelpSupport = {},
            onOpenPrivacyPolicy = {},
            onOpenTermsOfService = {},
            onOpenAbout = {},
            onOpenRateApp = {},
            onShowLogoutDialog = {},
            onShowDeleteAccountDialog = {}
        )
    }
}