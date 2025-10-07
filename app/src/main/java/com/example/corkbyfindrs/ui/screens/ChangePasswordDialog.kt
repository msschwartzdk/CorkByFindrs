package com.example.corkbyfindrs.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement // Moved up
import androidx.compose.foundation.layout.Row // Moved up
import androidx.compose.material3.CircularProgressIndicator // Moved up

@Composable
fun ChangePasswordDialog(
    showDialog: Boolean,
    message: String?, // For success/error messages from ViewModel
    isChangingPassword: Boolean,
    onDismissRequest: () -> Unit,
    onChangePasswordSubmit: (current: String, new: String, confirm: String) -> Unit,
    onClearMessage: () -> Unit
) {
    if (!showDialog) return

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var passwordsMatchError by remember { mutableStateOf(false) }
    val currentPasswordFocusRequester = remember { FocusRequester() }

    LaunchedEffect(showDialog) {
        if (showDialog) {
            // Reset fields when dialog becomes visible
            currentPassword = ""
            newPassword = ""
            confirmNewPassword = ""
            passwordsMatchError = false
            onClearMessage() // Clear any previous server messages
            currentPasswordFocusRequester.requestFocus() // Request focus on the first field
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Change Password") },
        text = {
            Column {
                message?.let {
                    Text(
                        it,
                        color = if (it.startsWith("Error") || it.startsWith("Failed")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth().focusRequester(currentPasswordFocusRequester),
                    enabled = !isChangingPassword
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        passwordsMatchError = false // Clear error when typing
                    },
                    label = { Text("New Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isChangingPassword
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmNewPassword,
                    onValueChange = {
                        confirmNewPassword = it
                        passwordsMatchError = false // Clear error when typing
                    },
                    label = { Text("Confirm New Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = passwordsMatchError,
                    supportingText = { if (passwordsMatchError) Text("Passwords do not match", color = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isChangingPassword
                )
                if (isChangingPassword) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newPassword == confirmNewPassword) {
                        passwordsMatchError = false
                        onChangePasswordSubmit(currentPassword, newPassword, confirmNewPassword)
                    } else {
                        passwordsMatchError = true
                    }
                },
                enabled = !isChangingPassword
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest, enabled = !isChangingPassword) {
                Text("Cancel")
            }
        }
    )
}
