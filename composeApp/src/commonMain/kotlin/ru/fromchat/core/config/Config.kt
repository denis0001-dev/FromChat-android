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
    fun getApiBaseUrl(): String {
        val config = _serverConfig.value ?: ServerConfigData("fromchat.ru", true)
        val protocol = if (config.httpsEnabled) "https" else "http"
        return "$protocol://${config.serverUrl}/api"
    }
    
    /**
     * Get WebSocket URL based on current server configuration
     */
    fun getWebSocketUrl(): String {
        val config = _serverConfig.value ?: ServerConfigData("fromchat.ru", true)
        val protocol = if (config.httpsEnabled) "wss" else "ws"
        return "$protocol://${config.serverUrl}/api/chat/ws"
    }
    
    /**
     * Checks if server configuration exists
     */
    suspend fun hasServerConfig(): Boolean {
        return ServerConfigStorage.hasConfiguration()
    }
}
