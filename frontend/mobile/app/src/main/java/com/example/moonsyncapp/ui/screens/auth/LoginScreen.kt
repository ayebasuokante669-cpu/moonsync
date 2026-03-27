package com.example.moonsyncapp.ui.screens.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.res.stringResource
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
import androidx.compose.ui.graphics.Color
import com.example.moonsyncapp.data.auth.LoginLockoutException


@Composable
fun LoginScreen(navController: NavHostController) {
    // Get context for AuthManager
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val coroutineScope = rememberCoroutineScope()

    // Form states
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) } // Default to true
    var passwordVisible by remember { mutableStateOf(false) }

    // UI states
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var isLockedOut by remember { mutableStateOf(false) }
    var lockoutRemainingMs by remember { mutableStateOf(0L) }
    var failedAttempts by remember { mutableStateOf(0) }

    // Pre-fill email if user logged in before
    LaunchedEffect(Unit) {
        authManager.getSavedEmail()?.let { savedEmail ->
            email = savedEmail
        }
    }

    // Check lockout status on screen load
    LaunchedEffect(Unit) {
        val locked = authManager.isLockedOut()
        if (locked) {
            isLockedOut = true
            lockoutRemainingMs = authManager.getRemainingLockoutMs()
        }
        failedAttempts = authManager.getFailedAttemptCount()
    }

// Countdown timer during lockout
    LaunchedEffect(isLockedOut) {
        if (isLockedOut) {
            while (lockoutRemainingMs > 0) {
                kotlinx.coroutines.delay(1000L)
                lockoutRemainingMs -= 1000L

                if (lockoutRemainingMs <= 0) {
                    isLockedOut = false
                    lockoutRemainingMs = 0L
                    failedAttempts = 0
                    errorMessage = null
                }
            }
        }
    }

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
            text = "Welcome Back",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Log in to continue tracking your cycle",
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
                errorMessage = null // Clear error when typing
            },
            label = { Text("Password") },
            placeholder = { Text("Enter your password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = !isLoading && !isLockedOut,
            isError = errorMessage != null,
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
        // Lockout banner with countdown
        if (isLockedOut) {
            Spacer(modifier = Modifier.height(12.dp))

            val hours = (lockoutRemainingMs / 1000 / 60 / 60).toInt()
            val minutes = ((lockoutRemainingMs / 1000 / 60) % 60).toInt()
            val seconds = ((lockoutRemainingMs / 1000) % 60).toInt()
            val countdownText = String.format("%d:%02d:%02d", hours, minutes, seconds)

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🔒",
                        fontSize = 32.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Account Temporarily Locked",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Too many failed login attempts",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Countdown timer
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⏱️",
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Try again in $countdownText",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "You can still reset your password below",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

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

        Spacer(modifier = Modifier.height(12.dp))

        // Remember Me & Forgot Password
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    enabled = !isLoading,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = "Remember me",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            TextButton(
                onClick = {
                    navController.navigate(Routes.FORGOT_PASSWORD)
                },
                enabled = !isLoading
            ) {
                Text(
                    text = if (isLockedOut) "Reset Password →" else "Forgot Password?",
                    color = if (isLockedOut) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    fontSize = 14.sp,
                    fontWeight = if (isLockedOut) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Log In Button - UPDATED WITH AUTH LOGIC
        Button(
//            onClick = {
//                coroutineScope.launch {
//                    isLoading = true
//                    errorMessage = null
//
//                    // Call AuthManager to login
//                    authManager.login(email, password, rememberMe)
//                        .onSuccess { user ->
//                            // Navigate to home on success
//                            navController.navigate(Routes.HOME) {
//                                popUpTo(Routes.LOGIN) { inclusive = true }
//                            }
//                        }
//                        .onFailure { exception ->
//                            // Show error message
//                            errorMessage = exception.message
//                        }
//
//                    isLoading = false
//                }
//            },
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    errorMessage = null

                    authManager.login(email, password, rememberMe)
                        .onSuccess { user ->
                            isLockedOut = false
                            failedAttempts = 0
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        }
                        .onFailure { exception ->
                            when (exception) {
                                is LoginLockoutException -> {
                                    isLockedOut = true
                                    lockoutRemainingMs = exception.remainingMs
                                    errorMessage = exception.message
                                }
                                else -> {
                                    errorMessage = exception.message
                                    // Update failed attempt count
                                    failedAttempts = authManager.getFailedAttemptCount()
                                }
                            }
                        }

                    isLoading = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = email.isNotEmpty() && password.isNotEmpty() && !isLoading && !isLockedOut,
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
                    text = "LOG IN",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // OR Divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = " Or log in with ",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Social Login Buttons (Side by Side)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Google
            OutlinedButton(
                onClick = {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(webClientId)
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                },
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
//                onClick = { /* TODO: Facebook Login */ },
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
                    tint = Color.Unspecified
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

        Spacer(modifier = Modifier.height(32.dp))

        // Don't have account
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account? ",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(
                onClick = {
                    navController.navigate(Routes.SIGNUP) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                enabled = !isLoading,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Sign Up",
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
fun LoginScreenPreview() {
    MoonSyncTheme {
        LoginScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
fun LoginScreenDarkPreview() {
    MoonSyncTheme(darkTheme = true) {
        LoginScreen(navController = rememberNavController())
    }
}