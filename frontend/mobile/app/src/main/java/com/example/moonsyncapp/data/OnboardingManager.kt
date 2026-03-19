package com.example.moonsyncapp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.onboardingDataStore by preferencesDataStore(name = "onboarding_prefs")

class OnboardingManager(private val context: Context) {

    companion object {
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val SETUP_COMPLETED = booleanPreferencesKey("setup_completed")
        private val HOME_INTRO_SHOWN = booleanPreferencesKey("home_intro_shown")
        private val LOGGING_INTRO_SHOWN = booleanPreferencesKey("logging_intro_shown")
    }

    val isSetupCompleted: Flow<Boolean> = context.onboardingDataStore.data
        .map { preferences ->
            preferences[SETUP_COMPLETED] ?: false
        }

    suspend fun completeSetup() {
        context.onboardingDataStore.edit { preferences ->
            preferences[SETUP_COMPLETED] = true
        }
    }

    val isOnboardingCompleted: Flow<Boolean> = context.onboardingDataStore.data
        .map { preferences ->
            val value = preferences[ONBOARDING_COMPLETED] ?: false
            println("💾 OnboardingManager - Reading from DataStore: $value")
            value
        }

    val isHomeIntroShown: Flow<Boolean> = context.onboardingDataStore.data
        .map { preferences ->
            preferences[HOME_INTRO_SHOWN] ?: false
        }

    val isLoggingIntroShown: Flow<Boolean> = context.onboardingDataStore.data
        .map { preferences ->
            preferences[LOGGING_INTRO_SHOWN] ?: false
        }

    suspend fun completeOnboarding() {
        println("💾 OnboardingManager - Writing to DataStore...")
        context.onboardingDataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = true
            println("💾 OnboardingManager - Written ONBOARDING_COMPLETED = true")
        }
        println("💾 OnboardingManager - DataStore write complete")

        // Immediately verify it was written
        val verification = context.onboardingDataStore.data.first()
        println("💾 OnboardingManager - VERIFICATION READ: ${verification[ONBOARDING_COMPLETED]}")
    }

    suspend fun markHomeIntroShown() {
        context.onboardingDataStore.edit { preferences ->
            preferences[HOME_INTRO_SHOWN] = true
        }
    }

    suspend fun markLoggingIntroShown() {
        context.onboardingDataStore.edit { preferences ->
            preferences[LOGGING_INTRO_SHOWN] = true
        }
    }

    // For testing/debugging - reset all intro states
    suspend fun resetAllIntros() {
        context.onboardingDataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = false
            preferences[HOME_INTRO_SHOWN] = false
            preferences[LOGGING_INTRO_SHOWN] = false
        }
    }
}