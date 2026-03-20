package com.example.moonsyncapp.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.moonsyncapp.navigation.Routes
import com.example.moonsyncapp.ui.theme.MoonSyncTheme

private fun getDaysInMonth(month: String, year: Int): Int {
    val isLeapYear = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
    return when (month) {
        "Jan" -> 31
        "Feb" -> if (isLeapYear) 29 else 28
        "Mar" -> 31
        "Apr" -> 30
        "May" -> 31
        "Jun" -> 30
        "Jul" -> 31
        "Aug" -> 31
        "Sep" -> 30
        "Oct" -> 31
        "Nov" -> 30
        "Dec" -> 31
        else -> 31
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdateScreen(navController: NavHostController) {
    var selectedMonth by remember { mutableStateOf("Jan") }
    var selectedDay by remember { mutableStateOf(15) }
    var selectedYear by remember { mutableStateOf(2000) }

    // Calculate correct days for selected month/year
    val daysInMonth = getDaysInMonth(selectedMonth, selectedYear)

    // Adjust day if it exceeds the new month's maximum
    LaunchedEffect(daysInMonth) {
        if (selectedDay > daysInMonth) {
            selectedDay = daysInMonth
        }
    }

    Box(
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
            Spacer(modifier = Modifier.weight(0.5f))

            // Title
            Text(
                text = "When were you born?",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = "Since cycles can change over time, this assists\nus with modifying the application for you",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.weight(0.5f))

            // Date Picker Row (Horizontal Layout - Best Practice)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DatePickerColumn(
                    value = selectedMonth,
                    onValueChange = { selectedMonth = it },
                    items = listOf(
                        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
                    ),
                    modifier = Modifier.weight(1.2f)
                )

                DatePickerColumn(
                    value = selectedDay.toString(),
                    onValueChange = { selectedDay = it.toIntOrNull() ?: 1 },
                    items = (1..daysInMonth).map { it.toString() },
                    modifier = Modifier.weight(1.0f)
                )

                DatePickerColumn(
                    value = selectedYear.toString(),
                    onValueChange = { selectedYear = it.toIntOrNull() ?: 2000 },
                    items = (1950..2012).map { it.toString() }.reversed(),
                    modifier = Modifier.weight(1.3f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Next Button
            Button(
                onClick = { navController.navigate(Routes.MEDICATION) },
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
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerColumn(
    value: String,
    onValueChange: (String) -> Unit,
    items: List<String>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            textStyle = LocalTextStyle.current.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 200.dp)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    onClick = {
                        onValueChange(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun BirthdateScreenLightPreview() {
    MoonSyncTheme(darkTheme = false) {
        BirthdateScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
fun BirthdateScreenDarkPreview() {
    MoonSyncTheme(darkTheme = true) {
        BirthdateScreen(navController = rememberNavController())
    }
}