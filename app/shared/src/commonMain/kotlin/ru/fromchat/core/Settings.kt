package ru.fromchat.core

import com.pr0gramm3r101.utils.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.fromchat.ui.Theme

/**
 * Server configuration data
 */
data class ServerConfigData(
    val serverUrl: String,
    val httpsEnabled: Boolean
)

object Settings {
    private const val MATERIAL_YOU_KEY = "materialYou"
    private const val THEME_KEY = "theme"
    private const val SERVER_URL_KEY = "server_url"
    private const val HTTPS_ENABLED_KEY = "https_enabled"

    private val settings = Settings()

    private fun runIO(block: suspend CoroutineScope.() -> Unit) {
        CoroutineScope(Dispatchers.IO).launch(block = block)
    }

    private fun serverConfigNotInitialized(): Nothing
        = throw IllegalStateException("Server config not initialized")

    var materialYou: Boolean
        get() = runBlocking { settings.getBoolean(MATERIAL_YOU_KEY, true) }
        set(value) = runIO { settings.putBoolean(MATERIAL_YOU_KEY, value) }

    var theme: Theme
        get() = runBlocking { Theme.entries[settings.getInt(THEME_KEY, Theme.AsSystem.ordinal)] }
        set(value) = runIO { settings.putInt(THEME_KEY, value.ordinal) }

    var serverUrl: String
        get() = runBlocking {
            settings.getString(SERVER_URL_KEY).ifEmpty {
                serverConfigNotInitialized()
            }
        }
        set(value) = runIO { settings.putString(SERVER_URL_KEY, value) }

    var httpsEnabled: Boolean
        get() = runBlocking {
            if (settings.contains(HTTPS_ENABLED_KEY))
                settings.getBoolean(HTTPS_ENABLED_KEY, true)
            else serverConfigNotInitialized()
        }
        set(value) = runIO { settings.putBoolean(HTTPS_ENABLED_KEY, value) }

    val hasServerConfig get() = try {
        serverUrl
        httpsEnabled
        true
    } catch (_: IllegalStateException) {
        false
    }

    var serverConfig: ServerConfigData
        get() {
            if (!hasServerConfig) {
                serverUrl = "fromchat.ru"
                httpsEnabled = true

                return ServerConfigData("fromchat.ru", true)
            }

            return ServerConfigData(serverUrl, httpsEnabled)
        }
        set(value) {
            serverUrl = value.serverUrl
            httpsEnabled = value.httpsEnabled
        }
}

