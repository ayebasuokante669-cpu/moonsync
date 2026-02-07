package com.example.moonsyncapp.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Create DataStore instance
private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "auth_preferences"
)

data class User(
    val email: String,
    val token: String = "fake_token", // Will be replaced by backend
    val rememberMe: Boolean = true
)

class AuthManager(private val context: Context) {

    companion object {
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val REMEMBER_ME = booleanPreferencesKey("remember_me")
    }

    private val dataStore = context.authDataStore

    // Check if user is logged in
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        val rememberMe = preferences[REMEMBER_ME] ?: false
        val isLoggedIn = preferences[IS_LOGGED_IN] ?: false

        // Only stay logged in if "Remember me" was checked
        isLoggedIn && rememberMe
    }

    // Quick check for MainActivity
    suspend fun isUserLoggedIn(): Boolean {
        val prefs = dataStore.data.first()
        val rememberMe = prefs[REMEMBER_ME] ?: false
        val isLoggedIn = prefs[IS_LOGGED_IN] ?: false
        return isLoggedIn && rememberMe
    }

    val currentUser: Flow<User?> = dataStore.data.map { preferences ->
        val isLoggedIn = preferences[IS_LOGGED_IN] ?: false
        val rememberMe = preferences[REMEMBER_ME] ?: false

        if (isLoggedIn && rememberMe) {
            User(
                email = preferences[USER_EMAIL] ?: "",
                token = preferences[AUTH_TOKEN] ?: "",
                rememberMe = rememberMe
            )
        } else {
            null
        }
    }

    // Get saved email (for auto-fill)
    suspend fun getSavedEmail(): String? {
        return dataStore.data.first()[USER_EMAIL]
    }

    // FAKE Login - will be replaced with real API later
    suspend fun login(
        email: String,
        password: String,
        rememberMe: Boolean
    ): Result<User> {
        // Simulate network delay
        delay(1000)

        // Basic validation
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

        // FAKE SUCCESS - accept any valid email/password
        // TODO: Replace with real backend call
        /*
        val response = apiService.login(email, password)
        val user = User(email = response.email, token = response.token)
        */

        val user = User(
            email = email,
            token = "fake_token_${System.currentTimeMillis()}",
            rememberMe = rememberMe
        )

        // Save login state
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
        // Simulate network delay
        delay(1000)

        // Validation
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

        // FAKE SUCCESS - create account
        // TODO: Replace with real backend call
        /*
        val response = apiService.register(RegisterRequest(email, password))
        val user = User(email = response.email, token = response.token)
        */

        val user = User(
            email = email,
            token = "fake_token_${System.currentTimeMillis()}",
            rememberMe = true // Auto remember after signup
        )

        // Save login state (user is now logged in)
        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = true
            preferences[USER_EMAIL] = email
            preferences[AUTH_TOKEN] = user.token
            preferences[REMEMBER_ME] = true // Auto-remember on signup
        }

        return Result.success(user)
    }

    // Logout
    suspend fun logout() {
        dataStore.edit { preferences ->
            // Clear login state but keep email for convenience
            preferences[IS_LOGGED_IN] = false
            preferences[AUTH_TOKEN] = ""
            // Keep email so it appears next time they login
            // preferences[USER_EMAIL] stays
        }
    }

    // Clear all data (complete sign out)
    suspend fun clearAllData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}