package ru.fromchat.core

import com.pr0gramm3r101.utils.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.fromchat.ui.Theme

object Settings {
    private val settings = Settings()

    private fun runIO(block: suspend CoroutineScope.() -> Unit) {
        CoroutineScope(Dispatchers.IO).launch(block = block)
    }

    var materialYou: Boolean
        get() = runBlocking { settings.getBoolean("materialYou", true) }
        set(value) = runIO { settings.putBoolean("materialYou", value) }

    var theme: Theme
        get() = runBlocking { Theme.entries[settings.getInt("theme", Theme.AsSystem.ordinal)] }
        set(value) = runIO { settings.putInt("theme", value.ordinal) }
}

