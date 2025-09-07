package ru.fromchat.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ru.fromchat.ui.auth.LoginScreen
import ru.fromchat.ui.auth.RegisterScreen
import ru.fromchat.ui.chat.ChatScreen

@Composable
fun App() {
    FromChatTheme {
        val (route, setRoute) = remember { mutableStateOf("login") }
        when (route) {
            "login" -> LoginScreen(
                onLoginSuccess = { setRoute("chat") },
                onNavigateToRegister = { setRoute("register") }
            )
            "register" -> RegisterScreen(onRegistered = { setRoute("login") })
            "chat" -> ChatScreen()
        }
    }
}