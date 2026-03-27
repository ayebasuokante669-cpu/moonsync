package com.example.moonsyncapp.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "auth_preferences"
)

data class User(
    val email: String,
    val token: String = "fake_token",
    val rememberMe: Boolean = true
)
/**
 * Special exception for login lockout.
 * Contains remaining lockout time for UI countdown.
 */
class LoginLockoutException(
    message: String,
    val remainingMs: Long
) : Exception(message)

class AuthManager(private val context: Context) {

    companion object {
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val REMEMBER_ME = booleanPreferencesKey("remember_me")
        private val FAILED_ATTEMPT_COUNT = intPreferencesKey("failed_attempt_count")
        private val LOCKOUT_END_TIME = longPreferencesKey("lockout_end_time")

        const val MAX_FAILED_ATTEMPTS = 5
        const val LOCKOUT_DURATION_MS = 3 * 60 * 60 * 1000L // 3 hours in milliseconds
    }

    private val dataStore = context.authDataStore

    // ✅ FIX: Login state should persist regardless of "Remember Me"
    // "Remember Me" only controls whether email is pre-filled, not login persistence
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    suspend fun isUserLoggedIn(): Boolean {
        val prefs = dataStore.data.first()
        return prefs[IS_LOGGED_IN] ?: false
    }

    val currentUser: Flow<User?> = dataStore.data.map { preferences ->
        val isLoggedIn = preferences[IS_LOGGED_IN] ?: false

        if (isLoggedIn) {
            User(
                email = preferences[USER_EMAIL] ?: "",
                token = preferences[AUTH_TOKEN] ?: "",
                rememberMe = preferences[REMEMBER_ME] ?: false
            )
        } else {
            null
        }
    }

    // Get saved email (for auto-fill if Remember Me was checked)
    suspend fun getSavedEmail(): String? {
        val prefs = dataStore.data.first()
        val rememberMe = prefs[REMEMBER_ME] ?: false
        return if (rememberMe) prefs[USER_EMAIL] else null
    }

//    suspend fun login(
//        email: String,
//        password: String,
//        rememberMe: Boolean
//    ): Result<User> {
//        delay(1000)
//
//        if (email.isEmpty()) {
//            return Result.failure(Exception("Email is required"))
//        }
//
//        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            return Result.failure(Exception("Invalid email format"))
//        }
//
//        if (password.isEmpty()) {
//            return Result.failure(Exception("Password is required"))
//        }
//
//        if (password.length < 6) {
//            return Result.failure(Exception("Password must be at least 6 characters"))
//        }
//
//        val user = User(
//            email = email,
//            token = "fake_token_${System.currentTimeMillis()}",
//            rememberMe = rememberMe
//        )
//
//        // ✅ FIX: Always save login state (Remember Me only controls email auto-fill)
//        dataStore.edit { preferences ->
//            preferences[IS_LOGGED_IN] = true
//            preferences[USER_EMAIL] = email
//            preferences[AUTH_TOKEN] = user.token
//            preferences[REMEMBER_ME] = rememberMe
//        }
//
//        return Result.success(user)
//    }
suspend fun login(
    email: String,
    password: String,
    rememberMe: Boolean
): Result<User> {
    // Check lockout first
    if (isLockedOut()) {
        val remainingMs = getRemainingLockoutMs()
        val remainingMinutes = (remainingMs / 1000 / 60).toInt()
        val hours = remainingMinutes / 60
        val minutes = remainingMinutes % 60

        return Result.failure(
            LoginLockoutException(
                message = "Too many failed attempts. Try again in ${hours}h ${minutes}m",
                remainingMs = remainingMs
            )
        )
    }

    delay(1000)

    if (email.isEmpty()) {
        return Result.failure(Exception("Email is required"))
    }

    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        return Result.failure(Exception("Invalid email format"))
    }

    if (password.isEmpty()) {
        return Result.failure(Exception("Password is required"))
    }

    if (password.length < 6) {
        // Record failed attempt for wrong password
        val locked = recordFailedAttempt()

        return if (locked) {
            Result.failure(
                LoginLockoutException(
                    message = "Too many failed attempts. Account locked for 3 hours.",
                    remainingMs = LOCKOUT_DURATION_MS
                )
            )
        } else {
            val remaining = MAX_FAILED_ATTEMPTS - getFailedAttemptCount()
            Result.failure(Exception("Invalid credentials. $remaining attempt${if (remaining != 1) "s" else ""} remaining."))
        }
    }

    // Successful login — reset failed attempts
    resetFailedAttempts()

    val user = User(
        email = email,
        token = "fake_token_${System.currentTimeMillis()}",
        rememberMe = rememberMe
    )

    dataStore.edit { preferences ->
        preferences[IS_LOGGED_IN] = true
        preferences[USER_EMAIL] = email
        preferences[AUTH_TOKEN] = user.token
        preferences[REMEMBER_ME] = rememberMe
    }

