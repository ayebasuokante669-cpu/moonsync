package com.example.moonsyncapp.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.moonsyncapp.R
import com.example.moonsyncapp.data.auth.AuthManager
import com.example.moonsyncapp.navigation.Routes
import com.example.moonsyncapp.ui.theme.MoonSyncTheme
import kotlinx.coroutines.launch
import com.example.moonsyncapp.util.PasswordValidator
import com.example.moonsyncapp.util.PasswordValidationState
import com.example.moonsyncapp.ui.components.PasswordValidationChecklist
import androidx.compose.ui.graphics.Color

@Composable
fun SignUpScreen(navController: NavHostController) {
    // Get context for AuthManager
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val coroutineScope = rememberCoroutineScope()

    // Form states
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val passwordValidationState = remember(password) { PasswordValidator.validate(password) }
    var acceptedTerms by remember { mutableStateOf(false) }

    // UI states
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        // Logo
        Text(
            text = "MoonSync",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Cursive,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Header
        Text(
            text = "Create Account",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sign up to start tracking your cycle",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null // Clear error when typing
            },
            label = { Text("Email") },
            placeholder = { Text("name@gmail.com") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = !isLoading,
            isError = errorMessage != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text("Password") },
            placeholder = { Text("Create a password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = !isLoading,
            visualTransformation = if (passwordVisible)
                VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(
                    onClick = { passwordVisible = !passwordVisible },
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = if (passwordVisible)
                            Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible)
                            "Hide password" else "Show password"
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
//     add checklist between password and confirm password:
        Spacer(modifier = Modifier.height(8.dp))

        // Password validation checklist
        PasswordValidationChecklist(
            validationState = passwordValidationState,
            password = password
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password Field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                errorMessage = null
            },
            label = { Text("Confirm Password") },
            placeholder = { Text("Confirm your password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = !isLoading,
            visualTransformation = if (confirmPasswordVisible)
                VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(
                    onClick = { confirmPasswordVisible = !confirmPasswordVisible },
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = if (confirmPasswordVisible)
                            Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (confirmPasswordVisible)
                            "Hide password" else "Show password"
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            isError = confirmPassword.isNotEmpty() && password != confirmPassword,
            supportingText = {
                if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                    Text(
                        text = "Passwords don't match",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        // Error message display
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Terms and Conditions
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = acceptedTerms,
                onCheckedChange = { acceptedTerms = it },
                enabled = !isLoading,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                text = "I agree to the ",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(
                onClick = { /* TODO: Show Terms */ },
                enabled = !isLoading,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Terms & Conditions",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign Up Button - UPDATED WITH AUTH LOGIC
        Button(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    errorMessage = null

                    // Call AuthManager to register
                    authManager.register(
                        email = email,
                        password = password,
                        confirmPassword = confirmPassword
                    ).onSuccess { user ->
                        // Account created successfully
                        // Navigate to birthdate (continue onboarding)
                        navController.navigate(Routes.BIRTHDATE) {
                            popUpTo(Routes.SIGNUP) { inclusive = true }
                        }
                    }.onFailure { exception ->
                        // Show error message
                        errorMessage = exception.message
                    }

                    isLoading = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = email.isNotEmpty() &&
                    password.isNotEmpty() &&
                    confirmPassword.isNotEmpty() &&
                    password == confirmPassword &&
                    passwordValidationState.isValid &&
                    acceptedTerms &&
                    !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = "SIGN UP",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Rest of your UI remains the same...

        Spacer(modifier = Modifier.height(24.dp))

        // OR Divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = " Or sign up with ",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Social Login Buttons remain the same...
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Google
            OutlinedButton(
                onClick = { /* TODO: Google Sign Up */ },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = SolidColor(
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google",
                    modifier = Modifier.size(20.dp),
                    tint = androidx.compose.ui.graphics.Color.Unspecified
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Google", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }

            // Facebook
//            OutlinedButton(
//                onClick = { /* TODO: Facebook Sign Up */ },
//                modifier = Modifier
//                    .weight(1f)
//                    .height(52.dp),
//                enabled = !isLoading,
//                shape = RoundedCornerShape(16.dp),
//                border = ButtonDefaults.outlinedButtonBorder.copy(
//                    brush = SolidColor(
//                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
//                    )
//                ),
//                colors = ButtonDefaults.outlinedButtonColors(
//                    contentColor = MaterialTheme.colorScheme.onBackground
//                )
//            ) {
//                Icon(
//                    painter = painterResource(id = R.drawable.ic_facebook),
//                    contentDescription = "Facebook",
//                    modifier = Modifier.size(20.dp),
//                    tint = androidx.compose.ui.graphics.Color.Unspecified
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Text(text = "Facebook", fontSize = 14.sp, fontWeight = FontWeight.Medium)
//            }
//          Instagram (Coming Soon - requires backend OAuth)
            OutlinedButton(
                onClick = {
                    // Instagram OAuth requires backend implementation
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                enabled = false,
                shape = RoundedCornerShape(16.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = SolidColor(
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_instagram),
                    contentDescription = "Instagram",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Unspecified // Keep original Instagram colors
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Instagram",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Coming Soon",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Already have account
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account? ",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(
                onClick = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SIGNUP) { inclusive = true }
                    }
                },
                enabled = !isLoading,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Log In",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun SignUpScreenPreview() {
    MoonSyncTheme {
        SignUpScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
fun SignUpScreenDarkPreview() {
    MoonSyncTheme(darkTheme = true) {
        SignUpScreen(navController = rememberNavController())
    }
}