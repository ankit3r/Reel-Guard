package com.myreelguard.reelguard.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.myreelguard.reelguard.BlockActivity
import com.myreelguard.reelguard.data.PrefsRepository
import com.myreelguard.reelguard.utils.Constants

class ReelCounterService : AccessibilityService() {

    private lateinit var repository: PrefsRepository
    private var lastScrollTime = 0L
    private var isInReelsSection = false

    override fun onCreate() {
        super.onCreate()
        repository = PrefsRepository(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: return

        if (packageName !in Constants.MONITORED_APPS) {
            isInReelsSection = false
            return
        }

        val rootNode = rootInActiveWindow
        if (rootNode != null) {
            isInReelsSection = detectReelsSection(packageName, rootNode)
            rootNode.recycle()
        }

        if (isInReelsSection && event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastScrollTime > 1000) {
                lastScrollTime = currentTime
                repository.incrementReelCount()
                checkLimits()
            }
        }
    }

    private fun detectReelsSection(packageName: String, rootNode: AccessibilityNodeInfo): Boolean {
        return when (packageName) {
            "com.instagram.android" -> isInstagramReels(rootNode)
            "com.google.android.youtube" -> isYouTubeShorts(rootNode)
            "com.zhiliaoapp.musically" -> true
            else -> false
        }
    }

    private fun isInstagramReels(rootNode: AccessibilityNodeInfo): Boolean {
        val indicators = listOf(
            "clips_viewer_clips_tab",
            "reel_viewer",
            "ClipsViewerFragment",
            "clips_tab",
            "reels_tab",
            "reel_feed",
            "clips_",
            "reel_",
        )

        return searchForText(rootNode, indicators)
    }

    private fun isYouTubeShorts(rootNode: AccessibilityNodeInfo): Boolean {
        val indicators = listOf(
            "shorts",
            "reel_",
            "shorty",
            "short_player",
            "shorts_player",
            "reel_player",
            "/shorts/",
            "reel_watch_fragment",
            "reel_recycler"
        )

        return searchForText(rootNode, indicators)
    }

    private fun searchForText(node: AccessibilityNodeInfo?, keywords: List<String>): Boolean {
        if (node == null) return false

        try {
            val viewId = node.viewIdResourceName?.lowercase() ?: ""
            if (keywords.any { viewId.contains(it, ignoreCase = true) }) {
                return true
            }

            val className = node.className?.toString()?.lowercase() ?: ""
            if (keywords.any { className.contains(it, ignoreCase = true) }) {
                return true
            }

            val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""
            if (keywords.any { contentDesc.contains(it, ignoreCase = true) }) {
                return true
            }

            val text = node.text?.toString()?.lowercase() ?: ""
            if (keywords.any { text.contains(it, ignoreCase = true) }) {
                return true
            }

            if (node.childCount > 0 && node.childCount < 50) {
                for (i in 0 until node.childCount) {
                    val child = node.getChild(i)
                    if (child != null) {
                        if (searchForText(child, keywords)) {
                            child.recycle()
                            return true
                        }
                        child.recycle()
                    }
                }
            }
        } catch (e: Exception) {
            // Handle exceptions silently
        }

        return false
    }

    private fun checkLimits() {
        if (repository.dailyReelCount >= repository.reelLimit) {
            launchBlockActivity()
        }
    }

    private fun launchBlockActivity() {
        val intent = Intent(this, BlockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(BlockActivity.EXTRA_LIMIT_TYPE, BlockActivity.TYPE_REEL)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
    }
}
