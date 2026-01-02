package com.myreelguard.reelguard.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object Constants {
    const val PREFS_NAME = "reelguard_prefs"

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun getUsageCountKey(date: LocalDate): String = "usage_count_${date.format(dateFormatter)}"

    const val KEY_REEL_LIMIT = "reel_limit"
    const val KEY_PIN_HASH = "pin_hash"
    const val KEY_SECURITY_QUESTION = "security_question"
    const val KEY_SECURITY_ANSWER = "security_answer"

    const val DEFAULT_REEL_LIMIT = 50
    const val MAX_REEL_LIMIT = 10000

    val MONITORED_APPS = setOf(
        "com.instagram.android",
        "com.google.android.youtube",
        "com.zhiliaoapp.musically"
    )

    const val NOTIFICATION_CHANNEL_ID = "ReelGuardService"
    const val NOTIFICATION_ID = 1
    const val ACTION_START_SERVICE = "ACTION_START_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
}
