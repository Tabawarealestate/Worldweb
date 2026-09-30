package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

class SecurityPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("aura_security_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_APP_PIN_HASH = "app_pin_hash"
        private const val KEY_WITHDRAWAL_PIN_HASH = "withdrawal_pin_hash"
        private const val KEY_BIOMETRICS_ENABLED = "biometrics_enabled"
        private const val KEY_FAILED_PIN_ATTEMPTS = "failed_pin_attempts"
    }

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, true) // default logged in for active testing
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var userEmail: String
        get() = prefs.getString(KEY_USER_EMAIL, "trader@auraglobal.financial") ?: "trader@auraglobal.financial"
        set(value) = prefs.edit().putString(KEY_USER_EMAIL, value).apply()

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "Adam Nuuman") ?: "Adam Nuuman"
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var isBiometricsEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRICS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRICS_ENABLED, value).apply()

    var failedAttempts: Int
        get() = prefs.getInt(KEY_FAILED_PIN_ATTEMPTS, 0)
        set(value) = prefs.edit().putInt(KEY_FAILED_PIN_ATTEMPTS, value).apply()

    fun hasAppPin(): Boolean = prefs.contains(KEY_APP_PIN_HASH)

    fun hasWithdrawalPin(): Boolean = prefs.contains(KEY_WITHDRAWAL_PIN_HASH)

    fun saveAppPin(pin: String) {
        val hash = hashPin(pin)
        prefs.edit().putString(KEY_APP_PIN_HASH, hash).putInt(KEY_FAILED_PIN_ATTEMPTS, 0).apply()
    }

    fun verifyAppPin(pin: String): Boolean {
        val stored = prefs.getString(KEY_APP_PIN_HASH, null) ?: return true // If no PIN set yet, allow
        val isValid = stored == hashPin(pin)
        if (isValid) {
            failedAttempts = 0
        } else {
            failedAttempts++
        }
        return isValid
    }

    fun saveWithdrawalPin(pin: String) {
        val hash = hashPin(pin)
        prefs.edit().putString(KEY_WITHDRAWAL_PIN_HASH, hash).apply()
    }

    fun verifyWithdrawalPin(pin: String): Boolean {
        val stored = prefs.getString(KEY_WITHDRAWAL_PIN_HASH, null) ?: return true
        return stored == hashPin(pin)
    }

    fun resetAppPin() {
        prefs.edit().remove(KEY_APP_PIN_HASH).putInt(KEY_FAILED_PIN_ATTEMPTS, 0).apply()
    }

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest("AURA_SALT_$pin".toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
