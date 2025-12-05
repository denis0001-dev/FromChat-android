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
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material3.Button
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
import ru.fromchat.api.LoginRequest
import ru.fromchat.api.apiRequest
import ru.fromchat.ui.RowHeader
import com.pr0gramm3r101.utils.crypto.deriveAuthSecret

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { innerPadding ->
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var alert by remember { mutableStateOf<String?>(null) }

        val scope = rememberCoroutineScope()

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
                    icon = Icons.AutoMirrored.Filled.Login,
                    title = stringResource(R.string.welcome),
                    subtitle = stringResource(R.string.login_d)
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

                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val alert_error_filling = stringResource(R.string.fill_all_fields)

                    Button(
                        onClick = {
                            if (username.isBlank() || password.isBlank()) {
                                alert = alert_error_filling
                                return@Button
                            }

                            // Derive auth secret before sending (matches frontend implementation)
                            scope.launch {
                                val derived = deriveAuthSecret(username.trim(), password.trim())
                                
                                apiRequest(
                                    onError = { message, _ ->
                                        alert = message
                                    },
                                    onSuccess = { onLoginSuccess() }
                                ) {
                                    ApiClient.login(LoginRequest(username.trim(), derived))
                                }
                            }
                        }
                    ) {
                        Text(stringResource(R.string.login))
                    }

                    Button(onClick = onNavigateToRegister) {
                        Text(stringResource(R.string.register_button))
                    }
                }
            }
        }
    }
}