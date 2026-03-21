package com.example.moonsyncapp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moonsyncapp.util.PasswordValidationState

/**
 * Real-time password validation checklist.
 * Shows each rule with pass/fail indicator as user types.
 */
@Composable
fun PasswordValidationChecklist(
    validationState: PasswordValidationState,
    modifier: Modifier = Modifier,
    showOnlyWhenTyping: Boolean = true,
    password: String = ""
) {
    // Don't show if password is empty and showOnlyWhenTyping is true
    if (showOnlyWhenTyping && password.isEmpty()) {
        return
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header with progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Password strength",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "${validationState.passedCount}/${validationState.totalRules}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        validationState.isValid -> Color(0xFF4CAF50)
                        validationState.passedCount >= 4 -> Color(0xFFFFA726)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Progress bar
            LinearProgressIndicator(
                progress = { validationState.passedCount.toFloat() / validationState.totalRules },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = when {
                    validationState.isValid -> Color(0xFF4CAF50)
                    validationState.passedCount >= 4 -> Color(0xFFFFA726)
                    validationState.passedCount >= 2 -> Color(0xFFFF9800)
                    else -> MaterialTheme.colorScheme.error
                },
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Rule checklist
            ValidationRule(
                text = "At least 8 characters",
                passed = validationState.minLength
            )
            ValidationRule(
                text = "One uppercase letter (A-Z)",
                passed = validationState.hasUppercase
            )
            ValidationRule(
                text = "One lowercase letter (a-z)",
                passed = validationState.hasLowercase
            )
            ValidationRule(
                text = "One symbol (!@#\$%...)",
                passed = validationState.hasSymbol
            )
            ValidationRule(
                text = "No repeating characters (aa, 11)",
                passed = validationState.noRepeatingChars
            )
            ValidationRule(
                text = "No spaces",
                passed = validationState.noSpaces
            )
        }
    }
}

@Composable
private fun ValidationRule(
    text: String,
    passed: Boolean
) {
    val iconColor by animateColorAsState(
        targetValue = if (passed) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
        animationSpec = tween(200),
        label = "ruleIconColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (passed) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        },
        animationSpec = tween(200),
        label = "ruleTextColor"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = if (passed) Icons.Default.Check else Icons.Default.Close,
            contentDescription = if (passed) "Passed" else "Failed",
            tint = iconColor,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            fontSize = 12.sp,
            color = textColor,
            fontWeight = if (passed) FontWeight.Medium else FontWeight.Normal
        )
    }
}