package ru.fromchat.core.config

import com.pr0gramm3r101.utils.storage.ServerConfigData
import com.pr0gramm3r101.utils.storage.ServerConfigStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
    suspend fun initialize() {
        _serverConfig.value = ServerConfigStorage.getConfig()
    }
    
    /**
     * Update server configuration
     */
    suspend fun updateServerConfig(config: ServerConfigData) {
        ServerConfigStorage.saveConfig(config)
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
    
    /**
     * Checks if server configuration exists
     */
    suspend fun hasServerConfig() = ServerConfigStorage.hasConfiguration()
}
