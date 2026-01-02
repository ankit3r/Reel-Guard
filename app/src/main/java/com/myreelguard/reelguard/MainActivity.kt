package com.myreelguard.reelguard

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.myreelguard.reelguard.data.UsageViewModel
import com.myreelguard.reelguard.services.ReelCounterService
import com.myreelguard.reelguard.services.UsageMonitorService
import com.myreelguard.reelguard.ui.theme.BluePrimary
import com.myreelguard.reelguard.ui.theme.OrangeWarning
import com.myreelguard.reelguard.ui.theme.RedLimit
import com.myreelguard.reelguard.ui.theme.ReelGuardTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: UsageViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startService(Intent(this, UsageMonitorService::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestAllPermissions()

        setContent {
            ReelGuardTheme {
                val reelCount by viewModel.reelCount.observeAsState(0)
                val reelLimit by viewModel.reelLimitLive.observeAsState(viewModel.reelLimit)

                LaunchedEffect(Unit) {
                    viewModel.loadData()
                }

                MainScreen(
                    reelCount = reelCount,
                    reelLimit = reelLimit
                )
            }
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.loadData()
    }

    private fun requestAllPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    startService(Intent(this, UsageMonitorService::class.java))
                }
                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            startService(Intent(this, UsageMonitorService::class.java))
        }

        if (!isUsageStatsPermissionGranted()) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        if (!isAccessibilityServiceEnabled()) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }

    private fun isUsageStatsPermissionGranted(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val service = "${packageName}/${ReelCounterService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices?.contains(service) == true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    reelCount: Int,
    reelLimit: Int
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ReelGuard") },
                actions = {
                    IconButton(onClick = {
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgress(
                value = reelCount.toLong(),
                limit = reelLimit.toLong(),
                label = "Reels Watched",
                formatter = { "${it.toInt()} reels" },
                modifier = Modifier.size(250.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            StatsCard(
                label = "Reels Remaining",
                value = "${(reelLimit - reelCount).coerceAtLeast(0)} reels",
                icon = Icons.Default.Info
            )

            Spacer(modifier = Modifier.height(16.dp))

            StatsCard(
                label = "Total Reels Today",
                value = "$reelCount reels",
                icon = Icons.Default.Info
            )

            Spacer(modifier = Modifier.weight(1f))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    text = "Only Reels and Shorts are tracked. Regular videos and feeds are not limited.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun CircularProgress(
    value: Long,
    limit: Long,
    label: String,
    formatter: (Long) -> String,
    modifier: Modifier = Modifier
) {
    val percentage = if (limit > 0) (value.toFloat() / limit.toFloat()).coerceIn(0f, 1f) else 0f
    val color = when {
        percentage >= 1.0f -> RedLimit
        percentage >= 0.8f -> OrangeWarning
        else -> BluePrimary
    }

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = Color.LightGray,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 24f, cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360 * percentage,
                useCenter = false,
                style = Stroke(width = 24f, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(formatter(value), fontWeight = FontWeight.Bold, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("of ${formatter(limit)}", fontSize = 18.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun StatsCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
