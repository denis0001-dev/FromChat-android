package ru.fromchat.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.fromchat.R
import ru.fromchat.api.ApiClient
import ru.fromchat.api.RegisterRequest
import ru.fromchat.api.apiRequest
import ru.fromchat.ui.LocalNavController
import ru.fromchat.ui.RowHeader

@Composable
fun RegisterScreen(
    onRegistered: () -> Unit
) {
    Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { innerPadding ->
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var alert by remember { mutableStateOf<String?>(null) }

        val scope = rememberCoroutineScope()
        val navController = LocalNavController.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RowHeader(
                    icon = Icons.Filled.PersonAdd,
                    title = stringResource(R.string.register),
                    subtitle = stringResource(R.string.register_d)
                )

                if (alert != null) {
                    Text(text = alert!!, color = MaterialTheme.colorScheme.error)
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(stringResource(R.string.username)) },
                    singleLine = true
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.password)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(stringResource(R.string.confirm_password)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val alert_error_filling = stringResource(R.string.fill_all_fields)
                    val alert_error_password_little = stringResource(R.string.password_length_error)
                    val alert_error_name_little = stringResource(R.string.username_length_error)
                    val alert_error_password_confrim = stringResource(R.string.passwords_dont_match)

                    IconButton(
                        onClick = { navController.navigateUp() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }

                    Button(
                        onClick = {
                            // Checks
                            if (username.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                                alert = alert_error_filling
                                return@Button
                            }
                            if (password != confirmPassword) {
                                alert = alert_error_password_confrim
                                return@Button
                            }
                            if (username.length !in 3..20) {
                                alert = alert_error_name_little
                                return@Button
                            }
                            if (password.length !in 5..50) {
                                alert = alert_error_password_little
                                return@Button
                            }

                            // Send the register request
                            scope.launch {
                                apiRequest(
                                    onError = { message, _ ->
                                        alert = message
                                    },
                                    onSuccess = { onRegistered() }
                                ) {
                                    ApiClient.register(
                                        RegisterRequest(
                                            username,
                                            password,
                                            confirmPassword
                                        )
                                    )
                                }
                            }
                        }
                    ) {
                        Text(stringResource(R.string.register_button))
                    }
                }
            }
        }
    }
}