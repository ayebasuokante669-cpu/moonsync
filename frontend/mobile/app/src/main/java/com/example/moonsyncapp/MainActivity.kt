package com.example.moonsyncapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.moonsyncapp.data.OnboardingManager
import com.example.moonsyncapp.data.auth.AuthManager
import com.example.moonsyncapp.navigation.NavGraph
import com.example.moonsyncapp.navigation.Routes
import com.example.moonsyncapp.ui.screens.onboarding.OnboardingScreen
import com.example.moonsyncapp.ui.screens.splash.SplashScreen
import com.example.moonsyncapp.ui.theme.LocalThemeManager
import com.example.moonsyncapp.ui.theme.MoonSyncTheme
import com.example.moonsyncapp.ui.theme.ThemeManager
import com.example.moonsyncapp.widget.WidgetRefreshHelper  // NEW: Widget refresh
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf

class MainActivity : ComponentActivity() {

    private lateinit var themeManager: ThemeManager
    private lateinit var onboardingManager: OnboardingManager
    private lateinit var authManager: AuthManager

    // Widget deep link — consumed once then cleared
    private var pendingWidgetRoute: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_MoonSyncApp)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        themeManager = ThemeManager(this)
        onboardingManager = OnboardingManager(this)
        authManager = AuthManager(this)

        // =============================================
        // NEW: Schedule widget midnight refresh worker
        // Uses KEEP policy — safe to call every launch
        // =============================================
        WidgetRefreshHelper.ensureScheduled(this)

        // Extract widget route ONCE, clear from intent immediately
        pendingWidgetRoute = intent?.getStringExtra("route")
        intent?.removeExtra("route")

        setContent {
            val systemTheme = isSystemInDarkTheme()
            var darkTheme by remember { mutableStateOf(systemTheme) }

            // ✅ CRITICAL: These must be the initial states
            var showSplash by remember { mutableStateOf(true) }      // Splash shows FIRST
            var isLoading by remember { mutableStateOf(true) }       // Loading until checks complete
            var showOnboarding by remember { mutableStateOf(false) } // Onboarding hidden initially
            var startDestination by remember { mutableStateOf(Routes.LOGIN) }

            // Load app state WHILE splash is showing
            LaunchedEffect(Unit) {
                val onboardingCompleted = onboardingManager.isOnboardingCompleted.first()
                val isLoggedIn = authManager.isUserLoggedIn()

                when {
                    !onboardingCompleted -> {
                        // First time user → show onboarding after splash
                        showOnboarding = true
                    }
                    !isLoggedIn -> {
                        // Not logged in → go to login
                        startDestination = Routes.LOGIN
                    }
                    else -> {
                        // Logged in → check setup status
                        val setupCompleted = onboardingManager.isSetupCompleted.first()
                        startDestination = if (setupCompleted) Routes.HOME else Routes.WELCOME
                    }
                }

                isLoading = false  // ✅ IMPORTANT: Mark loading as done
            }

            // =============================================
            // NEW: Refresh widget data on app open
            // Syncs widget with any changes made since last open
            // Only runs if widgets are placed on home screen
            // =============================================
            LaunchedEffect(isLoading) {
                if (!isLoading) {
                    WidgetRefreshHelper.refreshNow(this@MainActivity)
                }
            }

            // Observe theme changes
            LaunchedEffect(Unit) {
                themeManager.themeFlow.onEach { prefs ->
                    darkTheme = if (prefs.useSystemTheme) {
                        systemTheme
                    } else {
                        prefs.useDarkMode
                    }
                }.launchIn(lifecycleScope)
            }

            CompositionLocalProvider(LocalThemeManager provides themeManager) {
                MoonSyncTheme(darkTheme = darkTheme) {

                    // ✅ ORDER MATTERS: Splash is checked FIRST
                    when {
                        showSplash -> {
                            // ALWAYS show splash first
                            SplashScreen {
                                showSplash = false  // Only hide splash when animation completes
                            }
                        }

                        isLoading -> {
                            // Brief loading state (if data loads slower than splash)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background)
                            )
                        }

                        showOnboarding -> {
                            // Show onboarding for first-time users
                            OnboardingScreen(
                                onNavigateToLogin = {
                                    lifecycleScope.launch {
                                        onboardingManager.completeOnboarding()
                                        showOnboarding = false
                                    }
                                }
                            )
                        }

                        else -> {
                            // Show main navigation
                            val navController = rememberNavController()

                            // Handle widget deep link — fires once, then clears
                            LaunchedEffect(Unit) {
                                pendingWidgetRoute?.let { route ->
                                    kotlinx.coroutines.delay(100)
                                    try {
                                        navController.navigate(route) {
                                            popUpTo(Routes.HOME) {
                                                inclusive = false
                                                saveState = false
                                            }
                                            launchSingleTop = true
                                            restoreState = false
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                    pendingWidgetRoute = null
                                }
                            }

                            NavGraph(
                                navController = navController,
                                onboardingManager = onboardingManager,
                                startDestination = startDestination
                            )
                        }
                    }
                }
            }
        }
    }

    // =============================================
    // NEW: Widget deep link handler
    // Routes widget tap to the correct screen
    //
    // Widget tap actions:
    // - Fresh widget → Routes.HOME
    // - Stale widget → Routes.LOGGING
    // - Log button (large widget) → Routes.LOGGING
    // =============================================
}