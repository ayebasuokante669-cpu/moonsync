package com.example.moonsyncapp.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.moonsyncapp.navigation.Routes
import com.example.moonsyncapp.ui.theme.MoonSyncTheme

data class BottomNavItem(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String
)

private val NAV_ITEMS = listOf(
    BottomNavItem(Routes.HOME, Icons.Filled.Home, Icons.Outlined.Home, "Home"),
    BottomNavItem(Routes.CALENDAR, Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, "Calendar"),
    BottomNavItem(Routes.LOGGING, Icons.Filled.Add, Icons.Outlined.Add, "Log"),
    BottomNavItem(Routes.COMMUNITY, Icons.Filled.People, Icons.Outlined.People, "Community"),
    BottomNavItem(Routes.SETTINGS, Icons.Filled.Settings, Icons.Outlined.Settings, "Settings")
)

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val selectedIndex = NAV_ITEMS.indexOfFirst { it.route == currentRoute }.takeIf { it >= 0 } ?: 0

    BottomNavigationBarContent(
        navItems = NAV_ITEMS,
        selectedIndex = selectedIndex,
        onItemClick = { route ->
            if (currentRoute != route) {
                navController.navigate(route) {
                    popUpTo(Routes.HOME) {
                        inclusive = false
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        },
        modifier = modifier
    )
}

@Composable
private fun BottomNavigationBarContent(
    navItems: List<BottomNavItem>,
    selectedIndex: Int,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        val barWidth = minOf(maxWidth, 400.dp)
        val itemWidth = barWidth / navItems.size
        val isDark = isSystemInDarkTheme()

        Box(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            // 🌙 Ambient glow behind the bottom nav (dark-mode clarity)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                (if (isDark)
                                    MaterialTheme.colorScheme.tertiary
                                else
                                    MaterialTheme.colorScheme.primary
                                        ).copy(alpha = if (isDark) 0.20f else 0.06f),

                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(50)
                    )
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(50),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                    ),
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    navItems.forEachIndexed { index, item ->
                        if (index != selectedIndex) {
                            NavItemButton(
                                item = item,
                                isSelected = false,
                                itemWidth = itemWidth,
                                onClick = { onItemClick(item.route) }
                            )
                        } else {
                            Spacer(modifier = Modifier.width(itemWidth))
                        }
                    }
                }
            }

            navItems.getOrNull(selectedIndex)?.let { selectedItem ->
                FloatingSelectedItem(
                    item = selectedItem,
                    index = selectedIndex,
                    itemWidth = itemWidth,
                    onClick = { /* Already selected */ }
                )
            }
        }
    }
}
@Composable
private fun BoxScope.FloatingSelectedItem(
    item: BottomNavItem,
    index: Int,
    itemWidth: Dp,
    onClick: () -> Unit
) {
    val density = LocalDensity.current
    val interactionSource = remember { MutableInteractionSource() }

    // Calculate horizontal position
    val horizontalOffset = itemWidth * index + (itemWidth / 2)

    // Animate floating effect
    val offsetY by animateDpAsState(
        targetValue = (-24).dp, // How high it floats
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "floatOffset"
    )

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .offset(x = horizontalOffset - 28.dp, y = offsetY) // 28dp = half of 56dp circle
            .size(56.dp)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = item.selectedIcon,
            contentDescription = item.label,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun NavItemButton(
    item: BottomNavItem,
    isSelected: Boolean,
    itemWidth: Dp,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val iconColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 200),
        label = "iconColor"
    )

    Box(
        modifier = Modifier
            .width(itemWidth)
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = item.unselectedIcon,
            contentDescription = item.label,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════
// PREVIEWS
// ═══════════════════════════════════════════════════════════

@Preview(showBackground = true, name = "Light - Home Selected")
@Composable
private fun BottomNavBarLightHomePreview() {
    MoonSyncTheme(darkTheme = false) {
        PreviewWrapper(selectedIndex = 0)
    }
}

@Preview(showBackground = true, name = "Light - Log Selected (Center)")
@Composable
private fun BottomNavBarLightLogPreview() {
    MoonSyncTheme(darkTheme = false) {
        PreviewWrapper(selectedIndex = 2)
    }
}

@Preview(showBackground = true, name = "Dark - Calendar Selected")
@Composable
private fun BottomNavBarDarkPreview() {
    MoonSyncTheme(darkTheme = true) {
        PreviewWrapper(selectedIndex = 1)
    }
}

@Preview(showBackground = true, name = "Dark - Settings Selected")
@Composable
private fun BottomNavBarDarkSettingsPreview() {
    MoonSyncTheme(darkTheme = true) {
        PreviewWrapper(selectedIndex = 4)
    }
}

@Preview(showBackground = true, widthDp = 800, name = "Landscape - Community")
@Composable
private fun BottomNavBarLandscapePreview() {
    MoonSyncTheme(darkTheme = false) {
        PreviewWrapper(selectedIndex = 3)
    }
}

@Preview(showBackground = true, widthDp = 800, name = "Landscape Dark - Log")
@Composable
private fun BottomNavBarLandscapeDarkPreview() {
    MoonSyncTheme(darkTheme = true) {
        PreviewWrapper(selectedIndex = 2)
    }
}

@Composable
private fun PreviewWrapper(selectedIndex: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp) // Extra height for floating element
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.BottomCenter
    ) {
        BottomNavigationBarContent(
            navItems = NAV_ITEMS,
            selectedIndex = selectedIndex,
            onItemClick = {}
        )
    }
}