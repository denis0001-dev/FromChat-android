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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.pr0gramm3r101.utils.crypto.deriveAuthSecret
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ru.fromchat.Res
import ru.fromchat.api.ApiClient
import ru.fromchat.api.LoginRequest
import ru.fromchat.api.apiRequest
import ru.fromchat.error_unexpected
import ru.fromchat.fill_all_fields
import ru.fromchat.login
import ru.fromchat.login_d
import ru.fromchat.password
import ru.fromchat.register_button
import ru.fromchat.ui.RowHeader
import ru.fromchat.username
import ru.fromchat.welcome

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val errorUnexpected = stringResource(Res.string.error_unexpected)
    
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
                    title = stringResource(Res.string.welcome),
                    subtitle = stringResource(Res.string.login_d)
                )

                if (alert != null) {
                    Text(text = alert!!, color = MaterialTheme.colorScheme.error)
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(stringResource(Res.string.username)) },
                    singleLine = true
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(Res.string.password)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val alertErrorFilling = stringResource(Res.string.fill_all_fields)

                    Button(
                        onClick = {
                            if (username.isBlank() || password.isBlank()) {
                                alert = alertErrorFilling
                                return@Button
                            }

                            // Derive auth secret before sending (matches frontend implementation)
                            scope.launch {
                                val derived = deriveAuthSecret(username.trim(), password.trim())
                                
                                apiRequest(
                                    unexpectedError = errorUnexpected,
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
                        Text(stringResource(Res.string.login))
                    }

                    Button(onClick = onNavigateToRegister) {
                        Text(stringResource(Res.string.register_button))
                    }
                }
            }
        }
    }
}