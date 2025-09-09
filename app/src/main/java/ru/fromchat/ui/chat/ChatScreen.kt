package ru.fromchat.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.unit.dp
import ru.fromchat.R
import ru.fromchat.ui.LocalNavController

enum class ChatTab { CHATS, CONTACTS, DMS }

@Composable
fun ChatScreen() {
    var selectedTab by remember { mutableStateOf(ChatTab.CHATS) }
    val navController = LocalNavController.current

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == ChatTab.CHATS,
                    onClick = { selectedTab = ChatTab.CHATS },
                    label = { Text(stringResource(R.string.chats)) },
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = selectedTab == ChatTab.CONTACTS,
                    onClick = { selectedTab = ChatTab.CONTACTS },
                    label = { Text(stringResource(R.string.contacts)) },
                    icon = { Icon(Icons.Filled.Contacts, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = selectedTab == ChatTab.DMS,
                    onClick = { selectedTab = ChatTab.DMS },
                    label = { Text(stringResource(R.string.dms)) },
                    icon = { Icon(Icons.Filled.Mail, contentDescription = null) }
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(12.dp)) {
            when (selectedTab) {
                ChatTab.CHATS -> {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.public_chat)) },
                        supportingContent = { Text(stringResource(R.string.chat_last_mesaage)) },
                        modifier = Modifier.clickable {
                            navController.navigate("chats/publicChat")
                        }
                    )
                }
                ChatTab.CONTACTS -> {
                    Text(stringResource(R.string.coming_soon))
                }
                ChatTab.DMS -> {
                    Text(stringResource(R.string.coming_soon))
                }
            }
        }
    }
}