package com.example.moonsyncapp.ui.screens.setup

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.moonsyncapp.navigation.Routes
import com.example.moonsyncapp.ui.components.SafeScreen
import com.example.moonsyncapp.ui.theme.MoonSyncTheme
import kotlinx.coroutines.launch

data class MedicationType(
    val name: String,
    val description: String,
    val isPillBased: Boolean = false
)

private val medicationTypes = listOf(
    MedicationType(
        name = "Combined\npill",
        description = "The combined pill contains the chemicals estrogen and progestin. This is the most widely recognized kind of hormonal conception prevention pill.",
        isPillBased = true
    ),
    MedicationType(
        name = "Mini\npill",
        description = "The mini pill contains only progestin. It's often recommended for those who can't take estrogen or are breastfeeding.",
        isPillBased = true
    ),
    MedicationType(
        name = "Vaginal\nring",
        description = "A flexible ring inserted into the vagina that releases hormones. It's replaced monthly and offers continuous pregnancy prevention.",
        isPillBased = false
    ),
    MedicationType(
        name = "Patch",
        description = "A contraceptive patch worn on the skin that releases hormones through your skin into your bloodstream.",
        isPillBased = false
    ),
    MedicationType(
        name = "IUD\n(hormonal)",
        description = "A small T-shaped device inserted into the uterus that releases progestin. It can prevent pregnancy for 3-7 years.",
        isPillBased = false
    ),
    MedicationType(
        name = "IUD\n(copper)",
        description = "A non-hormonal IUD that uses copper to prevent pregnancy. It can last up to 10 years.",
        isPillBased = false
    ),
    MedicationType(
        name = "Implant",
        description = "A small rod inserted under the skin of your upper arm that releases progestin. It lasts up to 3 years.",
        isPillBased = false
    ),
    MedicationType(
        name = "Injection",
        description = "A hormone shot given every 3 months to prevent pregnancy. Contains progestin only.",
        isPillBased = false
    ),
    MedicationType(
        name = "None",
        description = "I don't currently use any anti-conception medication. MoonSync will track your natural cycle.",
        isPillBased = false
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MedicationScreen(navController: NavHostController) {
    var isNavigating by remember { mutableStateOf(false) }

    // Create infinite pager - start at a high number to allow scrolling both directions
    val pageCount = Int.MAX_VALUE
    val initialPage = pageCount / 2
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { pageCount }
    )

    // Calculate the actual medication index
    val currentMedicationIndex = pagerState.currentPage % medicationTypes.size
    val currentMedication = medicationTypes[currentMedicationIndex]

    val scope = rememberCoroutineScope()

    SafeScreen(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Main Heading
            Text(
                text = "Do you use anti-\nconception medication?",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle
            Text(
                text = "We can customize the application,so it's more\nvaluable to you",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Infinite Scrollable Medication Pager
            VerticalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                pageSize = PageSize.Fixed(80.dp),
                pageSpacing = 12.dp,
                contentPadding = PaddingValues(vertical = 120.dp)
            ) { page ->
                val medicationIndex = page % medicationTypes.size
                val medication = medicationTypes[medicationIndex]
                val isCurrentPage = page == pagerState.currentPage

                MedicationCard(
                    medication = medication,
                    isSelected = isCurrentPage,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(page)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = currentMedication.description,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(horizontal = 8.dp)
                    .heightIn(min = 88.dp) // Prevent layout jumps
            )

            Spacer(modifier = Modifier.height(32.dp))

            // NEXT Button
            Button(
                onClick = {
                    if (!isNavigating) {
                        isNavigating = true
                        val selectedMedication = medicationTypes[currentMedicationIndex]
                        if (selectedMedication.isPillBased) {
                            navController.navigate(Routes.PILL_SCHEDULE)
                        } else {
                            navController.navigate(Routes.SYMPTOMS)
                        }
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

            Spacer(modifier = Modifier.height(16.dp))

            // SKIP Button
            OutlinedButton(
                onClick = {
                    if (!isNavigating) {
                        isNavigating = true
                        navController.navigate(Routes.SYMPTOMS)
                    }
                },
                enabled = !isNavigating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                border = ButtonDefaults.outlinedButtonBorder,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground
                )
            ) {
                Text(
                    text = "SKIP",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MedicationCard(
    medication: MedicationType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(80.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)  // Subtle tint
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 1.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = medication.name.replace("\n", " "),
                fontSize = if (isSelected) 20.sp else 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary  // Solid primary color
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MedicationScreenPreview() {
    MoonSyncTheme {
        MedicationScreen(navController = rememberNavController())
    }
}