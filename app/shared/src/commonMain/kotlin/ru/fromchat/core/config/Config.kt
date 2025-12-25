package ru.fromchat.core.config

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.fromchat.core.ServerConfigData
import ru.fromchat.core.Settings

/**
 * Application configuration
 */
object Config {
    private val _serverConfig = MutableStateFlow<ServerConfigData?>(null)
    val serverConfig: StateFlow<ServerConfigData?> = _serverConfig.asStateFlow()

    private val config
        get() = _serverConfig.value ?: throw IllegalStateException("Server configuration not initialized")
    
    /**
     * Initialize configuration by loading from storage
     */
    fun initialize() {
        _serverConfig.value = Settings.serverConfig
    }
    
    /**
     * Update server configuration
     */
    fun updateServerConfig(config: ServerConfigData) {
        Settings.serverConfig = config
        _serverConfig.value = config
    }
    
    /**
     * Get API base URL based on current server configuration
     */
    val apiBaseUrl
        get() = "${if (config.httpsEnabled) "https" else "http"}://${config.serverUrl}/api"
    
    /**
     * Get WebSocket URL based on current server configuration
     */
    val webSocketUrl
        get() = "${if (config.httpsEnabled) "wss" else "ws"}://${config.serverUrl}/api/chat/ws"
}
