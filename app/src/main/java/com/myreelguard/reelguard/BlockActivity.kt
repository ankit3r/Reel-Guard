package com.myreelguard.reelguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class BlockActivity : ComponentActivity() {

    companion object {
        const val EXTRA_LIMIT_TYPE = "limit_type"
        const val TYPE_TIME = "time"
        const val TYPE_REEL = "reel"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val limitType = intent.getStringExtra(EXTRA_LIMIT_TYPE) ?: TYPE_TIME

        // Handle back press using OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Prevent user from escaping the block screen
                moveTaskToBack(true)
            }
        })

        setContent {
            BlockScreen(limitType)
        }
    }
}

@Composable
fun BlockScreen(limitType: String) {
    val title = if (limitType == BlockActivity.TYPE_TIME) {
        "Daily Time Limit Reached"
    } else {
        "Daily Reel Limit Reached"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 28.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Come back tomorrow!",
                color = Color.LightGray,
                fontSize = 20.sp
            )
        }
    }
}
