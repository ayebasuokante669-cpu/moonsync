package com.example.moonsyncapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.moonsyncapp.data.OnboardingManager

// Auth Screens
import com.example.moonsyncapp.ui.screens.auth.WelcomeScreen
import com.example.moonsyncapp.ui.screens.auth.SignUpScreen
import com.example.moonsyncapp.ui.screens.auth.LoginScreen
import com.example.moonsyncapp.ui.screens.auth.BirthdateScreen

// Setup Screens
import com.example.moonsyncapp.ui.screens.setup.MedicationScreen
import com.example.moonsyncapp.ui.screens.setup.PillScheduleScreen
import com.example.moonsyncapp.ui.screens.setup.PillDateScreen
import com.example.moonsyncapp.ui.screens.setup.SymptomsScreen
import com.example.moonsyncapp.ui.screens.setup.RegenerativeScreen
import com.example.moonsyncapp.ui.screens.setup.PeriodDateScreen
import com.example.moonsyncapp.ui.screens.setup.CycleLengthScreen
import com.example.moonsyncapp.ui.screens.setup.PeriodDurationScreen
import com.example.moonsyncapp.ui.screens.setup.SetupCompleteScreen
import com.example.moonsyncapp.ui.screens.setup.NotificationsScreen as SetupNotificationsScreen

// Main App Screens
import com.example.moonsyncapp.ui.screens.home.HomeScreen
import com.example.moonsyncapp.ui.screens.calendar.CalendarScreen
import com.example.moonsyncapp.ui.screens.logging.LoggingScreen
import com.example.moonsyncapp.ui.screens.community.CommunityScreen
import com.example.moonsyncapp.ui.screens.community.PostDetailScreen
import com.example.moonsyncapp.ui.screens.settings.SettingsScreen
import com.example.moonsyncapp.ui.screens.chat.MomoChatScreen
import com.example.moonsyncapp.ui.screens.healthtips.HealthTipsScreen
import com.example.moonsyncapp.ui.screens.healthtips.ArticleDetailScreen
import com.example.moonsyncapp.ui.screens.notifications.NotificationsScreen as AppNotificationsScreen

@Composable
fun NavGraph(
    onboardingManager: OnboardingManager,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.WELCOME  // ← CHANGED: Default to WELCOME, not ONBOARDING
) {
    NavHost(
        navController = navController,
        startDestination = startDestination  // ← Now uses parameter
    ) {

        // ❌ REMOVED: Onboarding composable - handled by MainActivity
        // composable(Routes.ONBOARDING) { ... }

        // ==========================================
        // AUTH SCREENS
        // ==========================================
        composable(Routes.WELCOME) {
            WelcomeScreen(navController = navController)
        }

        composable(Routes.SIGNUP) {
            SignUpScreen(navController = navController)
        }

        composable(Routes.LOGIN) {
            LoginScreen(navController = navController)
        }

        // ==========================================
        // SETUP FLOW
        // ==========================================
        composable(Routes.BIRTHDATE) {
            BirthdateScreen(navController = navController)
        }

        composable(Routes.MEDICATION) {
            MedicationScreen(navController = navController)
        }

        composable(Routes.PILL_SCHEDULE) {
            PillScheduleScreen(navController = navController)
        }

        composable(Routes.PILL_DATE) {
            PillDateScreen(navController = navController)
        }

        composable(Routes.SYMPTOMS) {
            SymptomsScreen(navController = navController)
        }

        composable(Routes.REGENERATIVE) {
            RegenerativeScreen(navController = navController)
        }

        composable(Routes.PERIOD_DATE) {
            PeriodDateScreen(navController = navController)
        }

        composable(Routes.CYCLE_LENGTH) {
            CycleLengthScreen(navController = navController)
        }

        composable(Routes.PERIOD_DURATION) {
            PeriodDurationScreen(navController = navController)
        }

        composable(Routes.NOTIFICATIONS_SETUP) {
            SetupNotificationsScreen(navController = navController)
        }

        composable(Routes.SETUP_COMPLETE) {
            SetupCompleteScreen(navController = navController)
        }

        // ==========================================
        // MAIN APP SCREENS
        // ==========================================
        composable(Routes.HOME) {
            HomeScreen(
                navController = navController,
                onboardingManager = onboardingManager
            )
        }

        composable(Routes.CALENDAR) {
            CalendarScreen(navController = navController)
        }

        composable(Routes.LOGGING) {
            LoggingScreen(navController = navController)
        }

        composable(Routes.COMMUNITY) {
            CommunityScreen(navController = navController)
        }

        composable(
            route = Routes.POST_DETAIL,
            arguments = listOf(
                navArgument("postId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
            PostDetailScreen(
                postId = postId,
                navController = navController
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(navController = navController)
        }

        // ==========================================
        // OVERLAY / MODAL SCREENS
        // ==========================================
        composable(Routes.NOTIFICATIONS) {
            AppNotificationsScreen(navController = navController)
        }

        composable(Routes.MOMO_CHAT) {
            MomoChatScreen(navController = navController)
        }

        composable(Routes.HEALTH_TIPS) {
            HealthTipsScreen(navController = navController)
        }

        composable(
            route = Routes.ARTICLE_DETAIL,
            arguments = listOf(
                navArgument("articleId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getString("articleId") ?: return@composable
            ArticleDetailScreen(
                articleId = articleId,
                navController = navController
            )
        }
    }
}

object Routes {
    // ❌ REMOVED: const val ONBOARDING = "onboarding"

    const val WELCOME = "welcome"
    const val SIGNUP = "signup"
    const val LOGIN = "login"

    const val BIRTHDATE = "birthdate"
    const val MEDICATION = "medication"
    const val PILL_SCHEDULE = "pill_schedule"
    const val PILL_DATE = "pill_date"
    const val SYMPTOMS = "symptoms"
    const val REGENERATIVE = "regenerative"
    const val PERIOD_DATE = "period_date"
    const val CYCLE_LENGTH = "cycle_length"
    const val PERIOD_DURATION = "period_duration"
    const val NOTIFICATIONS_SETUP = "notifications_setup"
    const val SETUP_COMPLETE = "setup_complete"

    const val HOME = "home"
    const val CALENDAR = "calendar"
    const val LOGGING = "logging"
    const val COMMUNITY = "community"
    const val SETTINGS = "settings"

    const val POST_DETAIL = "post/{postId}"
    fun postDetail(postId: String) = "post/$postId"

    const val HEALTH_TIPS = "health_tips"
    const val ARTICLE_DETAIL = "article/{articleId}"
    fun articleDetail(articleId: String) = "article/$articleId"

    const val NOTIFICATIONS = "notifications"
    const val MOMO_CHAT = "momo_chat"
}