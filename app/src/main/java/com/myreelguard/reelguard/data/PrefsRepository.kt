package com.myreelguard.reelguard.data

import android.content.Context
import com.myreelguard.reelguard.utils.Constants
import java.security.MessageDigest
import java.time.LocalDate

class PrefsRepository(context: Context) {

    private val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    private fun today(): LocalDate = LocalDate.now()

    var reelLimit: Int
        get() = prefs.getInt(Constants.KEY_REEL_LIMIT, Constants.DEFAULT_REEL_LIMIT)
        set(value) = prefs.edit().putInt(Constants.KEY_REEL_LIMIT, value).apply()

    var dailyReelCount: Int
        get() = prefs.getInt(Constants.getUsageCountKey(today()), 0)
        set(value) {
            prefs.edit().putInt(Constants.getUsageCountKey(today()), value).apply()
        }

    fun incrementReelCount() {
        dailyReelCount = dailyReelCount + 1
    }

    fun resetTodaysUsage() {
        dailyReelCount = 0
    }

    // PIN Authentication
    fun hasPin(): Boolean {
        return prefs.contains(Constants.KEY_PIN_HASH)
    }

    fun savePin(pin: String) {
        val hash = hashPin(pin)
        prefs.edit().putString(Constants.KEY_PIN_HASH, hash).apply()
    }

    fun verifyPin(pin: String): Boolean {
        val storedHash = prefs.getString(Constants.KEY_PIN_HASH, null) ?: return false
        return hashPin(pin) == storedHash
    }

    fun clearPin() {
        prefs.edit().remove(Constants.KEY_PIN_HASH).apply()
    }

    // Security Question
    fun saveSecurityQuestion(question: String, answer: String) {
        prefs.edit()
            .putString(Constants.KEY_SECURITY_QUESTION, question)
            .putString(Constants.KEY_SECURITY_ANSWER, hashPin(answer.lowercase().trim()))
            .apply()
    }

    fun getSecurityQuestion(): String {
        return prefs.getString(Constants.KEY_SECURITY_QUESTION, "No question set") ?: "No question set"
    }

    fun verifySecurityAnswer(answer: String): Boolean {
        val storedHash = prefs.getString(Constants.KEY_SECURITY_ANSWER, null) ?: return false
        return hashPin(answer.lowercase().trim()) == storedHash
    }

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
