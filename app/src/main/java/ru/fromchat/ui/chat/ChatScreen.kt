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
import androidx.compose.ui.unit.dp

enum class ChatTab { CHATS, CONTACTS, DMS }

@Composable
fun ChatScreen() {
    var selectedTab by remember { mutableStateOf(ChatTab.CHATS) }
    var activeChat by remember { mutableStateOf<String?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == ChatTab.CHATS,
                    onClick = { selectedTab = ChatTab.CHATS },
                    label = { Text("Чаты") },
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = selectedTab == ChatTab.CONTACTS,
                    onClick = { selectedTab = ChatTab.CONTACTS },
                    label = { Text("Контакты") },
                    icon = { Icon(Icons.Filled.Contacts, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = selectedTab == ChatTab.DMS,
                    onClick = { selectedTab = ChatTab.DMS },
                    label = { Text("ЛС") },
                    icon = { Icon(Icons.Filled.Mail, contentDescription = null) }
                )
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(12.dp)) {
            when (selectedTab) {
                ChatTab.CHATS -> {
                    if (activeChat == null) {
                        Text("Общий чат", modifier = Modifier.clickable { activeChat = "Общий чат" }.padding(8.dp))
                        Text("Общий чат 2", modifier = Modifier.clickable { activeChat = "Общий чат 2" }.padding(8.dp))
                    } else {
                        BaseChat(chatTitle = activeChat!!)
                    }
                }
                ChatTab.CONTACTS -> {
                    Text("Скоро будет...")
                }
                ChatTab.DMS -> {
                    Text("Скоро будет...")
                }
            }
        }
    }
}