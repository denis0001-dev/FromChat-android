package ru.fromchat.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.resources.stringResource
import ru.fromchat.Res
import ru.fromchat.chat_last_mesaage
import ru.fromchat.chats
import ru.fromchat.public_chat
import ru.fromchat.ui.LocalNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsTab() {
    val navController = LocalNavController.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.chats),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(contentPadding = innerPadding) {
            item {
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.public_chat)) },
                    supportingContent = { Text(stringResource(Res.string.chat_last_mesaage)) },
                    modifier = Modifier.clickable {
                        navController.navigate("chats/publicChat")
                    }
                )
            }
        }
    }
}