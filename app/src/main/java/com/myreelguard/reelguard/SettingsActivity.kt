package com.myreelguard.reelguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.myreelguard.reelguard.data.UsageViewModel
import com.myreelguard.reelguard.ui.theme.ReelGuardTheme
import com.myreelguard.reelguard.utils.Constants

class SettingsActivity : ComponentActivity() {

    private val viewModel: UsageViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ReelGuardTheme {
                var reelLimitText by remember { mutableStateOf(viewModel.reelLimit.toString()) }
                var error by remember { mutableStateOf("") }
                var showSuccessMessage by remember { mutableStateOf(false) }
                val focusManager = LocalFocusManager.current

                // Show success message temporarily
                LaunchedEffect(showSuccessMessage) {
                    if (showSuccessMessage) {
                        kotlinx.coroutines.delay(2000)
                        showSuccessMessage = false
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Settings") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                                }
                            }
                        )
                    },
                    snackbarHost = {
                        if (showSuccessMessage) {
                            Snackbar {
                                Text("Limit saved successfully!")
                            }
                        }
                    }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .padding(16.dp)
                            .fillMaxSize()
                    ) {
                        Text(
                            "Daily Reel Limit",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                            "Set maximum Reels/Shorts per day",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = reelLimitText,
                            onValueChange = {
                                // Only allow numbers
                                if (it.all { char -> char.isDigit() } && it.length <= 5) {
                                    reelLimitText = it
                                    error = ""
                                }
                            },
                            label = { Text("Number of Reels") },
                            placeholder = { Text("e.g., 50") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                }
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            isError = error.isNotEmpty(),
                            supportingText = {
                                if (error.isNotEmpty()) {
                                    Text(error, color = MaterialTheme.colorScheme.error)
                                } else {
                                    Text("Min: 10, Max: ${Constants.MAX_REEL_LIMIT}")
                                }
                            }
                        )

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val limit = reelLimitText.toIntOrNull()
                                when {
                                    limit == null -> error = "Enter a valid number"
                                    limit < 10 -> error = "Minimum is 10 reels"
                                    limit > Constants.MAX_REEL_LIMIT -> error = "Maximum is ${Constants.MAX_REEL_LIMIT} reels"
                                    else -> {
                                        viewModel.saveReelLimit(limit)
                                        viewModel.loadData() // Refresh data
                                        focusManager.clearFocus() // Clear focus
                                        error = ""
                                        showSuccessMessage = true
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Limit")
                        }

                        Spacer(Modifier.height(24.dp))

                        // Quick preset buttons
                        Text(
                            "Quick Presets",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    reelLimitText = "25"
                                    viewModel.saveReelLimit(25)
                                    viewModel.loadData()
                                    focusManager.clearFocus()
                                    showSuccessMessage = true
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("25")
                            }
                            OutlinedButton(
                                onClick = {
                                    reelLimitText = "50"
                                    viewModel.saveReelLimit(50)
                                    viewModel.loadData()
                                    focusManager.clearFocus()
                                    showSuccessMessage = true
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("50")
                            }
                            OutlinedButton(
                                onClick = {
                                    reelLimitText = "100"
                                    viewModel.saveReelLimit(100)
                                    viewModel.loadData()
                                    focusManager.clearFocus()
                                    showSuccessMessage = true
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("100")
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    reelLimitText = "200"
                                    viewModel.saveReelLimit(200)
                                    viewModel.loadData()
                                    focusManager.clearFocus()
                                    showSuccessMessage = true
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("200")
                            }
                            OutlinedButton(
                                onClick = {
                                    reelLimitText = "500"
                                    viewModel.saveReelLimit(500)
                                    viewModel.loadData()
                                    focusManager.clearFocus()
                                    showSuccessMessage = true
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("500")
                            }
                            OutlinedButton(
                                onClick = {
                                    reelLimitText = "1000"
                                    viewModel.saveReelLimit(1000)
                                    viewModel.loadData()
                                    focusManager.clearFocus()
                                    showSuccessMessage = true
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("1000")
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.resetUsage()
                                viewModel.loadData()
                                reelLimitText = viewModel.reelLimit.toString()
                                focusManager.clearFocus()
                                showSuccessMessage = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reset Today's Usage")
                        }

                        Spacer(Modifier.height(16.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text(
                                text = "Reels are counted only when you scroll through Reels or Shorts. Regular videos and browsing feeds are not counted.",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
