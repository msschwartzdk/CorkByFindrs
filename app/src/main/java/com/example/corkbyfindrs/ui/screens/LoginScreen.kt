package com.example.corkbyfindrs.ui.screens


import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.corkbyfindrs.ui.theme.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val status by viewModel.permissionStatus
    val showLoginDialog = viewModel.showLoginDialog

    LaunchedEffect(Unit) {
        viewModel.attemptAutoLoginOrShowManualLoginInternal()
    }

    // 🔔 React to "Login successful!" and show Toast
    LaunchedEffect(status) {
        if (status.contains("successful", ignoreCase = true)) {
            Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
        }
        onLoginSuccess()
    }

    if (showLoginDialog) {
        ManualLoginDialog(
            prefetchedEmail = viewModel.prefetchedEmail,
            prefetchedPassword = viewModel.prefetchedPassword,
            onLogin = { email, password ->
                viewModel.manualLogin(email, password, onLoginSuccess)
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = status)
    }
}

@Composable
fun ManualLoginDialog(
    prefetchedEmail: String,
    prefetchedPassword: String,
    onLogin: (String, String) -> Unit
) {
    var email by remember { mutableStateOf(prefetchedEmail) }
    var password by remember { mutableStateOf(prefetchedPassword) }

    AlertDialog(
        onDismissRequest = { /* keep open until successful login */ },
        title = { Text("Manual Login") },
        text = {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onLogin(email, password) }) {
                Text("Login")
            }
        },
        dismissButton = {}
    )
}

