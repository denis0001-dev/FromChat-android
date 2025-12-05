package ru.fromchat.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ru.fromchat.R
import ru.fromchat.utils.exclude

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf("chats") }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == "chats",
                    onClick = { selectedTab = "chats" },
                    label = { Text(stringResource(R.string.chats)) },
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = selectedTab == "contacts",
                    onClick = { selectedTab = "contacts" },
                    label = { Text(stringResource(R.string.contacts)) },
                    icon = { Icon(Icons.Filled.Contacts, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = selectedTab == "dms",
                    onClick = { selectedTab = "dms" },
                    label = { Text(stringResource(R.string.dms)) },
                    icon = { Icon(Icons.Filled.Mail, contentDescription = null) }
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing.exclude(WindowInsetsSides.Top),
        modifier = Modifier.imePadding()
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                "chats" -> ChatsTab()
                "contacts" -> {
                    Text(stringResource(R.string.coming_soon))
                }
                "dms" -> {
                    Text(stringResource(R.string.coming_soon))
                }
            }
        }
    }
}