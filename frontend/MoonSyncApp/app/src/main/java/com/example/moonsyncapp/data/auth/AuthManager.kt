package com.example.moonsyncapp.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "auth_preferences"
)

data class User(
    val email: String,
    val token: String = "fake_token",
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

    suspend fun login(
        email: String,
        password: String,
        rememberMe: Boolean
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

        val user = User(
            email = email,
            token = "fake_token_${System.currentTimeMillis()}",
            rememberMe = rememberMe
        )

        // ✅ FIX: Always save login state (Remember Me only controls email auto-fill)
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
}