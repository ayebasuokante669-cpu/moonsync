package com.example.moonsyncapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun SharedScaffold(
    navController: NavController,
    content: @Composable (PaddingValues) -> Unit
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // CONTENT LAYER (z-index = 0 by default)
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            // Main content with bottom padding for nav bar
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 120.dp) // Space for nav bar (70dp height + 16dp padding)
            ) {
                content(paddingValues)
            }
        }

        // NAVIGATION BAR LAYER (z-index = 10, floats on top)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .zIndex(10f) // 🔥 This ensures nav bar is on top
        ) {
            BottomNavigationBar(
                currentRoute = currentRoute,
                navController = navController
            )
        }
    }
}