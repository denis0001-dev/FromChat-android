package ru.fromchat.ui

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.End
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Start
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.fromchat.api.WebSocketManager
import ru.fromchat.ui.auth.LoginScreen
import ru.fromchat.ui.auth.RegisterScreen
import ru.fromchat.ui.main.MainScreen

val LocalNavController = compositionLocalOf<NavController> { error("") }

@Composable
fun App() {
    LaunchedEffect(Unit) {
        WebSocketManager.connect()
    }

    FromChatTheme(dynamicColor = false) {
        val navController = rememberNavController()
        val animationSpec = tween<IntOffset>(400)

        CompositionLocalProvider(
            LocalNavController provides navController
        ) {
            NavHost(
                navController = navController,
                startDestination = "login",
                enterTransition = {
                    slideIntoContainer(
                        Start,
                        animationSpec = animationSpec
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        Start,
                        animationSpec = animationSpec
                    )
                },
                popEnterTransition = {
                    slideIntoContainer(
                        End,
                        animationSpec = animationSpec
                    )
                },
                popExitTransition = {
                    slideOutOfContainer(
                        End,
                        animationSpec = animationSpec
                    )
                }
            ) {
                composable("login") {
                    LoginScreen(
                        onLoginSuccess = { navController.navigate("chat") },
                        onNavigateToRegister = { navController.navigate("register") }
                    )
                }

                composable("register") {
                    RegisterScreen(
                        onRegistered = { navController.navigate("login") }
                    )
                }

                composable("chat") { MainScreen() }

                composable("chats/publicChat") { PublicChatScreen() }
            }
        }
    }
}