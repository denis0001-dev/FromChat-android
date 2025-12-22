package com.pr0gramm3r101.utils.storage

/**
 * Server configuration data
 */
data class ServerConfigData(
    val serverUrl: String,
    val httpsEnabled: Boolean
)

/**
 * Server configuration storage
 */
object ServerConfigStorage {
    private val storage = NamespacedStorage("server_config")
    
    private const val SERVER_URL_KEY = "server_url"
    private const val HTTPS_ENABLED_KEY = "https_enabled"
    
    suspend fun getServerUrl() = storage.getString(SERVER_URL_KEY).ifEmpty { null }
    
    suspend fun setServerUrl(url: String) = storage.putString(SERVER_URL_KEY, url)
    
    suspend fun getHttpsEnabled() =
        if (storage.contains(HTTPS_ENABLED_KEY))
            storage.getBoolean(HTTPS_ENABLED_KEY, true)
        else null
    
    suspend fun setHttpsEnabled(enabled: Boolean) {
        storage.putBoolean(HTTPS_ENABLED_KEY, enabled)
    }
    
    suspend fun hasConfiguration() = getServerUrl() != null && getHttpsEnabled() != null
    
    suspend fun getConfig(): ServerConfigData {
        var url = getServerUrl()
        var https = getHttpsEnabled()

        if (url == null || https == null) {
            url = "fromchat.ru"
            https = true
            saveConfig(ServerConfigData(url, https))
        }

        return ServerConfigData(url, https)
    }
    
    suspend fun saveConfig(config: ServerConfigData) {
        setServerUrl(config.serverUrl)
        setHttpsEnabled(config.httpsEnabled)
    }
}