    return Result.success(user)
}

    suspend fun register(
        email: String,
        password: String,
        confirmPassword: String
    ): Result<User> {
        delay(1000)

        if (email.isEmpty()) {
            return Result.failure(Exception("Email is required"))
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(Exception("Invalid email format"))
        }

        if (password.isEmpty()) {
            return Result.failure(Exception("Password is required"))
        }

        if (password.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters"))
        }

        if (password != confirmPassword) {
            return Result.failure(Exception("Passwords don't match"))
        }

        val user = User(
            email = email,
            token = "fake_token_${System.currentTimeMillis()}",
            rememberMe = true
        )

        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = true
            preferences[USER_EMAIL] = email
            preferences[AUTH_TOKEN] = user.token
            preferences[REMEMBER_ME] = true
        }

        return Result.success(user)
    }
    /**
     * Request password reset email.
     * Validates email exists before sending.
     *
     * Backend team: Replace with Firebase sendPasswordResetEmail()
     * or your own backend endpoint.
     */
    suspend fun requestPasswordReset(email: String): Result<Unit> {
        delay(1500) // Simulate network call

        if (email.isEmpty()) {
            return Result.failure(Exception("Email is required"))
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(Exception("Please enter a valid email address"))
        }

        // Check if email is registered
        // In real implementation: Firebase or backend checks this
        val prefs = dataStore.data.first()
        val savedEmail = prefs[USER_EMAIL]

        // For now, we check if email matches saved email
        // Backend team will replace this with actual email verification
        if (savedEmail != null && savedEmail.equals(email, ignoreCase = true)) {
            // Email is registered — "send" reset link
            return Result.success(Unit)
        }

        // If no saved email exists yet (first install),
        // still show success to prevent email enumeration attacks
        // Backend will handle actual validation
        if (savedEmail == null) {
            // In production: backend sends email if exists, silently does nothing if not
            // For now, show success to match production behavior
            return Result.success(Unit)
        }

        // Email doesn't match — in production, you'd still return success
        // to prevent email enumeration. But since this is mock:
        return Result.failure(Exception("No account found with this email address"))
    }

    suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val auth = FirebaseAuth.getInstance()
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Google sign-in failed: no user returned"))

            val user = User(
                email = firebaseUser.email ?: "",
                token = firebaseUser.uid
            )

            dataStore.edit { preferences ->
                preferences[IS_LOGGED_IN] = true
                preferences[USER_EMAIL] = user.email
                preferences[AUTH_TOKEN] = user.token
                preferences[REMEMBER_ME] = true
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception("Google sign-in failed: ${e.message}"))
        }
    }

    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = false
            preferences[AUTH_TOKEN] = ""
            // Keep email if Remember Me was checked
            val rememberMe = preferences[REMEMBER_ME] ?: false
            if (!rememberMe) {
                preferences[USER_EMAIL] = ""
            }
        }
    }

    suspend fun clearAllData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    // ==================== LOGIN LOCKOUT ====================

    /**
     * Check if the account is currently locked out
     */
    suspend fun isLockedOut(): Boolean {
        val prefs = dataStore.data.first()
        val lockoutEnd = prefs[LOCKOUT_END_TIME] ?: 0L
        return System.currentTimeMillis() < lockoutEnd
    }

    /**
     * Get lockout end time in milliseconds
     */
    suspend fun getLockoutEndTime(): Long {
        val prefs = dataStore.data.first()
        return prefs[LOCKOUT_END_TIME] ?: 0L
    }

    /**
     * Get remaining lockout time in milliseconds
     */
    suspend fun getRemainingLockoutMs(): Long {
        val lockoutEnd = getLockoutEndTime()
        val remaining = lockoutEnd - System.currentTimeMillis()
        return if (remaining > 0) remaining else 0L
    }

    /**
     * Get current failed attempt count
     */
    suspend fun getFailedAttemptCount(): Int {
        val prefs = dataStore.data.first()
        return prefs[FAILED_ATTEMPT_COUNT] ?: 0
    }

    /**
     * Record a failed login attempt
     * Returns true if lockout was triggered
     */
    private suspend fun recordFailedAttempt(): Boolean {
        val currentCount = getFailedAttemptCount() + 1

        dataStore.edit { preferences ->
            preferences[FAILED_ATTEMPT_COUNT] = currentCount

            if (currentCount >= MAX_FAILED_ATTEMPTS) {
                preferences[LOCKOUT_END_TIME] = System.currentTimeMillis() + LOCKOUT_DURATION_MS
            }
        }

        return currentCount >= MAX_FAILED_ATTEMPTS
    }

    /**
     * Reset failed attempts (called on successful login or lockout expiry)
     */
    private suspend fun resetFailedAttempts() {
        dataStore.edit { preferences ->
            preferences[FAILED_ATTEMPT_COUNT] = 0
            preferences[LOCKOUT_END_TIME] = 0L
        }
    }
}