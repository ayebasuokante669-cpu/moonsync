package com.example.moonsyncapp.ui.screens.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.moonsyncapp.navigation.Routes
import com.example.moonsyncapp.ui.components.SafeScreen
import com.example.moonsyncapp.ui.theme.MoonSyncTheme

private val symptomOptions = listOf(
    "Painful Feminine spasms",
    "PMS symptoms",
    "Uncommon release",
    "Weighty feminine stream",
    "State of mind swings",
    "Other",
    "No, nothing bothers me"
)

@Composable
fun SymptomsScreen(navController: NavHostController) {
    val scrollState = rememberScrollState()
    var selectedSymptoms by remember { mutableStateOf(setOf<Int>()) }
    var isNavigating by remember { mutableStateOf(false) }

    SafeScreen(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        // Back Button
        IconButton(
            onClick = {
                if (!isNavigating) {
                    isNavigating = true
                    navController.navigateUp()
                }
            },
            enabled = !isNavigating,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .zIndex(1f)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Go back",
                tint = if (isNavigating)
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                else
                    MaterialTheme.colorScheme.onBackground
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Main Heading
            Text(
                text = "Any uneasiness because of any of\nthe accompanying?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle
            Text(
                text = "This assists us with understanding your\npersonal designs better",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Symptom Options (Multi-select)
            symptomOptions.forEachIndexed { index, symptom ->
                val isSelected = selectedSymptoms.contains(index)
                val isNoneOption = index == symptomOptions.lastIndex

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable {
                            selectedSymptoms = if (isSelected) {
                                // Deselect
                                selectedSymptoms - index
                            } else {
                                // Select
                                if (isNoneOption) {
                                    // "No, nothing bothers me" clears all others
                                    setOf(index)
                                } else {
                                    // Remove "none" option if selecting a symptom
                                    (selectedSymptoms - symptomOptions.lastIndex) + index
                                }
                            }
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isSelected) 4.dp else 1.dp
                    ),
                    shape = RoundedCornerShape(28.dp),
                    border = if (!isSelected) {
                        CardDefaults.outlinedCardBorder()
                    } else null
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = symptom,
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Selection helper text
            if (selectedSymptoms.isNotEmpty()) {
                Text(
                    text = "${selectedSymptoms.size} symptom${if (selectedSymptoms.size != 1) "s" else ""} selected",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // NEXT Button
            Button(
                onClick = {
                    if (!isNavigating) {
                        isNavigating = true
                        navController.navigate(Routes.REGENERATIVE)
                    }
                },
                enabled = !isNavigating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "NEXT",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SymptomsScreenPreview() {
    MoonSyncTheme {
        SymptomsScreen(navController = rememberNavController())
    }
}