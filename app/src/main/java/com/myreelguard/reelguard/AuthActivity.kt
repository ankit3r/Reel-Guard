package com.myreelguard.reelguard

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.myreelguard.reelguard.data.PrefsRepository
import com.myreelguard.reelguard.ui.theme.ReelGuardTheme

class AuthActivity : ComponentActivity() {

    private lateinit var repository: PrefsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = PrefsRepository(this)

        val hasPin = repository.hasPin()

        setContent {
            ReelGuardTheme {
                var showForgotPin by remember { mutableStateOf(false) }

                if (showForgotPin) {
                    ForgotPinScreen(
                        onSuccess = {
                            showForgotPin = false
                        },
                        onBack = { showForgotPin = false }
                    )
                } else if (hasPin) {
                    LoginScreen(
                        onSuccess = { navigateToMain() },
                        onForgotPin = { showForgotPin = true }
                    )
                } else {
                    SetupPinScreen(
                        onPinSet = { pin, question, answer ->
                            repository.savePin(pin)
                            repository.saveSecurityQuestion(question, answer)
                            navigateToMain()
                        }
                    )
                }
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupPinScreen(onPinSet: (String, String, String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var selectedQuestion by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val securityQuestions = listOf(
        "What is your mother's maiden name?",
        "What was the name of your first pet?",
        "What city were you born in?",
        "What is your favorite color?",
        "What was your childhood nickname?"
    )

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Set up Security",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Create a 4-digit PIN and security question",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 4) pin = it },
                label = { Text("Enter PIN") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPin,
                onValueChange = { if (it.length <= 4) confirmPin = it },
                label = { Text("Confirm PIN") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedQuestion,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Security Question") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    securityQuestions.forEach { question ->
                        DropdownMenuItem(
                            text = { Text(question) },
                            onClick = {
                                selectedQuestion = question
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = answer,
                onValueChange = { answer = it },
                label = { Text("Answer") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (error.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(error, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    when {
                        pin.length != 4 -> error = "PIN must be 4 digits"
                        pin != confirmPin -> error = "PINs don't match"
                        selectedQuestion.isEmpty() -> error = "Select a security question"
                        answer.trim().isEmpty() -> error = "Enter an answer"
                        else -> {
                            error = ""
                            onPinSet(pin, selectedQuestion, answer.trim())
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Set PIN")
            }
        }
    }
}

@Composable
fun LoginScreen(onSuccess: () -> Unit, onForgotPin: () -> Unit) {
    val repository = PrefsRepository(androidx.compose.ui.platform.LocalContext.current)
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "ReelGuard",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Enter your PIN to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 4) pin = it },
                label = { Text("PIN") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (error.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(error, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (repository.verifyPin(pin)) {
                        error = ""
                        onSuccess()
                    } else {
                        error = "Incorrect PIN"
                        pin = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Unlock")
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onForgotPin) {
                Text("Forgot PIN?")
            }
        }
    }
}

@Composable
fun ForgotPinScreen(onSuccess: () -> Unit, onBack: () -> Unit) {
    val repository = PrefsRepository(androidx.compose.ui.platform.LocalContext.current)
    val securityQuestion = repository.getSecurityQuestion()

    var answer by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(1) } // 1 = verify answer, 2 = set new PIN

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (step == 1) {
                Text(
                    "Security Question",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    securityQuestion,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    label = { Text("Your Answer") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (error.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error, color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (repository.verifySecurityAnswer(answer.trim())) {
                            error = ""
                            step = 2
                        } else {
                            error = "Incorrect answer"
                            answer = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Verify")
                }
            } else {
                Text(
                    "Set New PIN",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 4) newPin = it },
                    label = { Text("New PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 4) confirmPin = it },
                    label = { Text("Confirm PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (error.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error, color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        when {
                            newPin.length != 4 -> error = "PIN must be 4 digits"
                            newPin != confirmPin -> error = "PINs don't match"
                            else -> {
                                repository.savePin(newPin)
                                error = ""
                                onSuccess()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reset PIN")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onBack) {
                Text("Back to Login")
            }
        }
    }
}
