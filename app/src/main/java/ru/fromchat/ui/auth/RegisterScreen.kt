package ru.fromchat.ui.auth

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
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
import kotlinx.coroutines.launch
import ru.fromchat.api.ApiClient
import ru.fromchat.api.RegisterRequest
import ru.fromchat.api.apiRequest
import ru.fromchat.ui.RowHeader

@Composable
fun RegisterScreen(
    onRegistered: () -> Unit
) {
    Scaffold { innerPadding ->
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var alert by remember { mutableStateOf<String?>(null) }

        val scope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RowHeader(
                    icon = Icons.Filled.PersonAdd,
                    title = "Register",
                    subtitle = "Create a new account"
                )

                if (alert != null) {
                    Text(text = alert!!, color = MaterialTheme.colorScheme.error)
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Имя пользователя") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Подтвердите пароль") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                Button(
                    onClick = {
                        // Checks
                        if (username.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                            alert = "Пожалуйста, заполните все поля"
                            return@Button
                        }
                        if (password != confirmPassword) {
                            alert = "Пароли не совпадают"
                            return@Button
                        }
                        if (username.length !in 3..20) {
                            alert = "Имя пользователя должно быть от 3 до 20 символов"
                            return@Button
                        }
                        if (password.length !in 5..50) {
                            alert = "Пароль должен быть от 5 до 50 символов"
                            return@Button
                        }

                        // Send the register request
                        scope.launch {
                            apiRequest(
                                onError = { message, e ->
                                    Log.d("RegisterScreen", "Error while registering:", e)
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
                    Text("Зарегистрироваться")
                }
            }
        }
    }
}