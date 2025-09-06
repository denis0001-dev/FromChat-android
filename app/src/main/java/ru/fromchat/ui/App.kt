package ru.fromchat.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigationBar(selectedItem: Int, onSelectedItemChange: (Int) -> Unit) {
    val items = listOf("Chats", "DMs", "Settings")
    val selectedIcons = listOf(
        Icons.Filled.Group,
        Icons.AutoMirrored.Filled.Chat,
        Icons.Filled.Settings
    )
    val unselectedIcons = listOf(
        Icons.Outlined.Group,
        Icons.AutoMirrored.Outlined.Chat,
        Icons.Outlined.Settings
    )

    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        if (selectedItem == index) selectedIcons[index] else unselectedIcons[index],
                        contentDescription = item,
                    )
                },
                label = { Text(item) },
                selected = selectedItem == index,
                onClick = { onSelectedItemChange(index) },
            )
        }
    }
}

@Composable
fun App() {
    FromChatTheme {
        var selectedItem by remember { mutableIntStateOf(0) }
        val navController = rememberNavController()

        LaunchedEffect(selectedItem) {
            navController.navigate(
                when (selectedItem) {
                    0 -> "chats"
                    1 -> "dms"
                    2 -> "settings"
                    else -> error("")
                }
            )
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                AppNavigationBar(
                    selectedItem = selectedItem,
                    onSelectedItemChange = { selectedItem = it }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "chats",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("chats") {
                    Text("Chats")
                }
                composable("dms") {
                    Text("DMs")
                }
                composable("settings") {
                    Text("Settings")
                }
            }
        }
    }
}