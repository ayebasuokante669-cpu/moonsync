package com.example.moonsyncapp.util

/**
 * Password validation rules per product requirements:
 * - Minimum 8 characters
 * - At least one uppercase letter
 * - At least one lowercase letter
 * - At least one symbol
 * - No repeating characters (aa, 11, !!)
 * - No spaces
 */
data class PasswordValidationState(
    val minLength: Boolean = false,
    val hasUppercase: Boolean = false,
    val hasLowercase: Boolean = false,
    val hasSymbol: Boolean = false,
    val noRepeatingChars: Boolean = false,
    val noSpaces: Boolean = false
) {
    val isValid: Boolean
        get() = minLength && hasUppercase && hasLowercase &&
                hasSymbol && noRepeatingChars && noSpaces

    val passedCount: Int
        get() = listOf(
            minLength, hasUppercase, hasLowercase,
            hasSymbol, noRepeatingChars, noSpaces
        ).count { it }

    val totalRules: Int = 6
}

object PasswordValidator {

    private const val MIN_LENGTH = 8
    private val SYMBOL_REGEX = Regex("[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?`~]")
    private val REPEATING_CHAR_REGEX = Regex("(.)\\1")

    /**
     * Validates password and returns state for each rule.
     * Called on every keystroke for real-time feedback.
     */
    fun validate(password: String): PasswordValidationState {
        return PasswordValidationState(
            minLength = password.length >= MIN_LENGTH,
            hasUppercase = password.any { it.isUpperCase() },
            hasLowercase = password.any { it.isLowerCase() },
            hasSymbol = SYMBOL_REGEX.containsMatchIn(password),
            noRepeatingChars = !REPEATING_CHAR_REGEX.containsMatchIn(password),
            noSpaces = !password.contains(' ')
        )
    }

    /**
     * Returns a user-friendly error message for invalid passwords.
     * Used when form is submitted with invalid password.
     */
    fun getErrorMessage(state: PasswordValidationState): String? {
        if (state.isValid) return null

        val errors = mutableListOf<String>()
        if (!state.minLength) errors.add("at least 8 characters")
        if (!state.hasUppercase) errors.add("an uppercase letter")
        if (!state.hasLowercase) errors.add("a lowercase letter")
        if (!state.hasSymbol) errors.add("a symbol")
        if (!state.noRepeatingChars) errors.add("no repeating characters")
        if (!state.noSpaces) errors.add("no spaces")

        return "Password must have: ${errors.joinToString(", ")}"
    }
}