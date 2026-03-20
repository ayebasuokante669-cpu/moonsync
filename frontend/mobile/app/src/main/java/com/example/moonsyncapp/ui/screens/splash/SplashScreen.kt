package com.example.moonsyncapp.ui.screens.splash

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    val startTime = remember { System.currentTimeMillis() }

    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("Icon.json")
    )

    val animatable = rememberLottieAnimatable()

    // Log when composition loads
    LaunchedEffect(composition) {
        if (composition != null) {
            val loadTime = System.currentTimeMillis() - startTime
            Log.d("SPLASH", "Asset loaded in: ${loadTime}ms")
            Log.d("SPLASH", "Animation duration: ${composition!!.duration}ms")

            animatable.animate(
                composition = composition,
                iterations = 1
            )

            val totalTime = System.currentTimeMillis() - startTime
            Log.d("SPLASH", "Total splash time: ${totalTime}ms")

            onFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { animatable.progress },
            modifier = Modifier.size(200.dp)
        )
    }
}