package com.example.moonsyncapp.ui.screens.setup

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.moonsyncapp.navigation.Routes
import com.example.moonsyncapp.ui.theme.MoonSyncTheme
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moonsyncapp.data.settings.SettingsManager
import com.example.moonsyncapp.data.OnboardingManager

@Composable
fun SetupCompleteScreen(navController: NavHostController) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val onboardingManager = remember { OnboardingManager(context) }

    val viewModel: SetupViewModel = viewModel(
        factory = SetupViewModelFactory(settingsManager, onboardingManager, context)
    )

    var showContent by remember { mutableStateOf(false) }
    var isNavigating by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Trigger animation
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    // Animation values
    val scale by animateFloatAsState(
        targetValue = if (showContent) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = tween(500),
        label = "alpha"
    )

    // Continuous pulse animation for emoji
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Confetti-like rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp)
                .graphicsLayer {
                    this.alpha = alpha
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Animated Celebration Emoji
            Text(
                text = "🎉",
                fontSize = 80.sp,
                modifier = Modifier
                    .scale(scale * pulse)
                    .graphicsLayer {
                        rotationZ = rotation
                    }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = "You're All Set!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.scale(scale)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle
            Text(
                text = "MoonSync is ready to help you\nunderstand your body better",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Feature Cards
            FeatureCard(
                emoji = "📅",
                title = "Track your cycle",
                description = "Log periods, symptoms & moods"
            )

            Spacer(modifier = Modifier.height(12.dp))

            FeatureCard(
                emoji = "🔮",
                title = "Get predictions",
                description = "Know when your next period is coming"
            )

            Spacer(modifier = Modifier.height(12.dp))

            FeatureCard(
                emoji = "💜",
                title = "Stay informed",
                description = "Receive personalized health insights"
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Go to Dashboard Button
            Button(
                onClick = {
                    if (!isNavigating) {
                        isNavigating = true

                        // ✅ CRITICAL: Mark setup complete + refresh widget
                        viewModel.completeSetup()

                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.WELCOME) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                enabled = !isNavigating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .scale(scale),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "GO TO DASHBOARD",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FeatureCard(
    emoji: String,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SetupCompleteScreenPreview() {
    MoonSyncTheme {
        SetupCompleteScreen(navController = rememberNavController())
    }
}