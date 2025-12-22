package ru.fromchat.ui

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.End
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Start
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.fromchat.api.WebSocketManager
import ru.fromchat.core.config.Config
import ru.fromchat.ui.auth.LoginScreen
import ru.fromchat.ui.auth.RegisterScreen
import ru.fromchat.ui.chat.PublicChatScreen
import ru.fromchat.ui.main.MainScreen
import ru.fromchat.ui.setup.ServerConfigScreen

val LocalNavController = compositionLocalOf<NavController> { error("") }

@Composable
fun App() {
    LaunchedEffect(Unit) {
        runCatching {
            Config.initialize()
        }
    }

    // Observe lifecycle events to manage WebSocket connection
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    // Connect WebSocket when app comes to foreground
                    WebSocketManager.connect()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    // Disconnect WebSocket when app goes to background
                    WebSocketManager.disconnect()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    FromChatTheme {
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
                    composable("serverConfig") {
                        ServerConfigScreen()
                    }

                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate("chat") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onNavigateToRegister = { navController.navigate("register") }
                        )
                    }

                    composable("register") {
                        RegisterScreen(
                            onRegistered = { navController.navigate("login") }
                        )
                    }

                    composable("chat") {
                        MainScreen(
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo("chat") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("chats/publicChat") {
                        PublicChatScreen()
                    }

                    composable("about") {
                        AboutScreen()
                    }
                }
        }
    }
}